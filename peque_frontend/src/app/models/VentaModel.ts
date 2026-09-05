export interface VentaModel {
    id_venta: number;
    serie: string;
    numero: number;
    fecha_anulacion: string;
    motivo_anulacion: string;
    total_pago: number;
    moneda: string;
    condicion_pago: string;
    valor_venta: number;
    igv: number;
    tasa_igv: number;
    isc: number;
    otros_tributos: number;
    fecha_venta: string;
    id_usuario: number;
    tipo_comprobante: string;
    id_cliente: number;
    id_estado_venta: number;
}