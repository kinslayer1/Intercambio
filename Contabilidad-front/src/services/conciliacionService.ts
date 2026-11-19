import type { Banco } from "../models/Banco";
import type { Conciliacion } from "../models/Conciliacion";
import type { MovimientoAuxiliar } from "../models/MovimientoAuxiliar";
import type { MovimientoExtracto } from "../models/MovimientoExtracto";
import type { CorporateRole } from "../models/Rol";
import { obtenerBancosActivos } from "./bancoService";
import {
  obtenerMovimientosAuxiliar,
  obtenerMovimientosExtracto,
  obtenerSaldoContable,
  validarConciliacionHabilitada
} from "./cargaService";
import { estaFechaBloqueada } from "./cierreService";

const CONCILIACIONES_STORAGE_KEY = "conciliacion.registros";
const AUDITORIA_STORAGE_KEY = "conciliacion.auditoria";

type CruceValidation = {
  ok: boolean;
  totalExtracto: number;
  totalAuxiliar: number;
  diferencia: number;
  message: string;
};

type AplicarCruceInput = {
  bancoId: number;
  fechaConciliacion: string;
  extractoIds: number[];
  auxiliarIds: number[];
  usuario: string;
  roles: CorporateRole[];
};

type AplicarCruceResult =
  | { ok: true; message: string; cruzado: number }
  | { ok: false; message: string };

export interface AuditoriaConciliacionEvent {
  id: number;
  fechaHora: string;
  usuario: string;
  bancoId: number;
  fechaConciliacion: string;
  cruzado: number;
  cantidadRegistros: number;
  periodo: string;
}

interface ReportePreliminar {
  oficina: string;
  banco: string;
  sucursal: string;
  fecha: string;
  cuentaCorriente: string;
  cuentaAuxiliar: string;
  saldoSegunLibros: number;
  consignacionesPendientesContabilizar: number;
  chequesPendientesContabilizar: number;
  consignacionesPendientesAbonar: number;
  saldoSegunExtractoBancario: number;
}

type ContextoConciliacionOk = {
  ok: true;
  data: {
    reportePreliminar: ReportePreliminar;
    extractoPendiente: MovimientoExtracto[];
    auxiliarPendiente: MovimientoAuxiliar[];
  };
};

type ContextoConciliacionError = {
  ok: false;
  message: string;
};

type ContextoConciliacion = ContextoConciliacionOk | ContextoConciliacionError;

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

const monthToPeriodo = (fecha: string): string => fecha.slice(0, 7).replace("-", "");

const getConciliadosIds = (bancoId: number, fechaConciliacion: string): Set<number> => {
  const conciliaciones = obtenerConciliaciones().filter(
    (row) => row.bancoId === bancoId && row.fechaConciliacion === fechaConciliacion
  );

  const ids = new Set<number>();
  conciliaciones.forEach((row) => {
    row.extractos.forEach((id) => ids.add(id));
    row.auxiliares.forEach((id) => ids.add(id));
  });
  return ids;
};

const obtenerBancoActivo = (bancoId: number): Banco | null => {
  return obtenerBancosActivos().find((banco) => banco.id === bancoId) ?? null;
};

const calcularCruce = (extractoRows: MovimientoExtracto[], auxiliarRows: MovimientoAuxiliar[]): CruceValidation => {
  if (extractoRows.length === 0 || auxiliarRows.length === 0) {
    return {
      ok: false,
      totalExtracto: 0,
      totalAuxiliar: 0,
      diferencia: 0,
      message: "Debes seleccionar al menos un movimiento de cada panel"
    };
  }

  const totalExtracto = extractoRows.reduce((acc, row) => acc + row.valor, 0);
  const totalAuxiliar = auxiliarRows.reduce((acc, row) => acc + row.valor, 0);
  const diferencia = totalExtracto - totalAuxiliar;

  if (Math.abs(diferencia) > 0.000001) {
    return {
      ok: false,
      totalExtracto,
      totalAuxiliar,
      diferencia,
      message: "La diferencia debe ser igual a cero para aplicar el cruce"
    };
  }

  return {
    ok: true,
    totalExtracto,
    totalAuxiliar,
    diferencia: 0,
    message: "Cruce valido"
  };
};

