import type { MovimientoAuxiliar } from "../models/MovimientoAuxiliar";
import type { MovimientoExtracto } from "../models/MovimientoExtracto";
import type { CorporateRole } from "../models/Rol";
import type { SaldoContable } from "../models/SaldoContable";
import { obtenerBancosActivos } from "./bancoService";
import { estaFechaBloqueada } from "./cierreService";

const AUXILIAR_STORAGE_KEY = "carga.auxiliar";
const EXTRACTO_STORAGE_KEY = "carga.extracto";
const SALDO_STORAGE_KEY = "carga.saldos";
const AUDITORIA_STORAGE_KEY = "carga.auditoria";

type CargaTipo = "SAP" | "EXTRACTO" | "SALDO";

type CargaIssue = {
  linea: number;
  campo: string;
  mensaje: string;
};

export interface CargaAuditEvent {
  id: number;
  fechaHora: string;
  tipo: CargaTipo;
  usuario: string;
  bancoId: number;
  fechaConciliacion: string;
  nombreArchivo?: string;
  resultado: "EXITOSO" | "RECHAZADO";
  motivo?: string;
}

type CargaResumen = {
  totalEntrada: number;
  cargados: number;
  descartadosValorCero: number;
  descartadosContrapartidaCero: number;
};

type ProcesarCargaInput = {
  bancoId: number;
  fechaConciliacion: string;
  saldoUsd: number;
  sapFileName: string;
  sapContent: string;
  extractoFileName: string;
  extractoContent: string;
  usuario: string;
  roles: CorporateRole[];
};

type CargaSuccess = {
  ok: true;
  message: string;
  resumenSap: CargaResumen;
  resumenExtracto: CargaResumen;
  conciliacionHabilitada: boolean;
};

type CargaFailureCode = "CARGA-001" | "CARGA-002" | "CARGA-003" | "CARGA-004" | "CARGA-007";
type CargaFailure = {
  ok: false;
  code: CargaFailureCode;
  message: string;
  issues?: CargaIssue[];
};

type CsvMovimiento = {
  fecha: string;
  concepto: string;
  referencia: string;
  tipo: "D" | "C";
  valor: number;
};

const readArray = <T>(key: string): T[] => {
  const raw = localStorage.getItem(key);
  if (raw === null) {
    return [];
  }

  try {
    return JSON.parse(raw) as T[];
  } catch {
    return [];
  }
};

const writeArray = <T>(key: string, values: T[]): void => {
  localStorage.setItem(key, JSON.stringify(values));
};

const parseCsvLine = (line: string): string[] => {
  const cells: string[] = [];
  let current = "";
  let insideQuotes = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    const nextChar = line[index + 1];

    if (char === '"' && insideQuotes && nextChar === '"') {
      current += '"';
      index += 1;
      continue;
    }

    if (char === '"') {
      insideQuotes = !insideQuotes;
      continue;
    }

    if (char === "," && !insideQuotes) {
      cells.push(current.trim());
      current = "";
      continue;
    }

    current += char;
  }

  cells.push(current.trim());
  return cells;
};

const parseCsvContent = (content: string): string[][] => {
  return content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .map(parseCsvLine);
};

const parseDecimal = (value: string): number | null => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

const parseTipo = (value: string): "D" | "C" | null => {
  const normalized = value.trim().toUpperCase();

  if (normalized === "D") {
    return "D";
  }
  if (normalized === "C") {
    return "C";
  }
  return null;
};

const parseMovimientos = (content: string): { movimientos: CsvMovimiento[]; issues: CargaIssue[] } => {
  const rows = parseCsvContent(content);

  if (rows.length === 0) {
    return {
      movimientos: [],
      issues: [{ linea: 1, campo: "archivo", mensaje: "Archivo vacio" }]
    };
  }

  const headers = rows[0].map((cell) => cell.toLowerCase());
  const expectedHeaders = ["fecha", "concepto", "referencia", "tipo", "valor"];
  const hasExpectedHeaders = expectedHeaders.every((header) => headers.includes(header));

  if (!hasExpectedHeaders) {
    return {
      movimientos: [],
      issues: [{ linea: 1, campo: "estructura", mensaje: "Estructura CSV invalida" }]
    };
  }

  const headerIndex = Object.fromEntries(headers.map((header, index) => [header, index])) as Record<string, number>;
  const issues: CargaIssue[] = [];
  const movimientos: CsvMovimiento[] = [];

  rows.slice(1).forEach((row, rowIndex) => {
    const linea = rowIndex + 2;
    const fecha = row[headerIndex.fecha] ?? "";
    const concepto = row[headerIndex.concepto] ?? "";
    const referencia = row[headerIndex.referencia] ?? "";
    const tipoRaw = row[headerIndex.tipo] ?? "";
    const valorRaw = row[headerIndex.valor] ?? "";

    const tipo = parseTipo(tipoRaw);
    const valor = parseDecimal(valorRaw);

    if (fecha.length === 0 || concepto.length === 0 || referencia.length === 0 || tipo === null || valor === null) {
      issues.push({ linea, campo: "registro", mensaje: "Registro invalido" });
      return;
    }

    movimientos.push({
      fecha,
      concepto,
      referencia,
      tipo,
      valor
    });
  });

  return { movimientos, issues };
};

