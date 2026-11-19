import { Link } from "react-router-dom";

export default function AccessDenied() {

  return (

    <div>

      <h1>
        403
      </h1>

      <p>
        Acceso denegado
      </p>

      <Link to="/home">
        Volver al inicio
      </Link>

      <br />

      <Link to="/">
        Ir al login
      </Link>

    </div>

  );

}