const registrarAuditoria = (
  usuario: string,
  bancoId: number,
  fechaConciliacion: string,
  cruzado: number,
  cantidadRegistros: number
): void => {
  const eventos = obtenerAuditoriaConciliacion();
  const evento: AuditoriaConciliacionEvent = {
    id: Date.now() + Math.floor(Math.random() * 1000),
    fechaHora: new Date().toISOString(),
    usuario,
    bancoId,
    fechaConciliacion,
    cruzado,
    cantidadRegistros,
    periodo: monthToPeriodo(fechaConciliacion)
  };
  writeArray(AUDITORIA_STORAGE_KEY, [evento, ...eventos]);
};

export const obtenerAuditoriaConciliacion = (): AuditoriaConciliacionEvent[] => {
  return readArray<AuditoriaConciliacionEvent>(AUDITORIA_STORAGE_KEY);
};

export const obtenerConciliaciones = (): Conciliacion[] => {
  return readArray<Conciliacion>(CONCILIACIONES_STORAGE_KEY);
};

export const obtenerConciliacionesPorPeriodo = (periodo: string): Conciliacion[] => {
  return obtenerConciliaciones().filter((row) => row.periodo === periodo);
};

export const obtenerBancosPendientesPorConciliar = (fechaConciliacion: string): Banco[] => {
  return obtenerBancosActivos().filter((banco) => {
    const validacion = validarConciliacionHabilitada(banco.id, fechaConciliacion);
    return validacion.habilitada;
  });
};

export const obtenerContextoConciliacion = (
  bancoId: number,
  fechaConciliacion: string
): ContextoConciliacion => {
  if (estaFechaBloqueada(fechaConciliacion)) {
    return {
      ok: false,
      message: "El periodo se encuentra cerrado para conciliacion"
    };
  }

  const banco = obtenerBancoActivo(bancoId);
  if (banco === null) {
    return {
      ok: false,
      message: "El banco no existe o se encuentra inactivo"
    };
  }

  const validacion = validarConciliacionHabilitada(bancoId, fechaConciliacion);
  if (!validacion.habilitada) {
    return {
      ok: false,
      message: validacion.message
    };
  }

  const conciliados = getConciliadosIds(bancoId, fechaConciliacion);
  const extractoPendiente = obtenerMovimientosExtracto(bancoId, fechaConciliacion).filter(
    (row) => !conciliados.has(row.id)
  );
  const auxiliarPendiente = obtenerMovimientosAuxiliar(bancoId, fechaConciliacion).filter(
    (row) => !conciliados.has(row.id)
  );

  const saldo = obtenerSaldoContable(bancoId, fechaConciliacion);
  const saldoSegunLibros = saldo?.saldoUsd ?? 0;
  const consignacionesPendientesContabilizar = extractoPendiente
    .filter((row) => row.tipo === "D")
    .reduce((acc, row) => acc + row.valor, 0);
  const chequesPendientesContabilizar = extractoPendiente
    .filter((row) => row.tipo === "C")
    .reduce((acc, row) => acc + row.valor, 0);
  const consignacionesPendientesAbonar = auxiliarPendiente.reduce((acc, row) => acc + row.valor, 0);
  const saldoSegunExtractoBancario =
    saldoSegunLibros +
    consignacionesPendientesContabilizar -
    chequesPendientesContabilizar -
    consignacionesPendientesAbonar;

  return {
    ok: true,
    data: {
      reportePreliminar: {
        oficina: "OFICINA PRINCIPAL",
        banco: banco.nombre,
        sucursal: "SUCURSAL GENERAL",
        fecha: fechaConciliacion,
        cuentaCorriente: banco.cuentaBancaria,
        cuentaAuxiliar: `${banco.id}`,
        saldoSegunLibros,
        consignacionesPendientesContabilizar,
        chequesPendientesContabilizar,
        consignacionesPendientesAbonar,
        saldoSegunExtractoBancario
      },
      extractoPendiente,
      auxiliarPendiente
    }
  };
};

