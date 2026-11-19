import { Link } from "react-router-dom";
import { clearSession, getSession } from "../services/storageService";
import logoAvVillas from "../assets/logos/LOGO AV VILLAS USO FONDO ROJO.png";

export default function Sidebar() {
  const session = getSession();

  const salir = (): void => {
    clearSession();
    window.location.href = "/";
  };

  return (

    <aside className="sidebar">

      <div className="sidebar-brand">
        <img src={logoAvVillas} alt="Banco AV Villas" className="sidebar-logo" />
      </div>

      <div className="sidebar-user">
        <p>{session?.nombre ?? ""}</p>
        <small>{session?.roles.join(", ") ?? ""}</small>
      </div>

      {session?.menus.map((menu) => (
        <div key={menu.code} className="sidebar-menu-group">
          {menu.path !== undefined ? (
            <Link to={menu.path}>{menu.label}</Link>
          ) : (
            <p className="sidebar-group-title">{menu.label}</p>
          )}

          {menu.submenus.map((submenu) => (
            <Link key={submenu.code} to={submenu.path} className="sidebar-submenu-link">
              {submenu.label}
            </Link>
          ))}
        </div>
      ))}

      <button className="logout-btn" onClick={salir} type="button">
        Cerrar sesion
      </button>

    </aside>
  );

}