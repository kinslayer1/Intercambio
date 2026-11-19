import Layout from "../components/Layout";
import { useState } from "react";
import { bumpPermissionVersion } from "../services/storageService";

export default function Seguridad() {
  const [versionActualizada, setVersionActualizada] = useState<number | null>(null);

  const actualizarPermisos = (): void => {
    const nuevaVersion = bumpPermissionVersion();
    setVersionActualizada(nuevaVersion);
  };

  return (

    <Layout>

      <h1>
        Seguridad
      </h1>

      <p>
        Simulacion de cambio de permisos funcionales (RF-004).
      </p>

      <button type="button" onClick={actualizarPermisos}>
        Aplicar cambios de permisos
      </button>

      <p>
        {versionActualizada === null
          ? "Version actual sin cambios en esta sesion"
          : `Permisos actualizados a la version ${versionActualizada}. Las sesiones previas quedaran invalidas.`}
      </p>

    </Layout>

  );

}