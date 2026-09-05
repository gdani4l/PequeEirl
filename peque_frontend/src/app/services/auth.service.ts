import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { appConfig } from '../config/appConfig';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private apiUrl = appConfig.apiUrl;

    constructor(private http: HttpClient) {}

    registro(data: any): Observable<any> {
        return this.http.post(`${this.apiUrl}api/auth/registro`, data);
    }

    login(correo: string, password: string): Observable<any> {
        return this.http.post(`${this.apiUrl}api/auth/login`, { correo, password });
    }

    verificarMfa(correo: string, codigo: string): Observable<any> {
        return this.http.post(`${this.apiUrl}api/auth/mfa`, { correo, codigo });
    }

    verifyEmail(token: string): Observable<any> {
        return this.http.get(`${this.apiUrl}api/auth/verify?token=${token}`);
    }

    getUsuario(id: number): Observable<any> {
        return this.http.get(`${this.apiUrl}api/usuarios/${id}`);
    }

    actualizarUsuario(id: number, data: any): Observable<any> {
        return this.http.put(`${this.apiUrl}api/usuarios/${id}`, data);
    }

    guardarSesion(response: any): void {
        localStorage.setItem('usuario', JSON.stringify(response));
        if (response.token) {
            localStorage.setItem('token', response.token);
        }
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    obtenerSesion(): any {
        const usuario = localStorage.getItem('usuario');
        return usuario ? JSON.parse(usuario) : null;
    }

    cerrarSesion(): void {
        localStorage.removeItem('usuario');
        localStorage.removeItem('token');
    }

    esAdmin(): boolean {
        const usuario = this.obtenerSesion();
        return usuario && usuario.rol === 'ADMIN';
    }

    esVendedor(): boolean {
        const usuario = this.obtenerSesion();
        return usuario && usuario.rol === 'VENDEDOR';
    }
}