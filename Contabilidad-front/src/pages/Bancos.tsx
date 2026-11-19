import { useEffect, useState } from "react";
import Layout from "../components/Layout";

import { Banco } from "../models/Banco";

import {
  BancoAuditEvent,
  cambiarEstadoBanco,
  crearBanco as crearBancoParametrizado,
  inicializarBancos,
  obtenerAuditoriaBancos,
  obtenerBancos,
  obtenerCatalogoNoParametrizado
} from "../services/bancoService";
import { getSession } from "../services/storageService";

export default function Bancos() {

  const [bancos, setBancos] =
    useState<Banco[]>([]);

  const [nombre, setNombre] = useState("");

  const [cuentaBancaria,
    setCuentaBancaria] =
    useState("");

  const [auditoria, setAuditoria] = useState<BancoAuditEvent[]>([]);

  const [filtroEstado, setFiltroEstado] = useState<"TODOS" | "ACTIVO" | "INACTIVO">("TODOS");

  const session = getSession();
  const usuarioSesion = session?.usuario ?? "SISTEMA";
  const puedeAdministrar = session?.roles.includes("ADMINISTRADOR") ?? false;

  useEffect(() => {

    inicializarBancos();

    cargar();

  }, []);

  const cargar = () => {

    setBancos(
      obtenerBancos()
    );

    setAuditoria(obtenerAuditoriaBancos());

  };

  const crearNuevoBanco = () => {

    if (!puedeAdministrar) {
      alert("No tienes permisos para administrar bancos");
      return;
    }

    if (!nombre.trim()) {

      alert(
        "Seleccione un banco del catalogo"
      );

      return;

    }

    const resultado = crearBancoParametrizado({
      nombre,
      cuentaBancaria,
      usuario: usuarioSesion
    });

    if (!resultado.ok) {
      alert(resultado.message);
      return;
    }

    setBancos(resultado.bancos);
    setAuditoria(obtenerAuditoriaBancos());

    setNombre("");

    setCuentaBancaria("");

  };

  const cambiarEstado =
    (id:number) => {

      if (!puedeAdministrar) {
        alert("No tienes permisos para cambiar el estado de bancos");
        return;
      }

      const resultado = cambiarEstadoBanco({
        id,
        usuario: usuarioSesion
      });

      if (!resultado.ok) {
        alert(resultado.message);
        return;
      }

      setBancos(resultado.bancos);
      setAuditoria(obtenerAuditoriaBancos());

    };

  const bancosDisponibles = obtenerCatalogoNoParametrizado();

  const bancosFiltrados = bancos.filter((banco) => {
    if (filtroEstado === "TODOS") {
      return true;
    }

    return banco.estado === filtroEstado;
  });

  return (

    <Layout>

      <h1>
        Administración de Bancos
      </h1>

      {!puedeAdministrar && (
        <p className="nota-permisos">
          Consulta habilitada. Solo el rol ADMINISTRADOR puede crear, activar o inactivar bancos.
        </p>
      )}

      <div
        className="formulario-banco"
      >

        <select
          value={nombre}
          onChange={(e)=>
            setNombre(
              e.target.value
            )
          }
          disabled={!puedeAdministrar}
        >
          <option value="">
            Seleccione banco del catalogo
          </option>

          {bancosDisponibles.map((bancoCatalogo) => (
            <option key={bancoCatalogo.nombre} value={bancoCatalogo.nombre}>
              {bancoCatalogo.nombre} ({bancoCatalogo.tipoConciliacion})
            </option>
          ))}
        </select>

        <input
          placeholder="Cuenta Bancaria (opcional)"
          value={cuentaBancaria}
          onChange={(e)=>
            setCuentaBancaria(
              e.target.value
            )
          }
          disabled={!puedeAdministrar}
        />

        <button
          onClick={
            crearNuevoBanco
          }
          disabled={!puedeAdministrar}
        >
          Crear Banco
        </button>

        <select
          value={filtroEstado}
          onChange={(e) =>
            setFiltroEstado(
              e.target.value as "TODOS" | "ACTIVO" | "INACTIVO"
            )
          }
        >
          <option value="TODOS">Todos</option>
          <option value="ACTIVO">Activos</option>
          <option value="INACTIVO">Inactivos</option>
        </select>

      </div>

      <table
        className="tabla-bancos"
      >

        <thead>

          <tr>

            <th>ID</th>

            <th>Banco</th>

            <th>Tipo</th>

            <th>Cuenta</th>

            <th>Estado</th>

            <th>Acciones</th>

          </tr>

        </thead>

        <tbody>

          {bancosFiltrados.map(
            banco => (

            <tr
              key={
                banco.id
              }
            >

              <td>
                {banco.id}
              </td>

              <td>
                {banco.nombre}
              </td>

              <td>
                {
                  banco.tipoConciliacion
                }
              </td>

              <td>
                {
                  banco.cuentaBancaria
                }
              </td>

              <td>
                {banco.estado}
              </td>

              <td>

                <button
                  onClick={() =>
                    cambiarEstado(
                      banco.id
                    )
                  }
                  disabled={!puedeAdministrar}
                >

                  {
                    banco.estado
                    === "ACTIVO"

                    ? "Inactivar"

                    : "Activar"
                  }

                </button>

              </td>

            </tr>

          ))}

          {bancosFiltrados.length === 0 && (
            <tr>
              <td colSpan={6}>No hay bancos para el filtro seleccionado.</td>
            </tr>
          )}

        </tbody>

      </table>

      <h2>Histórico de cambios</h2>

      <table className="tabla-bancos">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Banco</th>
            <th>Acción</th>
            <th>Usuario</th>
            <th>Detalle</th>
          </tr>
        </thead>
        <tbody>
          {auditoria.map((evento) => (
            <tr key={evento.id}>
              <td>{new Date(evento.fecha).toLocaleString()}</td>
              <td>{evento.bancoNombre}</td>
              <td>{evento.accion}</td>
              <td>{evento.usuario}</td>
              <td>{evento.detalle}</td>
            </tr>
          ))}

          {auditoria.length === 0 && (
            <tr>
              <td colSpan={5}>No hay eventos registrados.</td>
            </tr>
          )}
        </tbody>
      </table>

    </Layout>

  );

}