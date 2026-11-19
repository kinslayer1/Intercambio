import type { Menu } from "../models/Menu";
import type { CorporateRole } from "../models/Rol";
import type { AuthenticatedUser, UserSession } from "../models/User";
import { getCurrentPermissionVersion } from "./storageService";

type AuthErrorCode =
  | "AUTH-001"
  | "AUTH-002"
  | "AUTH-003"
  | "AUTH-004"
  | "AUTH-005"
  | "AUTH-006"
  | "AUTH-007"
  | "AUTH-008";

interface LoginSuccessResult {
  success: true;
  session: UserSession;
}

interface LoginFailureResult {
  success: false;
  code: AuthErrorCode;
  message: string;
}

export type LoginResult = LoginSuccessResult | LoginFailureResult;

interface CorporateUserRecord {
  password: string;
  nombre: string;
  blocked: boolean;
  roles: CorporateRole[];
}

const CORPORATE_USER_DIRECTORY: Record<string, CorporateUserRecord> = {
  "1031803384": {
    password: "TEST1234",
    nombre: "Javier Plata",
    blocked: false,
    roles: ["ADMINISTRADOR", "SEGURIDAD"]
  },
  "1020304050": {
    password: "TEST1234",
    nombre: "Maria Perez",
    blocked: false,
    roles: ["ANALISTA"]
  },
  "9988776655": {
    password: "TEST1234",
    nombre: "Usuario Bloqueado",
    blocked: true,
    roles: ["ANALISTA"]
  },
  "1234567890": {
    password: "TEST1234",
    nombre: "Usuario Sin Rol",
    blocked: false,
    roles: []
  }
};

const MENUS_BASE: Menu[] = [
  {
    code: "M_HOME",
    label: "Dashboard",
    path: "/home",
    active: true,
    submenus: []
  },
  {
    code: "M_OPERACION",
    label: "Operacion",
    active: true,
    submenus: [
      {
        code: "SM_BANCOS",
        label: "Bancos",
        path: "/bancos",
        active: true
      },
      {
        code: "SM_CARGA",
        label: "Carga",
        path: "/carga",
        active: true
      },
      {
        code: "SM_CONCILIACION",
        label: "Conciliacion",
        path: "/conciliacion",
        active: true
      },
      {
        code: "SM_CIERRE",
        label: "Cierre",
        path: "/cierre",
        active: true
      }
    ]
  },
  {
    code: "M_SEGURIDAD",
    label: "Seguridad",
    active: true,
    submenus: [
      {
        code: "SM_SEGURIDAD",
        label: "Permisos",
        path: "/seguridad",
        active: true
      },
      {
        code: "SM_USUARIOS",
        label: "Usuarios",
        path: "/usuarios",
        active: true
      },
      {
        code: "SM_ROLES",
        label: "Roles",
        path: "/roles",
        active: true
      }
    ]
  }
];

const ROLE_ALLOWED_SUBMENUS: Record<CorporateRole, string[]> = {
  ADMINISTRADOR: [
    "SM_BANCOS",
    "SM_CARGA",
    "SM_CONCILIACION",
    "SM_CIERRE",
    "SM_SEGURIDAD",
    "SM_USUARIOS",
    "SM_ROLES"
  ],
  SEGURIDAD: ["SM_SEGURIDAD", "SM_USUARIOS", "SM_ROLES"],
  ANALISTA: ["SM_BANCOS", "SM_CARGA", "SM_CONCILIACION"],
  APROBADOR: ["SM_CONCILIACION", "SM_CIERRE"]
};

const ERROR_MESSAGES: Record<AuthErrorCode, string> = {
  "AUTH-001": "Usuario invalido",
  "AUTH-002": "Contrasena invalida",
  "AUTH-003": "Usuario bloqueado",
  "AUTH-004": "Usuario sin roles autorizados",
  "AUTH-005": "Servicio AUTENTICAR no disponible",
  "AUTH-006": "Error tecnico de autenticacion",
  "AUTH-007": "Servicio AUTORIZAR no disponible",
  "AUTH-008": "Error tecnico de autorizacion"
};

