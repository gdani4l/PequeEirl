import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConfirmModal } from '../../components/confirm-modal/confirm-modal';
import { AdminCuentas } from '../admin-cuentas/admin-cuentas';
import { Ventas } from '../ventas/ventas';
import { Dashboards } from '../dashboards/dashboards';
import { Movimientos } from '../movimientos/movimientos';
import { MisVentas } from '../mis-ventas/mis-ventas';

@Component({
    selector: 'app-inicio',
    templateUrl: './inicio.html',
    styleUrls: ['./inicio.css'],
    standalone: true,
    imports: [CommonModule, ConfirmModal, AdminCuentas, Ventas, Dashboards, Movimientos, MisVentas]
})
export class Inicio implements OnInit {
    @ViewChild('confirmModal') confirmModal!: ConfirmModal;
    
    usuario: any;
    rol: string = '';
    nombreCompleto: string = '';
    moduloSeleccionado: string = 'ventas';

    constructor(
        private authService: AuthService, 
        private router: Router
    ) {}

    ngOnInit(): void {
        this.usuario = this.authService.obtenerSesion();
        if (!this.usuario) {
            this.router.navigate(['/login']);
            return;
        }
        this.rol = this.usuario.rol;
        this.nombreCompleto = this.usuario.nombre + ' ' + this.usuario.apellidoPat;
    }

    seleccionarModulo(modulo: string): void {
        this.moduloSeleccionado = modulo;
    }

    abrirModalCerrarSesion(): void {
        this.confirmModal.abrir();
    }

    confirmarCerrarSesion(): void {
        this.authService.cerrarSesion();
        this.router.navigate(['/login']);
    }

    esAdmin(): boolean {
        return this.rol === 'ADMIN';
    }

    esVendedor(): boolean {
        return this.rol === 'VENDEDOR';
    }
}