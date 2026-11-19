import Layout from "../components/Layout";

import logoAvVillas from "../assets/logos/logo_aval.svg";
import logoAval from "../assets/logos/logo_aval_1.svg";

export default function Home() {

  return (

    <Layout>

      <div className="home-hero">

        <div className="home-hero-content">

          <h1>
            Sistema de Conciliación Interna
          </h1>

          <p>
            Plataforma para la administración de bancos,
            carga de información, conciliación interna
            y gestión de reportes operativos.
          </p>

        </div>

      </div>

      <div className="modules-grid">

        <div className="module-card">

          <div className="module-icon">
            🏦
          </div>

          <h3>
            Bancos
          </h3>

          <p>
            Administración y parametrización de los
            bancos utilizados en los procesos de
            conciliación bancaria.
          </p>

        </div>

        <div className="module-card">

          <div className="module-icon">
            📤
          </div>

          <h3>
            Carga de Información
          </h3>

          <p>
            Carga de archivos SAP, extractos
            bancarios y registro de saldos
            contables.
          </p>

        </div>

        <div className="module-card">

          <div className="module-icon">
            ⚖️
          </div>

          <h3>
            Conciliación Manual
          </h3>

          <p>
            Cruce y validación manual de movimientos
            entre extractos bancarios y registros
            contables.
          </p>

        </div>

        <div className="module-card">

          <div className="module-icon">
            📄
          </div>

          <h3>
            Reportes
          </h3>

          <p>
            Consulta, seguimiento y exportación
            de resultados de conciliación.
          </p>

        </div>

      </div>

      <div className="home-footer">

        <div className="home-footer-left">

          <img src={logoAvVillas} alt="Logo Av Villas" className="home-footer-avvillas" />

        </div>

        <img src={logoAval} alt="Logo Aval" className="home-footer-aval" />

      </div>

    </Layout>

  );

}