const withMockDelay = async (): Promise<void> => {
  await new Promise<void>((resolve) => {
    window.setTimeout(() => resolve(), 250);
  });
};

const authenticateWithNovell = async (
  usuario: string,
  password: string
): Promise<{ success: true; usuarioAutenticado: string } | LoginFailureResult> => {
  await withMockDelay();

  const normalizedUser = usuario.trim();

  if (normalizedUser.length === 0) {
    return {
      success: false,
      code: "AUTH-001",
      message: ERROR_MESSAGES["AUTH-001"]
    };
  }

  const record = CORPORATE_USER_DIRECTORY[normalizedUser];

  if (record === undefined) {
    return {
      success: false,
      code: "AUTH-001",
      message: ERROR_MESSAGES["AUTH-001"]
    };
  }

  if (record.blocked) {
    return {
      success: false,
      code: "AUTH-003",
      message: ERROR_MESSAGES["AUTH-003"]
    };
  }

  if (record.password !== password) {
    return {
      success: false,
      code: "AUTH-002",
      message: ERROR_MESSAGES["AUTH-002"]
    };
  }

  return {
    success: true,
    usuarioAutenticado: normalizedUser
  };
};

const authorizeWithNovell = async (
  usuarioAutenticado: string
): Promise<{ success: true; user: AuthenticatedUser } | LoginFailureResult> => {
  await withMockDelay();

  const record = CORPORATE_USER_DIRECTORY[usuarioAutenticado];

  if (record === undefined) {
    return {
      success: false,
      code: "AUTH-008",
      message: ERROR_MESSAGES["AUTH-008"]
    };
  }

  if (record.roles.length === 0) {
    return {
      success: false,
      code: "AUTH-004",
      message: ERROR_MESSAGES["AUTH-004"]
    };
  }

  return {
    success: true,
    user: {
      usuario: usuarioAutenticado,
      nombre: record.nombre,
      roles: record.roles
    }
  };
};

const buildAuthorizedMenus = (roles: CorporateRole[]): Menu[] => {
  const allowedSubmenuCodes = new Set<string>();

  roles.forEach((role) => {
    ROLE_ALLOWED_SUBMENUS[role].forEach((submenuCode) => {
      allowedSubmenuCodes.add(submenuCode);
    });
  });

  return MENUS_BASE.map((menu) => {
    const authorizedSubmenus = menu.submenus.filter(
      (submenu) => submenu.active && allowedSubmenuCodes.has(submenu.code)
    );

    return {
      ...menu,
      submenus: authorizedSubmenus
    };
  }).filter((menu) => {
    if (!menu.active) {
      return false;
    }

    if (menu.path !== undefined && menu.path.length > 0) {
      return true;
    }

    return menu.submenus.length > 0;
  });
};

const resolveAllowedPaths = (menus: Menu[]): string[] => {
  const allowedPaths = new Set<string>();

  menus.forEach((menu) => {
    if (menu.path !== undefined && menu.path.length > 0) {
      allowedPaths.add(menu.path);
    }

    menu.submenus.forEach((submenu) => {
      allowedPaths.add(submenu.path);
    });
  });

  return Array.from(allowedPaths);
};

export const login = async (usuario: string, password: string): Promise<LoginResult> => {
  const authResult = await authenticateWithNovell(usuario, password);

  if (!authResult.success) {
    return authResult;
  }

  const authorizationResult = await authorizeWithNovell(authResult.usuarioAutenticado);

  if (!authorizationResult.success) {
    return authorizationResult;
  }

  const menus = buildAuthorizedMenus(authorizationResult.user.roles);
  const allowedPaths = resolveAllowedPaths(menus);

  const session: UserSession = {
    ...authorizationResult.user,
    menus,
    allowedPaths,
    permissionVersion: getCurrentPermissionVersion(),
    createdAt: Date.now()
  };

  return {
    success: true,
    session
  };
};

export const isRouteAllowed = (allowedPaths: string[], path: string): boolean => {
  if (path === "/") {
    return true;
  }

  return allowedPaths.includes(path);
};