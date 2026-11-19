import { inicializarBancos, obtenerBancosActivos } from "./bancoService";
import { procesarCargaInformacion, validarConciliacionHabilitada } from "./cargaService";

const SAP_VALIDO = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-01,Pago proveedor,REF-1,D,100",
  "2026-10-01,Pago proveedor,REF-2,C,25",
  "2026-10-01,Pago proveedor,REF-2,D,25",
  "2026-10-01,Pago proveedor,REF-3,D,0",
].join("\n");

const EXTRACTO_VALIDO = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-01,Abono,EXT-1,D,200",
  "2026-10-01,Abono,EXT-2,C,50",
  "2026-10-01,Abono,EXT-2,D,50",
  "2026-10-01,Abono,EXT-3,C,0",
].join("\n");

describe("RF-002 cargaService", () => {
  beforeEach(() => {
    localStorage.clear();
    inicializarBancos();
  });

  it("debe procesar carga valida y habilitar conciliacion", () => {
    const banco = obtenerBancosActivos()[0];

    const resultado = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-01",
      saldoUsd: 1500.25,
      sapFileName: "sap.csv",
      sapContent: SAP_VALIDO,
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_VALIDO,
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    expect(resultado.ok).toBe(true);

    if (!resultado.ok) {
      throw new Error("La carga valida debe ser exitosa");
    }

    expect(resultado.resumenSap.cargados).toBeGreaterThan(0);
    expect(resultado.resumenExtracto.cargados).toBeGreaterThan(0);

    const habilitacion = validarConciliacionHabilitada(banco.id, "2026-10-01");
    expect(habilitacion.habilitada).toBe(true);
  });

  it("debe rechazar archivo SAP con estructura invalida", () => {
    const banco = obtenerBancosActivos()[0];

    const resultado = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-02",
      saldoUsd: 10,
      sapFileName: "sap.csv",
      sapContent: "concepto,valor\nPago,100",
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_VALIDO,
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    expect(resultado.ok).toBe(false);

    if (resultado.ok) {
      throw new Error("Se esperaba rechazo por estructura invalida");
    }

    expect(resultado.code).toBe("CARGA-007");
  });

  it("debe rechazar carga para usuario sin rol analista", () => {
    const banco = obtenerBancosActivos()[0];

    const resultado = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-03",
      saldoUsd: 10,
      sapFileName: "sap.csv",
      sapContent: SAP_VALIDO,
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_VALIDO,
      usuario: "1031803384",
      roles: ["ADMINISTRADOR"]
    });

    expect(resultado.ok).toBe(false);

    if (resultado.ok) {
      throw new Error("Se esperaba rechazo por permisos");
    }

    expect(resultado.code).toBe("CARGA-001");
  });
});
