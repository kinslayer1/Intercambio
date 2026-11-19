import { useMemo, useState } from "react";

import Layout from "../components/Layout";
import { getSession } from "../services/storageService";
import {
  cerrarPeriodo,
  obtenerAuditoriaCierres,
  obtenerPeriodosConEstado
} from "../services/cierreService";

const monthToPeriodo = (value: string): string => value.replace("-", "");

export default function Cierre() {
  const session = getSession();

  const [mes, setMes] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [refreshToken, setRefreshToken] = useState(0);

  const puedeEjecutarCierre = session?.roles.includes("ADMINISTRADOR") ?? false;

  const periodos = useMemo(() => obtenerPeriodosConEstado(), [refreshToken]);
  const auditoria = useMemo(() => obtenerAuditoriaCierres().slice(0, 10), [refreshToken]);

  const ejecutarCierre = (): void => {
    if (session === null) {
      setMensaje("Debes iniciar sesion");
      return;
    }

    if (mes.length === 0) {
      setMensaje("Debes seleccionar el periodo a cerrar");
      return;
    }

    const confirmado = window.confirm(
      "Esta operacion cerrara el periodo y bloqueara nuevas conciliaciones y modificaciones. Deseas continuar?"
    );

    const resultado = cerrarPeriodo({
      periodo: monthToPeriodo(mes),
      usuario: session.usuario,
      roles: session.roles,
      confirmado
    });

    setMensaje(resultado.message);

    if (resultado.ok) {
      setRefreshToken((value) => value + 1);
    }
  };

  const cancelar = (): void => {
    setMes("");
    setMensaje("Operacion cancelada. No se ejecuto ningun cierre");
  };

  return (
    <Layout>
      <h1>Cierre Mensual</h1>

      {!puedeEjecutarCierre && (
        <p className="nota-permisos">
          Consulta habilitada. Solo el rol ADMINISTRADOR puede ejecutar cierres mensuales.
        </p>
      )}

      <div className="formulario-banco">
        <input
          type="month"
          value={mes}
          onChange={(event) => setMes(event.target.value)}
          disabled={!puedeEjecutarCierre}
        />

        <button type="button" onClick={ejecutarCierre} disabled={!puedeEjecutarCierre}>
          Aceptar
        </button>

        <button type="button" onClick={cancelar}>
          Cancelar
        </button>
      </div>

      <p>{mensaje}</p>

      <h2>Estado de periodos</h2>
      <table className="tabla-bancos">
        <thead>
          <tr>
            <th>Periodo</th>
            <th>Estado</th>
            <th>Usuario Cierre</th>
            <th>Fecha Cierre</th>
          </tr>
        </thead>
        <tbody>
          {periodos.map((item) => (
            <tr key={item.periodo}>
              <td>{item.periodo}</td>
              <td>{item.estado}</td>
              <td>{item.usuarioCierre ?? ""}</td>
              <td>{item.fechaHoraCierre ? new Date(item.fechaHoraCierre).toLocaleString() : ""}</td>
            </tr>
          ))}

          {periodos.length === 0 && (
            <tr>
              <td colSpan={4}>No existen periodos registrados para consulta.</td>
            </tr>
          )}
        </tbody>
      </table>

      <h2>Auditoria de cierres (ultimos 10)</h2>
      <table className="tabla-bancos">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Usuario</th>
            <th>Operacion</th>
            <th>Periodo</th>
            <th>Resultado</th>
            <th>Motivo</th>
          </tr>
        </thead>
        <tbody>
          {auditoria.map((item) => (
            <tr key={item.id}>
              <td>{new Date(item.fechaHora).toLocaleString()}</td>
              <td>{item.usuario}</td>
              <td>{item.operacion}</td>
              <td>{item.periodo}</td>
              <td>{item.resultado}</td>
              <td>{item.motivo ?? ""}</td>
            </tr>
          ))}

          {auditoria.length === 0 && (
            <tr>
              <td colSpan={6}>No hay eventos de cierre registrados.</td>
            </tr>
          )}
        </tbody>
      </table>
    </Layout>
  );
}
