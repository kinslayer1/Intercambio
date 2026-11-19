import {
  Routes,
  Route
} from "react-router-dom";

import Login from "./pages/Login";
import Home from "./pages/Home";
import Bancos from "./pages/Bancos";
import Carga from "./pages/Carga";
import Conciliacion from "./pages/Conciliacion";
import Seguridad from "./pages/Seguridad";
import Usuarios from "./pages/Usuarios";
import Roles from "./pages/Roles";
import Cierre from "./pages/Cierre";
import AccessDenied from "./pages/AccessDenied";
import ProtectedRoute from "./components/ProtectedRoute";

export default function Router() {

  return (

    <Routes>

      <Route
        path="/"
        element={<Login />}
      />

      <Route
        path="/home"
        element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        }
      />

      <Route
        path="/bancos"
        element={
          <ProtectedRoute>
            <Bancos />
          </ProtectedRoute>
        }
      />

      <Route
        path="/carga"
        element={
          <ProtectedRoute>
            <Carga />
          </ProtectedRoute>
        }
      />

      <Route
        path="/conciliacion"
        element={
          <ProtectedRoute>
            <Conciliacion />
          </ProtectedRoute>
        }
      />

      <Route
        path="/seguridad"
        element={
          <ProtectedRoute>
            <Seguridad />
          </ProtectedRoute>
        }
      />

      <Route
        path="/usuarios"
        element={
          <ProtectedRoute>
            <Usuarios />
          </ProtectedRoute>
        }
      />

      <Route
        path="/roles"
        element={
          <ProtectedRoute>
            <Roles />
          </ProtectedRoute>
        }
      />

      <Route
        path="/cierre"
        element={
          <ProtectedRoute>
            <Cierre />
          </ProtectedRoute>
        }
      />

      <Route
        path="/access-denied"
        element={<AccessDenied />}
      />

    </Routes>

  );

}