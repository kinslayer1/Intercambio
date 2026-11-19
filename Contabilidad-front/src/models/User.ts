import type { Menu } from "./Menu";
import type { CorporateRole } from "./Rol";

export interface AuthenticatedUser {
	usuario: string;
	nombre: string;
	roles: CorporateRole[];
}

export interface UserSession extends AuthenticatedUser {
	menus: Menu[];
	allowedPaths: string[];
	permissionVersion: number;
	createdAt: number;
}
