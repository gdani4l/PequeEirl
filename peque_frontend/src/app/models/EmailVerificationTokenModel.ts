export interface EmailVerificationTokenModel {
    id_token: number;
    id_usuario: number;
    token: string;
    expires_at: string;
    is_used: boolean;
    created_at: string;
}