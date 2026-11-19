export interface MovimientoExtracto {
  id: number;

  bancoId: number;

  fechaConciliacion: string;

  fecha: string;

  concepto: string;

  referencia: string;

  tipo: "D" | "C";

  valor: number;
  cheque?: string;
  ajuste?: string;
}