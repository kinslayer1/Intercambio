import { DragEvent, useEffect, useMemo, useState } from "react";

import Layout from "../components/Layout";
import { getSession } from "../services/storageService";
import {
  aplicarCruce,
  exportarReportePreliminarTexto,
  obtenerAuditoriaConciliacion,
  obtenerBancosPendientesPorConciliar,
  obtenerConciliaciones,
  obtenerContextoConciliacion,
  validarCruce
} from "../services/conciliacionService";

type TabView = "MIXTO" | "SOLO_EXTRACTO" | "SOLO_AUXILIAR";
type Panel = "EXTRACTO" | "AUXILIAR";

export default function ConciliacionManual() {
  const session = getSession();

  const [fecha, setFecha] = useState("");
  const [bancoId, setBancoId] = useState("");
  const [tab, setTab] = useState<TabView>("MIXTO");

  const [extractoSeleccionado, setExtractoSeleccionado] = useState<number[]>([]);
  const [auxiliarSeleccionado, setAuxiliarSeleccionado] = useState<number[]>([]);

  const [mensaje, setMensaje] = useState("");
  const [refreshToken, setRefreshToken] = useState(0);

  const [validacionActual, setValidacionActual] = useState<{
    ok: boolean;
    totalExtracto: number;
    totalAuxiliar: number;
    diferencia: number;
    message: string;
  } | null>(null);

  const bancosConciliables = useMemo(() => {
    if (fecha.length === 0) {
      return [];
    }

    return obtenerBancosPendientesPorConciliar(fecha);
  }, [fecha, refreshToken]);

  const contextoConciliacion = useMemo(() => {
    if (bancoId.length === 0 || fecha.length === 0) {
      return null;
    }

    return obtenerContextoConciliacion(Number(bancoId), fecha);
  }, [bancoId, fecha, refreshToken]);

  const conciliacionesAplicadas = useMemo(() => {
    if (bancoId.length === 0 || fecha.length === 0) {
      return [];
    }

    return obtenerConciliaciones().filter(
      (item) => item.bancoId === Number(bancoId) && item.fechaConciliacion === fecha
    );
  }, [bancoId, fecha, refreshToken]);

  const auditoria = useMemo(() => {
    return obtenerAuditoriaConciliacion().slice(0, 8);
  }, [refreshToken]);

  useEffect(() => {
    setBancoId("");
    setExtractoSeleccionado([]);
    setAuxiliarSeleccionado([]);
    setValidacionActual(null);
    setMensaje("");
  }, [fecha]);

  const alternarSeleccion = (panel: Panel, id: number): void => {
    setValidacionActual(null);

    if (panel === "EXTRACTO") {
      setExtractoSeleccionado((prev) =>
        prev.includes(id) ? prev.filter((value) => value !== id) : [...prev, id]
      );
      return;
    }

    setAuxiliarSeleccionado((prev) =>
      prev.includes(id) ? prev.filter((value) => value !== id) : [...prev, id]
    );
  };

  const seleccionarParArrastre = (
    sourcePanel: Panel,
    sourceId: number,
    targetPanel: Panel,
    targetId: number
  ): void => {
    if (sourcePanel === targetPanel) {
      return;
    }

    setValidacionActual(null);

    if (sourcePanel === "EXTRACTO") {
      setExtractoSeleccionado((prev) => (prev.includes(sourceId) ? prev : [...prev, sourceId]));
      setAuxiliarSeleccionado((prev) => (prev.includes(targetId) ? prev : [...prev, targetId]));
      return;
    }

    setAuxiliarSeleccionado((prev) => (prev.includes(sourceId) ? prev : [...prev, sourceId]));
    setExtractoSeleccionado((prev) => (prev.includes(targetId) ? prev : [...prev, targetId]));
  };

  const onDragStartRow = (event: DragEvent<HTMLTableRowElement>, panel: Panel, id: number): void => {
    event.dataTransfer.setData("text/plain", JSON.stringify({ panel, id }));
  };

  const onDropRow = (
    event: DragEvent<HTMLTableRowElement>,
    targetPanel: Panel,
    targetId: number
  ): void => {
    event.preventDefault();

    try {
      const payload = JSON.parse(event.dataTransfer.getData("text/plain")) as {
        panel: Panel;
        id: number;
      };

      seleccionarParArrastre(payload.panel, payload.id, targetPanel, targetId);
    } catch {
      setMensaje("No se pudo interpretar la accion de arrastre");
    }
  };

  const aceptarCruce = (): void => {
    if (bancoId.length === 0 || fecha.length === 0) {
      setMensaje("Selecciona banco y fecha");
      return;
    }

    const validacion = validarCruce(
      Number(bancoId),
      fecha,
      extractoSeleccionado,
      auxiliarSeleccionado
    );

    setValidacionActual(validacion);
    setMensaje(validacion.message);
  };

  const aplicarCruceSeleccionado = (): void => {
    if (session === null) {
      setMensaje("Debes iniciar sesion");
      return;
    }

    if (validacionActual === null || !validacionActual.ok) {
      setMensaje("Debes validar un cruce sin diferencia antes de aplicar");
      return;
    }

    const response = aplicarCruce({
      bancoId: Number(bancoId),
      fechaConciliacion: fecha,
      extractoIds: extractoSeleccionado,
      auxiliarIds: auxiliarSeleccionado,
      usuario: session.usuario,
      roles: session.roles
    });

    if (!response.ok) {
      setMensaje(response.message);
      return;
    }

    setMensaje(response.message);
    setExtractoSeleccionado([]);
    setAuxiliarSeleccionado([]);
    setValidacionActual(null);
    setRefreshToken((value) => value + 1);
  };

  const cancelarCruce = (): void => {
    setExtractoSeleccionado([]);
    setAuxiliarSeleccionado([]);
    setValidacionActual(null);
    setMensaje("Cruce cancelado. No se persistieron cambios");
  };

  const exportarReporte = (): void => {
    if (contextoConciliacion === null || !contextoConciliacion.ok) {
      setMensaje("No hay reporte preliminar para exportar");
      return;
    }

    const reporteTexto = exportarReportePreliminarTexto(contextoConciliacion.data.reportePreliminar);

    const popup = window.open("", "_blank");

    if (popup === null) {
      setMensaje("No se pudo abrir la ventana para exportar");
      return;
    }

    popup.document.write(`<html><head><title>Reporte preliminar</title></head><body><pre>${reporteTexto}</pre></body></html>`);
    popup.document.close();
    popup.focus();
    popup.print();
  };

  const renderPanelTable = (
    panel: Panel,
    rows: Array<{ id: number; fecha: string; tipo: "D" | "C"; valor: number; cheque?: string; ajuste?: string }>
  ) => {
    const seleccion = panel === "EXTRACTO" ? extractoSeleccionado : auxiliarSeleccionado;

    return (
      <>
        <p>Mostrando {rows.length} registros</p>

        <div style={{ maxHeight: "420px", overflowY: "auto", border: "1px solid #ddd" }}>
          <table className="tabla-bancos">
            <thead>
              <tr>
                <th>Sel</th>
                <th>Fecha</th>
                <th>Tipo</th>
                <th>Valor</th>
                <th>Cheque</th>
                <th>Ajuste</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr
                  key={`${panel}-${row.id}`}
                  draggable
                  onDragStart={(event) => onDragStartRow(event, panel, row.id)}
                  onDragOver={(event) => event.preventDefault()}
                  onDrop={(event) => onDropRow(event, panel, row.id)}
                >
                  <td>
                    <input
                      type="checkbox"
                      checked={seleccion.includes(row.id)}
                      onChange={() => alternarSeleccion(panel, row.id)}
                    />
                  </td>
                  <td>{row.fecha}</td>
                  <td>{row.tipo}</td>
                  <td>{row.valor.toFixed(2)}</td>
                  <td>{row.cheque ?? ""}</td>
                  <td>{row.ajuste ?? ""}</td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={6}>Sin movimientos pendientes</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </>
    );
  };

  const renderReportePreliminar = () => {
    if (contextoConciliacion === null) {
      return null;
    }

    if (!contextoConciliacion.ok) {
      return <p>{contextoConciliacion.message}</p>;
    }

    const reporte = contextoConciliacion.data.reportePreliminar;

    return (
      <div>
        <h2>Reporte preliminar</h2>
        <p>
          Oficina: {reporte.oficina} | Banco: {reporte.banco} | Sucursal: {reporte.sucursal} | Fecha: {reporte.fecha}
        </p>
        <p>
          Cuenta Corriente: {reporte.cuentaCorriente} | Cuenta Auxiliar: {reporte.cuentaAuxiliar}
        </p>
        <p>Saldo segun libros: {reporte.saldoSegunLibros.toFixed(2)}</p>
        <p>
          Consignaciones pendientes de contabilizar: {reporte.consignacionesPendientesContabilizar.toFixed(2)}
        </p>
        <p>Cheques pendientes de contabilizar: {reporte.chequesPendientesContabilizar.toFixed(2)}</p>
        <p>Consignaciones pendientes de abonar: {reporte.consignacionesPendientesAbonar.toFixed(2)}</p>
        <p>Saldo segun extracto bancario: {reporte.saldoSegunExtractoBancario.toFixed(2)}</p>
        <button type="button" onClick={exportarReporte}>
          Exportar reporte preliminar a PDF
        </button>
      </div>
    );
  };

  const extractoPendiente =
    contextoConciliacion !== null && contextoConciliacion.ok ? contextoConciliacion.data.extractoPendiente : [];

  const auxiliarPendiente =
    contextoConciliacion !== null && contextoConciliacion.ok ? contextoConciliacion.data.auxiliarPendiente : [];

  const totalExtractoPendiente = extractoPendiente.reduce((acc, row) => acc + row.valor, 0);
  const totalAuxiliarPendiente = auxiliarPendiente.reduce((acc, row) => acc + row.valor, 0);

  const extractoDebitos = extractoPendiente.filter((row) => row.tipo === "D");
  const extractoCreditos = extractoPendiente.filter((row) => row.tipo === "C");

  const auxiliarDebitos = auxiliarPendiente.filter((row) => row.tipo === "D");
  const auxiliarCreditos = auxiliarPendiente.filter((row) => row.tipo === "C");

  return (
    <Layout>
      <h1>Conciliacion Manual</h1>

      <label>Fecha de conciliacion</label>
      <input type="date" value={fecha} onChange={(event) => setFecha(event.target.value)} />

      <br />
      <br />

      <label>Banco pendiente por conciliar</label>
      <select
        value={bancoId}
        onChange={(event) => {
          setBancoId(event.target.value);
          setExtractoSeleccionado([]);
          setAuxiliarSeleccionado([]);
          setValidacionActual(null);
          setMensaje("");
        }}
        disabled={fecha.length === 0}
      >
        <option value="">Seleccione banco</option>
        {bancosConciliables.map((banco) => (
          <option key={banco.id} value={banco.id}>
            {banco.nombre}
          </option>
        ))}
      </select>

      {fecha.length > 0 && bancosConciliables.length === 0 && (
        <p>No hay bancos con AUXILIAR, EXTRACTO y OFICINAS completos para la fecha seleccionada.</p>
      )}

      {renderReportePreliminar()}

      {contextoConciliacion !== null && contextoConciliacion.ok && (
        <>
          <h2>Pendientes para conciliar</h2>
          <p>
            Total Extracto: {totalExtractoPendiente.toFixed(2)} | Total Auxiliar: {totalAuxiliarPendiente.toFixed(2)}
          </p>

          <div className="formulario-banco">
            <button type="button" onClick={() => setTab("MIXTO")}>Tab 1: Extracto / Auxiliar</button>
            <button type="button" onClick={() => setTab("SOLO_EXTRACTO")}>Tab 2: Solo Extracto</button>
            <button type="button" onClick={() => setTab("SOLO_AUXILIAR")}>Tab 3: Solo Auxiliar</button>
          </div>

          {tab === "MIXTO" && (
            <div className="formulario-banco">
              <div>
                <h3>Panel Izquierdo - EXTRACTO</h3>
                {renderPanelTable("EXTRACTO", extractoPendiente)}
              </div>

              <div>
                <h3>Panel Derecho - AUXILIAR</h3>
                {renderPanelTable("AUXILIAR", auxiliarPendiente)}
              </div>
            </div>
          )}

          {tab === "SOLO_EXTRACTO" && (
            <div className="formulario-banco">
              <div>
                <h3>Debitos EXTRACTO</h3>
                {renderPanelTable("EXTRACTO", extractoDebitos)}
              </div>
              <div>
                <h3>Creditos EXTRACTO</h3>
                {renderPanelTable("EXTRACTO", extractoCreditos)}
              </div>
            </div>
          )}

          {tab === "SOLO_AUXILIAR" && (
            <div className="formulario-banco">
              <div>
                <h3>Debitos AUXILIAR</h3>
                {renderPanelTable("AUXILIAR", auxiliarDebitos)}
              </div>
              <div>
                <h3>Creditos AUXILIAR</h3>
                {renderPanelTable("AUXILIAR", auxiliarCreditos)}
              </div>
            </div>
          )}

          <h3>Validacion de Cruce</h3>
          <p>Extractos seleccionados: {extractoSeleccionado.length}</p>
          <p>Auxiliares seleccionados: {auxiliarSeleccionado.length}</p>
          <p>
            {validacionActual === null
              ? "Sin validacion"
              : `Total Debitos/Extracto: ${validacionActual.totalExtracto.toFixed(2)} | Total Creditos/Auxiliar: ${validacionActual.totalAuxiliar.toFixed(2)} | Diferencia: ${validacionActual.diferencia.toFixed(2)}`}
          </p>

          <div className="formulario-banco">
            <button type="button" onClick={aceptarCruce}>Aceptar</button>
            <button
              type="button"
              onClick={aplicarCruceSeleccionado}
              disabled={validacionActual === null || !validacionActual.ok}
            >
              Aplicar
            </button>
            <button type="button" onClick={cancelarCruce}>Cancelar</button>
            <button type="button" onClick={() => setRefreshToken((value) => value + 1)}>
              Recargar informacion
            </button>
          </div>

          <h3>Conciliaciones aplicadas</h3>
          <table className="tabla-bancos">
            <thead>
              <tr>
                <th>CRUZADO</th>
                <th>Periodo</th>
                <th>Total Extracto</th>
                <th>Total Auxiliar</th>
                <th>Registros</th>
                <th>Usuario</th>
                <th>Fecha Registro</th>
              </tr>
            </thead>
            <tbody>
              {conciliacionesAplicadas.map((item) => (
                <tr key={item.cruzado}>
                  <td>{item.cruzado}</td>
                  <td>{item.periodo}</td>
                  <td>{item.totalExtracto.toFixed(2)}</td>
                  <td>{item.totalAuxiliar.toFixed(2)}</td>
                  <td>{item.cantidadExtractos + item.cantidadAuxiliares}</td>
                  <td>{item.usuario}</td>
                  <td>{new Date(item.fechaRegistro).toLocaleString()}</td>
                </tr>
              ))}
              {conciliacionesAplicadas.length === 0 && (
                <tr>
                  <td colSpan={7}>No hay conciliaciones aplicadas para banco/fecha</td>
                </tr>
              )}
            </tbody>
          </table>

          <h3>Auditoria de conciliacion (ultimos 8)</h3>
          <table className="tabla-bancos">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Usuario</th>
                <th>Banco</th>
                <th>Fecha Conciliacion</th>
                <th>CRUZADO</th>
                <th>Registros</th>
                <th>Periodo</th>
              </tr>
            </thead>
            <tbody>
              {auditoria.map((evento) => (
                <tr key={evento.id}>
                  <td>{new Date(evento.fechaHora).toLocaleString()}</td>
                  <td>{evento.usuario}</td>
                  <td>{evento.bancoId}</td>
                  <td>{evento.fechaConciliacion}</td>
                  <td>{evento.cruzado}</td>
                  <td>{evento.cantidadRegistros}</td>
                  <td>{evento.periodo}</td>
                </tr>
              ))}
              {auditoria.length === 0 && (
                <tr>
                  <td colSpan={7}>No hay auditoria registrada</td>
                </tr>
              )}
            </tbody>
          </table>
        </>
      )}

      <p>{mensaje}</p>
    </Layout>
  );
}
