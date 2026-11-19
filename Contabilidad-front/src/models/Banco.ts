export interface Banco {
  id: number;
  nombre: string;
  tipoConciliacion: "DIARIO" | "MENSUAL";
  cuentaBancaria: string;
  estado: "ACTIVO" | "INACTIVO";
  fechaCreacion: string;
  usuarioCreacion: string;
}