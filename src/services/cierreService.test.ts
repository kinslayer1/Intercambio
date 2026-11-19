import { inicializarBancos, obtenerBancosActivos } from "./bancoService";
import { procesarCargaInformacion } from "./cargaService";
import { aplicarCruce, obtenerContextoConciliacion } from "./conciliacionService";
import { cerrarPeriodo, estaFechaBloqueada, obtenerPeriodosCerrados } from "./cierreService";

const SAP_BASE = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-15,Detalle SAP,REF-A,C,100"
].join("\n");

const EXTRACTO_BASE = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-15,Detalle EXT,EXT-A,D,100"
].join("\n");

describe("RF-005 cierreService", () => {
  beforeEach(() => {
    localStorage.clear();
    inicializarBancos();
  });

  it("cierra periodo con usuario administrador y persiste estado", () => {
    const resultado = cerrarPeriodo({
      periodo: "202610",
      usuario: "1031803384",
      roles: ["ADMINISTRADOR"],
      confirmado: true
    });

    expect(resultado.ok).toBe(true);
    expect(estaFechaBloqueada("2026-10-01")).toBe(true);

    const periodos = obtenerPeriodosCerrados();
    expect(periodos.some((item) => item.periodo === "202610")).toBe(true);
  });

  it("rechaza cierre para usuario sin permisos", () => {
    const resultado = cerrarPeriodo({
      periodo: "202610",
      usuario: "1020304050",
      roles: ["ANALISTA"],
      confirmado: true
    });

    expect(resultado.ok).toBe(false);

    if (resultado.ok) {
      throw new Error("Se esperaba rechazo por permisos");
    }

    expect(resultado.code).toBe("CIERRE-001");
  });

  it("bloquea nuevas conciliaciones en periodo cerrado", () => {
    const banco = obtenerBancosActivos()[0];

    const carga = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-15",
      saldoUsd: 900,
      sapFileName: "sap.csv",
      sapContent: SAP_BASE,
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_BASE,
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    if (!carga.ok) {
      throw new Error("No se pudo preparar la data para validacion de cierre");
    }

    const cierre = cerrarPeriodo({
      periodo: "202610",
      usuario: "1031803384",
      roles: ["ADMINISTRADOR"],
      confirmado: true
    });

    if (!cierre.ok) {
      throw new Error("No se pudo cerrar el periodo de prueba");
    }

    const contexto = obtenerContextoConciliacion(banco.id, "2026-10-15");
    expect(contexto.ok).toBe(false);

    const intentoAplicar = aplicarCruce({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-15",
      extractoIds: [1],
      auxiliarIds: [1],
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    expect(intentoAplicar.ok).toBe(false);

    const recargaBloqueada = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-20",
      saldoUsd: 901,
      sapFileName: "sap.csv",
      sapContent: SAP_BASE,
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_BASE,
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    expect(recargaBloqueada.ok).toBe(false);
  });
});