const filtrarMovimientos = (movimientos: CsvMovimiento[]): { rows: CsvMovimiento[]; resumen: CargaResumen } => {
  const totalEntrada = movimientos.length;
  const sinValorCero = movimientos.filter((row) => row.valor !== 0);
  const descartadosValorCero = totalEntrada - sinValorCero.length;

  const referenciasContrapartidaCero = new Set<string>();
  const sumatoriaPorReferencia = new Map<string, number>();

  sinValorCero.forEach((row) => {
    const actual = sumatoriaPorReferencia.get(row.referencia) ?? 0;
    const signed = row.tipo === "D" ? row.valor : -row.valor;
    sumatoriaPorReferencia.set(row.referencia, actual + signed);
  });

  sumatoriaPorReferencia.forEach((sumatoria, referencia) => {
    if (Math.abs(sumatoria) < 0.000001) {
      referenciasContrapartidaCero.add(referencia);
    }
  });

  const rows = sinValorCero.filter((row) => !referenciasContrapartidaCero.has(row.referencia));
  const descartadosContrapartidaCero = sinValorCero.length - rows.length;

  return {
    rows,
    resumen: {
      totalEntrada,
      cargados: rows.length,
      descartadosValorCero,
      descartadosContrapartidaCero
    }
  };
};

const registrarAuditoria = (
  tipo: CargaTipo,
  usuario: string,
  bancoId: number,
  fechaConciliacion: string,
  resultado: CargaAuditEvent["resultado"],
  nombreArchivo?: string,
  motivo?: string
): void => {
  const auditoria = obtenerAuditoriaCargas();
  const evento: CargaAuditEvent = {
    id: Date.now() + Math.floor(Math.random() * 1000),
    fechaHora: new Date().toISOString(),
    tipo,
    usuario,
    bancoId,
    fechaConciliacion,
    nombreArchivo,
    resultado,
    motivo
  };
  writeArray(AUDITORIA_STORAGE_KEY, [evento, ...auditoria]);
};

const replaceMovimientos = <T extends { bancoId: number; fechaConciliacion: string }>(
  current: T[],
  replacements: T[],
  bancoId: number,
  fechaConciliacion: string
): T[] => {
  const remaining = current.filter(
    (item) => item.bancoId !== bancoId || item.fechaConciliacion !== fechaConciliacion
  );
  return [...remaining, ...replacements];
};

export const obtenerMovimientosAuxiliar = (bancoId?: number, fechaConciliacion?: string): MovimientoAuxiliar[] => {
  const rows = readArray<MovimientoAuxiliar>(AUXILIAR_STORAGE_KEY);

  return rows.filter((row) => {
    if (bancoId !== undefined && row.bancoId !== bancoId) {
      return false;
    }
    if (fechaConciliacion !== undefined && row.fechaConciliacion !== fechaConciliacion) {
      return false;
    }
    return true;
  });
};

export const obtenerMovimientosExtracto = (bancoId?: number, fechaConciliacion?: string): MovimientoExtracto[] => {
  const rows = readArray<MovimientoExtracto>(EXTRACTO_STORAGE_KEY);

  return rows.filter((row) => {
    if (bancoId !== undefined && row.bancoId !== bancoId) {
      return false;
    }
    if (fechaConciliacion !== undefined && row.fechaConciliacion !== fechaConciliacion) {
      return false;
    }
    return true;
  });
};

export const obtenerSaldoContable = (bancoId: number, fechaConciliacion: string): SaldoContable | null => {
  const rows = readArray<SaldoContable>(SALDO_STORAGE_KEY);
  return rows.find((row) => row.bancoId === bancoId && row.fechaConciliacion === fechaConciliacion) ?? null;
};

export const obtenerAuditoriaCargas = (): CargaAuditEvent[] => {
  return readArray<CargaAuditEvent>(AUDITORIA_STORAGE_KEY);
};

export const validarConciliacionHabilitada = (
  bancoId: number,
  fechaConciliacion: string
): { habilitada: boolean; message: string } => {
  if (estaFechaBloqueada(fechaConciliacion)) {
    return {
      habilitada: false,
      message: "El periodo se encuentra cerrado"
    };
  }

  const auxiliar = obtenerMovimientosAuxiliar(bancoId, fechaConciliacion);
  const extracto = obtenerMovimientosExtracto(bancoId, fechaConciliacion);
  const saldo = obtenerSaldoContable(bancoId, fechaConciliacion);

  if (auxiliar.length === 0 || extracto.length === 0 || saldo === null) {
    return {
      habilitada: false,
      message: "Falta cargar informacion en AUXILIAR, EXTRACTO u OFICINAS"
    };
  }

  return {
    habilitada: true,
    message: "Conciliacion habilitada"
  };
};