export const validarCruce = (
  bancoId: number,
  fechaConciliacion: string,
  extractoIds: number[],
  auxiliarIds: number[]
): CruceValidation => {
  const contexto = obtenerContextoConciliacion(bancoId, fechaConciliacion);

  if (!contexto.ok) {
    return {
      ok: false,
      totalExtracto: 0,
      totalAuxiliar: 0,
      diferencia: 0,
      message: contexto.message
    };
  }

  const extractoRows = contexto.data.extractoPendiente.filter((row) => extractoIds.includes(row.id));
  const auxiliarRows = contexto.data.auxiliarPendiente.filter((row) => auxiliarIds.includes(row.id));

  return calcularCruce(extractoRows, auxiliarRows);
};

export const aplicarCruce = (input: AplicarCruceInput): AplicarCruceResult => {
  if (estaFechaBloqueada(input.fechaConciliacion)) {
    return {
      ok: false,
      message: "No se pueden aplicar cruces en periodos cerrados"
    };
  }

  if (!input.roles.includes("ANALISTA")) {
    return {
      ok: false,
      message: "No tienes permisos para aplicar conciliaciones"
    };
  }

  const validacion = validarCruce(
    input.bancoId,
    input.fechaConciliacion,
    input.extractoIds,
    input.auxiliarIds
  );

  if (!validacion.ok) {
    return {
      ok: false,
      message: validacion.message
    };
  }

  const conciliaciones = obtenerConciliaciones();
  const ultimoCruzado = conciliaciones.reduce((max, row) => Math.max(max, row.cruzado), 0);
  const nuevoCruzado = ultimoCruzado + 1;

  const nuevoRegistro: Conciliacion = {
    cruzado: nuevoCruzado,
    periodo: monthToPeriodo(input.fechaConciliacion),
    bancoId: input.bancoId,
    fechaConciliacion: input.fechaConciliacion,
    extractos: [...input.extractoIds],
    auxiliares: [...input.auxiliarIds],
    totalExtracto: validacion.totalExtracto,
    totalAuxiliar: validacion.totalAuxiliar,
    cantidadExtractos: input.extractoIds.length,
    cantidadAuxiliares: input.auxiliarIds.length,
    usuario: input.usuario,
    fechaRegistro: new Date().toISOString()
  };

  writeArray(CONCILIACIONES_STORAGE_KEY, [...conciliaciones, nuevoRegistro]);
  registrarAuditoria(
    input.usuario,
    input.bancoId,
    input.fechaConciliacion,
    nuevoCruzado,
    input.extractoIds.length + input.auxiliarIds.length
  );

  return {
    ok: true,
    message: `Cruce aplicado con identificador CRUZADO ${nuevoCruzado}`,
    cruzado: nuevoCruzado
  };
};

export const exportarReportePreliminarTexto = (reporte: ReportePreliminar): string => {
  return [
    "REPORTE PRELIMINAR DE CONCILIACION",
    `Oficina: ${reporte.oficina}`,
    `Banco: ${reporte.banco}`,
    `Sucursal: ${reporte.sucursal}`,
    `Fecha: ${reporte.fecha}`,
    `Cuenta corriente: ${reporte.cuentaCorriente}`,
    `Cuenta auxiliar: ${reporte.cuentaAuxiliar}`,
    `Saldo segun libros: ${reporte.saldoSegunLibros.toFixed(2)}`,
    `Consignaciones pendientes de contabilizar: ${reporte.consignacionesPendientesContabilizar.toFixed(2)}`,
    `Cheques pendientes de contabilizar: ${reporte.chequesPendientesContabilizar.toFixed(2)}`,
    `Consignaciones pendientes de abonar: ${reporte.consignacionesPendientesAbonar.toFixed(2)}`,
    `Saldo segun extracto bancario: ${reporte.saldoSegunExtractoBancario.toFixed(2)}`
  ].join("\n");
};
