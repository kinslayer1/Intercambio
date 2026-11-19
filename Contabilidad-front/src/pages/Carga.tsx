import { useMemo, useState } from "react";

import Layout from "../components/Layout";

import { obtenerBancosActivos } from "../services/bancoService";
import {
  obtenerAuditoriaCargas,
  procesarCargaInformacion,
  validarConciliacionHabilitada
} from "../services/cargaService";
import { getSession } from "../services/storageService";
import { normalizarArchivoCargue } from "../services/fileIngestionService";

export default function Carga() {
  const bancos = obtenerBancosActivos();

  const [bancoId, setBancoId] = useState("");
  const [fecha, setFecha] = useState("");
  const [saldo, setSaldo] = useState("");
  const [sapFile, setSapFile] = useState<File>();
  const [extractoFile, setExtractoFile] = useState<File>();

  const [isProcessing, setIsProcessing] = useState(false);
  const [resultado, setResultado] = useState("");

  const session = getSession();

  const auditoria = useMemo(() => obtenerAuditoriaCargas().slice(0, 10), [resultado]);

  const sinBancosDisponibles = bancos.length === 0;

  const estadoConciliacion = useMemo(() => {
    if (bancoId.length === 0 || fecha.length === 0) {
      return null;
    }

    return validarConciliacionHabilitada(Number(bancoId), fecha);
  }, [bancoId, fecha, resultado]);

  const procesarCarga = async (): Promise<void> => {
    if (session === null) {
      setResultado("Debes iniciar sesion para procesar la carga");
      return;
    }

    if (bancoId.length === 0) {
      setResultado("Debes seleccionar un banco");
      return;
    }

    if (fecha.length === 0) {
      setResultado("Debes seleccionar la fecha de conciliacion");
      return;
    }

    if (saldo.trim().length === 0) {
      setResultado("Debes registrar saldo contable en USD");
      return;
    }

    if (sapFile === undefined) {
      setResultado("Debes adjuntar el archivo CSV SAP");
      return;
    }

    if (extractoFile === undefined) {
      setResultado("Debes adjuntar el archivo CSV de extracto");
      return;
    }

    setIsProcessing(true);

    try {
      const bancoSeleccionado = bancos.find((item) => item.id === Number(bancoId));

      if (bancoSeleccionado === undefined) {
        setResultado("No se encontro el banco seleccionado");
        return;
      }

      const [sapContent, extractoContent] = await Promise.all([
        normalizarArchivoCargue(sapFile, "SAP", bancoSeleccionado.nombre),
        normalizarArchivoCargue(extractoFile, "EXTRACTO", bancoSeleccionado.nombre)
      ]);

      const response = procesarCargaInformacion({
        bancoId: Number(bancoId),
        fechaConciliacion: fecha,
        saldoUsd: Number(saldo),
        sapFileName: sapFile.name,
        sapContent,
        extractoFileName: extractoFile.name,
        extractoContent,
        usuario: session.usuario,
        roles: session.roles
      });

      if (!response.ok) {
        const issuesText = response.issues
          ?.slice(0, 5)
          .map((issue) => `Linea ${issue.linea} - ${issue.campo}: ${issue.mensaje}`)
          .join(" | ");

        setResultado(issuesText ? `${response.message}. ${issuesText}` : response.message);
        return;
      }

      setResultado(
        [
          response.message,
          `SAP cargados: ${response.resumenSap.cargados}/${response.resumenSap.totalEntrada}`,
          `SAP descartados valor cero: ${response.resumenSap.descartadosValorCero}`,
          `SAP descartados contrapartida cero: ${response.resumenSap.descartadosContrapartidaCero}`,
          `Extracto cargados: ${response.resumenExtracto.cargados}/${response.resumenExtracto.totalEntrada}`,
          `Extracto descartados valor cero: ${response.resumenExtracto.descartadosValorCero}`,
          `Extracto descartados contrapartida cero: ${response.resumenExtracto.descartadosContrapartidaCero}`,
          response.conciliacionHabilitada
            ? "Conciliacion habilitada para banco y fecha seleccionados"
            : "Conciliacion aun no habilitada"
        ].join(". ")
      );
    } catch (error) {
      setResultado(
        error instanceof Error
          ? error.message
          : "No fue posible leer y normalizar los archivos de entrada"
      );
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <Layout>
      <h1>Carga de Informacion</h1>

      <select value={bancoId} onChange={(e) => setBancoId(e.target.value)} disabled={sinBancosDisponibles}>
        <option value="">Seleccione Banco</option>

        {bancos.map((banco) => (
          <option key={banco.id} value={banco.id}>
            {banco.nombre}
          </option>
        ))}
      </select>

      {sinBancosDisponibles && (
        <p>No hay bancos activos parametrizados. Solicita a un administrador activar o crear un banco.</p>
      )}

      <br />
      <br />

      <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />

      <br />
      <br />

      <input
        type="file"
        accept=".csv,.xls,.xlsx,text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        onChange={(e) => setSapFile(e.target.files?.[0])}
      />

      <br />

      <label>SAP (CSV/XLS/XLSX)</label>

      <br />
      <br />

      <input
        type="file"
        accept=".csv,.xls,.xlsx,text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        onChange={(e) => setExtractoFile(e.target.files?.[0])}
      />

      <br />

      <label>Extracto (CSV/XLS/XLSX)</label>

      <br />
      <br />

      <input
        type="number"
        step="0.01"
        placeholder="Saldo USD"
        value={saldo}
        onChange={(e) => setSaldo(e.target.value)}
      />

      <br />
      <br />

      <button type="button" onClick={procesarCarga} disabled={sinBancosDisponibles || isProcessing}>
        {isProcessing ? "Procesando..." : "Procesar Carga"}
      </button>

      <p>{resultado}</p>

      {estadoConciliacion !== null && (
        <p>{estadoConciliacion.habilitada ? "Estado: HABILITADA" : `Estado: BLOQUEADA - ${estadoConciliacion.message}`}</p>
      )}

      <h2>Auditoria de Cargas (ultimas 10)</h2>

      <table className="tabla-bancos">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Tipo</th>
            <th>Usuario</th>
            <th>Banco</th>
            <th>Archivo</th>
            <th>Resultado</th>
            <th>Motivo</th>
          </tr>
        </thead>
        <tbody>
          {auditoria.map((evento) => (
            <tr key={evento.id}>
              <td>{new Date(evento.fechaHora).toLocaleString()}</td>
              <td>{evento.tipo}</td>
              <td>{evento.usuario}</td>
              <td>{evento.bancoId}</td>
              <td>{evento.nombreArchivo ?? "N/A"}</td>
              <td>{evento.resultado}</td>
              <td>{evento.motivo ?? ""}</td>
            </tr>
          ))}

          {auditoria.length === 0 && (
            <tr>
              <td colSpan={7}>No hay eventos de carga registrados.</td>
            </tr>
          )}
        </tbody>
      </table>
    </Layout>
  );
}
