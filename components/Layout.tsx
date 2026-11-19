import { ReactNode } from "react";
import Sidebar from "./Sidebar";
import { getSession } from "../services/storageService";
import logoTabAvVillas from "../assets/icons/logo-tab-av-villas.svg";

interface Props {
  children: ReactNode;
}

export default function Layout({
  children
}: Props) {
  const session = getSession();

  return (
    <div className="dashboard">

      <Sidebar />

      <main className="content">

        <header className="content-header">
          <div className="content-header-left">
            <img src={logoTabAvVillas} alt="AV Villas" className="content-header-logo" />
            <div>
              <p className="content-title">Sistema de Conciliacion</p>
            </div>
          </div>

          <div className="content-header-user">
            <p>{session?.nombre ?? ""}</p>
          </div>
        </header>

        {children}

      </main>

    </div>
  );

}