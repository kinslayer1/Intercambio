import { Banco } from "../models/Banco";

const STORAGE_KEY = "bancos";
const BANK_AUDIT_STORAGE_KEY = "bancos.auditoria";

type CatalogoBanco = {
  nombre: string;
  tipoConciliacion: Banco["tipoConciliacion"];
};

export interface BancoAuditEvent {
  id: number;
  bancoId: number;
  bancoNombre: string;
  accion: "CREADO" | "ACTIVADO" | "INACTIVADO";
  usuario: string;
  fecha: string;
  detalle: string;
}

interface CrearBancoInput {
  nombre: string;
  cuentaBancaria: string;
  usuario: string;
}

interface CambiarEstadoBancoInput {
  id: number;
  usuario: string;
}

interface OperationSuccess {
  ok: true;
  bancos: Banco[];
}

interface OperationFailure {
  ok: false;
  code: "BANCO_NO_PARAMETRIZABLE" | "BANCO_DUPLICADO" | "BANCO_NO_ENCONTRADO";
  message: string;
}

export type BancoOperationResult = OperationSuccess | OperationFailure;

const CATALOGO_BANCOS: CatalogoBanco[] = [
  { nombre: "Banco Republica", tipoConciliacion: "DIARIO" },
  { nombre: "CitiBank", tipoConciliacion: "DIARIO" },
  { nombre: "Bank of America", tipoConciliacion: "DIARIO" },
  { nombre: "Banco Popular Nacional", tipoConciliacion: "MENSUAL" },
  { nombre: "Banco Popular Exenta", tipoConciliacion: "MENSUAL" },
  { nombre: "Banco Occidente", tipoConciliacion: "MENSUAL" },
  { nombre: "Nexus", tipoConciliacion: "MENSUAL" },
  { nombre: "Pactia", tipoConciliacion: "MENSUAL" },
  { nombre: "AV Villas Comfecamaras", tipoConciliacion: "MENSUAL" },
  { nombre: "AV Villas Impuestos", tipoConciliacion: "MENSUAL" },
  { nombre: "AV Villas PILA", tipoConciliacion: "MENSUAL" },
  { nombre: "AV Villas Seguridad Social", tipoConciliacion: "MENSUAL" },
  { nombre: "AV Villas SNR", tipoConciliacion: "MENSUAL" }
];

const normalizeBankName = (value: string): string => value.trim().toUpperCase();

const seedBancos = (): Banco[] => {
  return [
    {
      id: 1,
      nombre: "Banco Republica",
      tipoConciliacion: "DIARIO",
      cuentaBancaria: "100001",
      estado: "ACTIVO",
      fechaCreacion: new Date().toISOString(),
      usuarioCreacion: "ADMIN"
    },

    {
      id: 2,
      nombre: "CitiBank",
      tipoConciliacion: "DIARIO",
      cuentaBancaria: "100002",
      estado: "ACTIVO",
      fechaCreacion: new Date().toISOString(),
      usuarioCreacion: "ADMIN"
    },

    {
      id: 3,
      nombre: "Bank of America",
      tipoConciliacion: "DIARIO",
      cuentaBancaria: "100003",
      estado: "ACTIVO",
      fechaCreacion: new Date().toISOString(),
      usuarioCreacion: "ADMIN"
    }
  ];
};

const ensureStorageInitialized = (): void => {
  const existe = localStorage.getItem(STORAGE_KEY);

  if (existe !== null) {
    return;
  }

  guardarBancos(seedBancos());
};

const guardarAuditoria = (eventos: BancoAuditEvent[]): void => {
  localStorage.setItem(BANK_AUDIT_STORAGE_KEY, JSON.stringify(eventos));
};

const registrarAuditoria = (
  bancoId: number,
  bancoNombre: string,
  accion: BancoAuditEvent["accion"],
  usuario: string,
  detalle: string
): void => {
  const auditoriaActual = obtenerAuditoriaBancos();

  const evento: BancoAuditEvent = {
    id: Date.now(),
    bancoId,
    bancoNombre,
    accion,
    usuario,
    fecha: new Date().toISOString(),
    detalle
  };

  guardarAuditoria([evento, ...auditoriaActual]);
};

