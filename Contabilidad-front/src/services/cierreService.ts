import type { CorporateRole } from "../models/Rol";

const PERIODOS_CERRADOS_STORAGE_KEY = "cierres.periodos";
const AUDITORIA_CIERRES_STORAGE_KEY = "cierres.auditoria";

export interface PeriodoCerrado {
  periodo: string;
  usuarioCierre: string;
  fechaHoraCierre: string;
}

export interface AuditoriaCierreEvent {
  id: number;
  fechaHora: string;
  usuario: string;
  operacion: "CIERRE_MENSUAL";
  periodo: string;
  resultado: "EXITOSO" | "RECHAZADO";
  motivo?: string;
}

type CerrarPeriodoInput = {
  periodo: string;
  usuario: string;
  roles: CorporateRole[];
  confirmado: boolean;
};

type CierreSuccess = {
  ok: true;
  message: string;
};

type CierreFailure = {
  ok: false;
  code: "CIERRE-001" | "CIERRE-002" | "CIERRE-003" | "CIERRE-004";
  message: string;
};

export type CierreResult = CierreSuccess | CierreFailure;

type EstadoPeriodo = "ABIERTO" | "CERRADO";

interface PeriodoConEstado {
  periodo: string;
  estado: EstadoPeriodo;
  usuarioCierre: string | null;
  fechaHoraCierre: string | null;
}

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

const registrarAuditoria = (
  usuario: string,
  periodo: string,
  resultado: AuditoriaCierreEvent["resultado"],
  motivo?: string
): void => {
  const auditoria = obtenerAuditoriaCierres();
  const evento: AuditoriaCierreEvent = {
    id: Date.now(),
    fechaHora: new Date().toISOString(),
    usuario,
    operacion: "CIERRE_MENSUAL",
    periodo,
    resultado,
    motivo
  };
  writeArray(AUDITORIA_CIERRES_STORAGE_KEY, [evento, ...auditoria]);
};

const isPeriodoValido = (periodo: string): boolean => /^\d{6}$/.test(periodo);

const periodoFromFecha = (fecha: string): string | null => {
  const match = fecha.match(/^(\d{4})-(\d{2})-\d{2}$/);
  if (match === null) {
    return null;
  }
  return `${match[1]}${match[2]}`;
};

export const obtenerPeriodosCerrados = (): PeriodoCerrado[] => {
  return readArray<PeriodoCerrado>(PERIODOS_CERRADOS_STORAGE_KEY);
};

export const obtenerAuditoriaCierres = (): AuditoriaCierreEvent[] => {
  return readArray<AuditoriaCierreEvent>(AUDITORIA_CIERRES_STORAGE_KEY);
};

export const estaPeriodoCerrado = (periodo: string): boolean => {
  return obtenerPeriodosCerrados().some((item) => item.periodo === periodo);
};

export const estaFechaBloqueada = (fecha: string): boolean => {
  const periodo = periodoFromFecha(fecha);
  if (periodo === null) {
    return false;
  }
  return estaPeriodoCerrado(periodo);
};

export const obtenerEstadoPeriodo = (periodo: string): EstadoPeriodo => {
  return estaPeriodoCerrado(periodo) ? "CERRADO" : "ABIERTO";
};

export const obtenerPeriodosConEstado = (): PeriodoConEstado[] => {
  return obtenerPeriodosCerrados().map((periodo) => ({
    periodo: periodo.periodo,
    estado: "CERRADO",
    usuarioCierre: periodo.usuarioCierre,
    fechaHoraCierre: periodo.fechaHoraCierre
  }));
};

export const cerrarPeriodo = (input: CerrarPeriodoInput): CierreResult => {
  if (!input.roles.includes("ADMINISTRADOR")) {
    registrarAuditoria(input.usuario, input.periodo, "RECHAZADO", "Usuario sin permisos para ejecutar cierre");
    return {
      ok: false,
      code: "CIERRE-001",
      message: "No tienes permisos para ejecutar cierre mensual"
    };
  }

  if (!isPeriodoValido(input.periodo)) {
    registrarAuditoria(input.usuario, input.periodo, "RECHAZADO", "Periodo invalido");
    return {
      ok: false,
      code: "CIERRE-002",
      message: "El periodo debe tener formato YYYYMM"
    };
  }

  if (!input.confirmado) {
    registrarAuditoria(input.usuario, input.periodo, "RECHAZADO", "Operacion cancelada por el usuario");
    return {
      ok: false,
      code: "CIERRE-003",
      message: "Operacion cancelada por el usuario"
    };
  }

  if (estaPeriodoCerrado(input.periodo)) {
    registrarAuditoria(input.usuario, input.periodo, "RECHAZADO", "Periodo ya se encuentra cerrado");
    return {
      ok: false,
      code: "CIERRE-004",
      message: "El periodo ya se encuentra cerrado"
    };
  }

  const periodos = obtenerPeriodosCerrados();
  const nuevoPeriodo: PeriodoCerrado = {
    periodo: input.periodo,
    usuarioCierre: input.usuario,
    fechaHoraCierre: new Date().toISOString()
  };

  writeArray(PERIODOS_CERRADOS_STORAGE_KEY, [...periodos, nuevoPeriodo]);
  registrarAuditoria(input.usuario, input.periodo, "EXITOSO");

  return {
    ok: true,
    message: `Cierre mensual ejecutado para el periodo ${input.periodo}`
  };
};

