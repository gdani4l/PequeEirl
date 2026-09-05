export interface ProductoModel {
    id_producto: number;
    precio_unitario: number;
    stock: number;
    descripcion: string;
    nombre: string;
    id_categoria: number;
    id_origen: number;
    id_presentacion: number;
    id_fecha_vencimiento: number;
}