export const obtenerBancos = (): Banco[] => {
  ensureStorageInitialized();

  const bancos = localStorage.getItem(STORAGE_KEY);

  if (bancos === null) {
    return [];
  }

  try {
    return JSON.parse(bancos) as Banco[];
  } catch {
    guardarBancos(seedBancos());
    return seedBancos();
  }
};

export const obtenerBancosActivos = (): Banco[] => {
  return obtenerBancos().filter(
    banco => banco.estado === "ACTIVO"
  );
};

export const obtenerAuditoriaBancos = (): BancoAuditEvent[] => {
  const rawAudit = localStorage.getItem(BANK_AUDIT_STORAGE_KEY);

  if (rawAudit === null) {
    return [];
  }

  try {
    return JSON.parse(rawAudit) as BancoAuditEvent[];
  } catch {
    return [];
  }
};

export const obtenerCatalogoBancos = (): CatalogoBanco[] => {
  return CATALOGO_BANCOS;
};

export const obtenerCatalogoNoParametrizado = (): CatalogoBanco[] => {
  const bancosParametrizados = new Set(
    obtenerBancos().map((banco) => normalizeBankName(banco.nombre))
  );

  return CATALOGO_BANCOS.filter(
    (bancoCatalogo) => !bancosParametrizados.has(normalizeBankName(bancoCatalogo.nombre))
  );
};

export const guardarBancos = (
  bancos: Banco[]
): void => {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(bancos)
  );
};

export const crearBanco = (input: CrearBancoInput): BancoOperationResult => {
  ensureStorageInitialized();

  const nombreNormalizado = normalizeBankName(input.nombre);
  const bancoCatalogo = CATALOGO_BANCOS.find(
    (item) => normalizeBankName(item.nombre) === nombreNormalizado
  );

  if (bancoCatalogo === undefined) {
    return {
      ok: false,
      code: "BANCO_NO_PARAMETRIZABLE",
      message: "El banco seleccionado no pertenece al catalogo parametrizable"
    };
  }

  const bancosActuales = obtenerBancos();

  const existe = bancosActuales.some(
    (banco) => normalizeBankName(banco.nombre) === nombreNormalizado
  );

  if (existe) {
    return {
      ok: false,
      code: "BANCO_DUPLICADO",
      message: "Ya existe un banco con ese nombre"
    };
  }

  const nuevo: Banco = {
    id: Date.now(),
    nombre: bancoCatalogo.nombre,
    tipoConciliacion: bancoCatalogo.tipoConciliacion,
    cuentaBancaria: input.cuentaBancaria.trim(),
    estado: "ACTIVO",
    fechaCreacion: new Date().toISOString(),
    usuarioCreacion: input.usuario
  };

  const nuevos = [...bancosActuales, nuevo];
  guardarBancos(nuevos);

  registrarAuditoria(
    nuevo.id,
    nuevo.nombre,
    "CREADO",
    input.usuario,
    "Creacion de banco parametrizado"
  );

  return {
    ok: true,
    bancos: nuevos
  };
};

export const cambiarEstadoBanco = (input: CambiarEstadoBancoInput): BancoOperationResult => {
  ensureStorageInitialized();

  const bancosActuales = obtenerBancos();
  const bancoObjetivo = bancosActuales.find((banco) => banco.id === input.id);

  if (bancoObjetivo === undefined) {
    return {
      ok: false,
      code: "BANCO_NO_ENCONTRADO",
      message: "No se encontro el banco a actualizar"
    };
  }

  const nuevoEstado: Banco["estado"] = bancoObjetivo.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

  const nuevos = bancosActuales.map((banco) => {
    if (banco.id !== input.id) {
      return banco;
    }

    return {
      ...banco,
      estado: nuevoEstado
    };
  });

  guardarBancos(nuevos);

  registrarAuditoria(
    bancoObjetivo.id,
    bancoObjetivo.nombre,
    nuevoEstado === "ACTIVO" ? "ACTIVADO" : "INACTIVADO",
    input.usuario,
    nuevoEstado === "ACTIVO" ? "Banco habilitado para conciliacion" : "Banco deshabilitado para conciliacion"
  );

  return {
    ok: true,
    bancos: nuevos
  };
};

export const inicializarBancos = (): void => {
  ensureStorageInitialized();
};