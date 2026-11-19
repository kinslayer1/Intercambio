import type { ReactElement } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { isRouteAllowed } from "../services/authService";
import { clearSession, getSession, touchSession, validateSession } from "../services/storageService";

interface ProtectedRouteProps {
	children: ReactElement;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps): ReactElement => {
	const location = useLocation();
	const validationResult = validateSession();

	if (validationResult !== "OK") {
		clearSession();

		const authMessage =
			validationResult === "EXPIRED"
				? "La sesion expiro por inactividad"
				: validationResult === "PERMISSIONS_UPDATED"
					? "Tus permisos cambiaron. Debes ingresar nuevamente"
					: "Debes iniciar sesion";

		return <Navigate to="/" replace state={{ authMessage }} />;
	}

	const session = getSession();

	if (session === null) {
		return <Navigate to="/" replace state={{ authMessage: "Debes iniciar sesion" }} />;
	}

	const isAuthorizedPath = isRouteAllowed(session.allowedPaths, location.pathname);

	if (!isAuthorizedPath) {
		return <Navigate to="/access-denied" replace />;
	}

	touchSession();
	return children;
};

export default ProtectedRoute;
