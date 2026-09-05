export interface MovimientosModel {
    id_movimiento: number;
    id_venta: number;
    id_estado_anterior: number;
    id_estado_nuevo: number;
    fecha_movimiento: string;
    id_usuario: number;
    observacion: string;
}