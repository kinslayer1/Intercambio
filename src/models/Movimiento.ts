export interface Movimiento {

  id: number;

  bancoId: number;

  fecha: string;

  tipo: "D" | "C";

  valor: number;

  cheque?: string;

  ajuste?: string;

  conciliado: boolean;

  cruzado?: number;

}