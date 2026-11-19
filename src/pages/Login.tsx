import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { login } from "../services/authService";
import { saveSession } from "../services/storageService";

import logoAvVillas from "../assets/logos/LOGO AV VILLAS USO SOBRE FONDO BLANCO-03.png";
import logoAval from "../assets/logos/logo_aval.svg";

interface LocationState {
  authMessage?: string;
}

export default function Login() {

  const navigate = useNavigate();

  const location = useLocation();

  const state =
    location.state as LocationState | null;

  const [usuario, setUsuario] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const ingresar = async (): Promise<void> => {

    setLoading(true);

    setError("");

    const user =
      await login(
        usuario,
        password
      );

    setLoading(false);

    if (!user.success) {

      setError(
        user.message
      );

      return;

    }

    saveSession(
      user.session
    );

    navigate("/home");

  };

  return (

    <div className="login-page">

      <div className="login-panel">

        <div className="login-brand">

          <img
            src={logoAvVillas}
            alt="Logo Av Villas"
            className="login-logo"
          />

        </div>

        <h1 className="login-title">
          Sistema de Conciliación
        </h1>

        <p className="login-info-message">
          {state?.authMessage ?? ""}
        </p>

        <p className="login-subtitle">
          Ingrese sus credenciales corporativas
        </p>

        <input
          placeholder="Usuario"
          value={usuario}
          onChange={(e) =>
            setUsuario(
              e.target.value
            )
          }
        />

        <input
          type="password"
          placeholder="Contraseña"
          value={password}
          onChange={(e) =>
            setPassword(
              e.target.value
            )
          }
        />

        <button
          disabled={loading}
          onClick={ingresar}
        >
          {
            loading
              ? "Ingresando..."
              : "Ingresar"
          }
        </button>

        <p className="login-error-message">
          {error}
        </p>

        <div className="login-footer">

          <img
            src={logoAval}
            alt="Logo Aval"
            className="login-footer-logo"
          />

        </div>

      </div>

    </div>

  );

} 