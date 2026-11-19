import {
  cambiarEstadoBanco,
  crearBanco,
  inicializarBancos,
  obtenerBancos,
  obtenerBancosActivos,
  obtenerCatalogoNoParametrizado
} from "./bancoService";

describe("RF-001 bancoService", () => {
  beforeEach(() => {
    localStorage.clear();
    inicializarBancos();
  });

  it("debe exponer bancos parametrizados para seleccion", () => {
    const bancos = obtenerBancos();

    expect(bancos.length).toBeGreaterThan(0);
    expect(bancos.some((banco) => banco.nombre === "Banco Republica")).toBe(true);
  });

  it("no debe exponer bancos inactivos para conciliacion", () => {
    const bancoActivo = obtenerBancosActivos()[0];

    const resultado = cambiarEstadoBanco({
      id: bancoActivo.id,
      usuario: "TEST_ADMIN"
    });

    expect(resultado.ok).toBe(true);

    const activos = obtenerBancosActivos();
    expect(activos.some((banco) => banco.id === bancoActivo.id)).toBe(false);
  });

  it("debe permitir parametrizar un banco del catalogo oficial", () => {
    const pendiente = obtenerCatalogoNoParametrizado()[0];

    const resultado = crearBanco({
      nombre: pendiente.nombre,
      cuentaBancaria: "999000",
      usuario: "TEST_ADMIN"
    });

    expect(resultado.ok).toBe(true);

    const activos = obtenerBancosActivos();
    expect(activos.some((banco) => banco.nombre === pendiente.nombre)).toBe(true);
  });

  it("no debe permitir bancos fuera de parametrizacion", () => {
    const resultado = crearBanco({
      nombre: "Banco Inventado",
      cuentaBancaria: "123",
      usuario: "TEST_ADMIN"
    });

    expect(resultado.ok).toBe(false);

    if (resultado.ok) {
      throw new Error("Se esperaba error de validacion");
    }

    expect(resultado.code).toBe("BANCO_NO_PARAMETRIZABLE");
  });
});