export const procesarCargaInformacion = (input: ProcesarCargaInput): CargaSuccess | CargaFailure => {
  if (!input.roles.includes("ANALISTA")) {
    return {
      ok: false,
      code: "CARGA-001",
      message: "Solo el rol ANALISTA puede ejecutar cargas"
    };
  }

  if (estaFechaBloqueada(input.fechaConciliacion)) {
    return {
      ok: false,
      code: "CARGA-002",
      message: "No se puede cargar informacion en un periodo cerrado"
    };
  }

  const bancoActivo = obtenerBancosActivos().find((banco) => banco.id === input.bancoId);
  if (bancoActivo === undefined) {
    return {
      ok: false,
      code: "CARGA-003",
      message: "El banco seleccionado no se encuentra activo"
    };
  }

  if (!Number.isFinite(input.saldoUsd)) {
    return {
      ok: false,
      code: "CARGA-004",
      message: "El saldo contable debe ser un valor numerico valido"
    };
  }

  const sapParsed = parseMovimientos(input.sapContent);
  if (sapParsed.issues.length > 0) {
    registrarAuditoria(
      "SAP",
      input.usuario,
      input.bancoId,
      input.fechaConciliacion,
      "RECHAZADO",
      input.sapFileName,
      "Estructura invalida"
    );
    return {
      ok: false,
      code: "CARGA-007",
      message: "Archivo SAP invalido",
      issues: sapParsed.issues
    };
  }

  const extractoParsed = parseMovimientos(input.extractoContent);
  if (extractoParsed.issues.length > 0) {
    registrarAuditoria(
      "EXTRACTO",
      input.usuario,
      input.bancoId,
      input.fechaConciliacion,
      "RECHAZADO",
      input.extractoFileName,
      "Estructura invalida"
    );
    return {
      ok: false,
      code: "CARGA-007",
      message: "Archivo de extracto invalido",
      issues: extractoParsed.issues
    };
  }

  const sapFiltrado = filtrarMovimientos(sapParsed.movimientos);
  const extractoFiltrado = filtrarMovimientos(extractoParsed.movimientos);

  const sapRows: MovimientoAuxiliar[] = sapFiltrado.rows.map((row, index) => ({
    id: Date.now() + index + Math.floor(Math.random() * 1000),
    bancoId: input.bancoId,
    fechaConciliacion: input.fechaConciliacion,
    fecha: row.fecha,
    concepto: row.concepto,
    referencia: row.referencia,
    tipo: row.tipo,
    valor: Math.abs(row.valor)
  }));

  const extractoRows: MovimientoExtracto[] = extractoFiltrado.rows.map((row, index) => ({
    id: Date.now() + 10_000 + index + Math.floor(Math.random() * 1000),
    bancoId: input.bancoId,
    fechaConciliacion: input.fechaConciliacion,
    fecha: row.fecha,
    concepto: row.concepto,
    referencia: row.referencia,
    tipo: row.tipo,
    valor: Math.abs(row.valor)
  }));

  const auxiliarActual = readArray<MovimientoAuxiliar>(AUXILIAR_STORAGE_KEY);
  const extractoActual = readArray<MovimientoExtracto>(EXTRACTO_STORAGE_KEY);
  const saldosActuales = readArray<SaldoContable>(SALDO_STORAGE_KEY);

  writeArray(
    AUXILIAR_STORAGE_KEY,
    replaceMovimientos(auxiliarActual, sapRows, input.bancoId, input.fechaConciliacion)
  );
  writeArray(
    EXTRACTO_STORAGE_KEY,
    replaceMovimientos(extractoActual, extractoRows, input.bancoId, input.fechaConciliacion)
  );
  writeArray(
    SALDO_STORAGE_KEY,
    replaceMovimientos(
      saldosActuales,
      [
        {
          bancoId: input.bancoId,
          fechaConciliacion: input.fechaConciliacion,
          saldoUsd: Number(input.saldoUsd),
          fechaRegistro: new Date().toISOString()
        }
      ],
      input.bancoId,
      input.fechaConciliacion
    )
  );

  registrarAuditoria(
    "SAP",
    input.usuario,
    input.bancoId,
    input.fechaConciliacion,
    "EXITOSO",
    input.sapFileName
  );
  registrarAuditoria(
    "EXTRACTO",
    input.usuario,
    input.bancoId,
    input.fechaConciliacion,
    "EXITOSO",
    input.extractoFileName
  );
  registrarAuditoria("SALDO", input.usuario, input.bancoId, input.fechaConciliacion, "EXITOSO");

  const conciliacion = validarConciliacionHabilitada(input.bancoId, input.fechaConciliacion);

  return {
    ok: true,
    message: "Carga procesada correctamente",
    resumenSap: sapFiltrado.resumen,
    resumenExtracto: extractoFiltrado.resumen,
    conciliacionHabilitada: conciliacion.habilitada
  };
};

