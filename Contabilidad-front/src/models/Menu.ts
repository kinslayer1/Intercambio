export interface Submenu {
	code: string;
	label: string;
	path: string;
	active: boolean;
}

export interface Menu {
	code: string;
	label: string;
	path?: string;
	active: boolean;
	submenus: Submenu[];
}
