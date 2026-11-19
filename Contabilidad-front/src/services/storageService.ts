import type { UserSession } from "../models/User";

const SESSION_KEY = "auth.session";
const LAST_ACTIVITY_KEY = "auth.lastActivityAt";
const PERMISSIONS_VERSION_KEY = "auth.permissionsVersion";

const INACTIVITY_TIMEOUT_MS = 60 * 60 * 1000;

type SessionValidationResult =
	| "OK"
	| "UNAUTHENTICATED"
	| "EXPIRED"
	| "PERMISSIONS_UPDATED";

const safeReadNumber = (rawValue: string | null): number | null => {
	if (rawValue === null) {
		return null;
	}

	const parsedValue = Number(rawValue);
	return Number.isFinite(parsedValue) ? parsedValue : null;
};

export const getCurrentPermissionVersion = (): number => {
	const storedValue = safeReadNumber(localStorage.getItem(PERMISSIONS_VERSION_KEY));

	if (storedValue === null) {
		localStorage.setItem(PERMISSIONS_VERSION_KEY, "1");
		return 1;
	}

	return storedValue;
};

export const bumpPermissionVersion = (): number => {
	const newVersion = getCurrentPermissionVersion() + 1;
	localStorage.setItem(PERMISSIONS_VERSION_KEY, String(newVersion));
	return newVersion;
};

export const saveSession = (session: UserSession): void => {
	localStorage.setItem(SESSION_KEY, JSON.stringify(session));
	localStorage.setItem(LAST_ACTIVITY_KEY, String(Date.now()));
};

export const getSession = (): UserSession | null => {
	const rawSession = localStorage.getItem(SESSION_KEY);

	if (rawSession === null) {
		return null;
	}

	try {
		const parsedSession = JSON.parse(rawSession) as UserSession;
		return parsedSession;
	} catch {
		clearSession();
		return null;
	}
};

export const touchSession = (): void => {
	localStorage.setItem(LAST_ACTIVITY_KEY, String(Date.now()));
};

export const clearSession = (): void => {
	localStorage.removeItem(SESSION_KEY);
	localStorage.removeItem(LAST_ACTIVITY_KEY);
};

const isInactiveSessionExpired = (): boolean => {
	const lastActivityAt = safeReadNumber(localStorage.getItem(LAST_ACTIVITY_KEY));

	if (lastActivityAt === null) {
		return true;
	}

	return Date.now() - lastActivityAt > INACTIVITY_TIMEOUT_MS;
};

export const validateSession = (): SessionValidationResult => {
	const session = getSession();

	if (session === null) {
		return "UNAUTHENTICATED";
	}

	if (isInactiveSessionExpired()) {
		return "EXPIRED";
	}

	if (session.permissionVersion !== getCurrentPermissionVersion()) {
		return "PERMISSIONS_UPDATED";
	}

	return "OK";
};
