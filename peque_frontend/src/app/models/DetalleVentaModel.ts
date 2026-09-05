export interface DetalleVentaModel {
    id_detalle: number;
    cantidad: number;
    precio_fijo: number;
    id_venta: number;
    id_producto: number;
}