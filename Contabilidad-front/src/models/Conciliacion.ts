export interface Conciliacion {
  cruzado: number;
  periodo: string;
  bancoId: number;
  fechaConciliacion: string;

  extractos: number[];
  auxiliares: number[];

  totalExtracto: number;
  totalAuxiliar: number;
  cantidadExtractos: number;
  cantidadAuxiliares: number;

  usuario: string;

  fechaRegistro: string;
}