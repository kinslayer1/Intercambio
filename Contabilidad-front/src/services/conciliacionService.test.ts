import { inicializarBancos, obtenerBancosActivos } from "./bancoService";
import { procesarCargaInformacion } from "./cargaService";
import {
  aplicarCruce,
  obtenerBancosPendientesPorConciliar,
  obtenerContextoConciliacion,
  validarCruce
} from "./conciliacionService";

const SAP_BASE = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-10,Detalle SAP,REF-A,C,100",
  "2026-10-10,Detalle SAP,REF-B,D,80"
].join("\n");

const EXTRACTO_BASE = [
  "fecha,concepto,referencia,tipo,valor",
  "2026-10-10,Detalle EXT,EXT-A,D,100",
  "2026-10-10,Detalle EXT,EXT-B,C,80"
].join("\n");

describe("RF-003 conciliacionService", () => {
  beforeEach(() => {
    localStorage.clear();
    inicializarBancos();

    const banco = obtenerBancosActivos()[0];

    const carga = procesarCargaInformacion({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-10",
      saldoUsd: 500,
      sapFileName: "sap.csv",
      sapContent: SAP_BASE,
      extractoFileName: "extracto.csv",
      extractoContent: EXTRACTO_BASE,
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    if (!carga.ok) {
      throw new Error("No se pudo preparar la data para RF-003");
    }
  });

  it("solo muestra bancos con informacion completa", () => {
    const conciliables = obtenerBancosPendientesPorConciliar("2026-10-10");

    expect(conciliables.length).toBeGreaterThan(0);
    expect(conciliables[0].estado).toBe("ACTIVO");
  });

  it("bloquea cruces con diferencia distinta de cero", () => {
    const banco = obtenerBancosActivos()[0];

    const contexto = obtenerContextoConciliacion(banco.id, "2026-10-10");

    if (!contexto.ok) {
      throw new Error("Se esperaba contexto valido");
    }

    const extracto = contexto.data.extractoPendiente.find((row) => row.valor === 100);
    const auxiliar = contexto.data.auxiliarPendiente.find((row) => row.valor === 80);

    if (extracto === undefined || auxiliar === undefined) {
      throw new Error("No se encontraron registros de prueba");
    }

    const validacion = validarCruce(banco.id, "2026-10-10", [extracto.id], [auxiliar.id]);

    expect(validacion.ok).toBe(false);
    expect(validacion.diferencia).not.toBe(0);
  });

  it("aplica cruce valido y remueve partidas de pendientes", () => {
    const banco = obtenerBancosActivos()[0];

    const contexto = obtenerContextoConciliacion(banco.id, "2026-10-10");

    if (!contexto.ok) {
      throw new Error("Se esperaba contexto valido");
    }

    const extracto = contexto.data.extractoPendiente.find(
      (row) => row.tipo === "D" && row.valor === 100
    );
    const auxiliar = contexto.data.auxiliarPendiente.find(
      (row) => row.tipo === "C" && row.valor === 100
    );

    if (extracto === undefined || auxiliar === undefined) {
      throw new Error("No se encontraron registros validos para cruce");
    }

    const resultado = aplicarCruce({
      bancoId: banco.id,
      fechaConciliacion: "2026-10-10",
      extractoIds: [extracto.id],
      auxiliarIds: [auxiliar.id],
      usuario: "1020304050",
      roles: ["ANALISTA"]
    });

    expect(resultado.ok).toBe(true);

    const recargado = obtenerContextoConciliacion(banco.id, "2026-10-10");

    if (!recargado.ok) {
      throw new Error("Se esperaba contexto recargado valido");
    }

    expect(recargado.data.extractoPendiente.some((row) => row.id === extracto.id)).toBe(false);
    expect(recargado.data.auxiliarPendiente.some((row) => row.id === auxiliar.id)).toBe(false);
  });
});
