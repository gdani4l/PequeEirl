export interface NotaCreditoModel {
    id_nota_credito: number;
    serie: string;
    numero: number;
    fecha_emision: string;
    tipo: string;
    monto: number;
    motivo: string;
    id_venta_original: number;
    id_usuario: number;
    id_cliente: number;
}