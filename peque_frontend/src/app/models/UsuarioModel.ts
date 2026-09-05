export interface UsuarioModel {
    id_usuario: number;
    id_tipo_documento: number;
    id_rol: number;
    nombre: string;
    segundo_nombre: string;
    apellido_pat: string;
    apellido_mat: string;
    correo: string;
    password: string;
    fecha_registro: string;
    activo: boolean;
    email_verified: boolean;
    email_verified_at: string | null;
    numero_documento: string;
}