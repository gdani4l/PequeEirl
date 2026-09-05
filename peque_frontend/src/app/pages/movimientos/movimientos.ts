import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';
import { appConfig } from '../../config/appConfig';

@Component({
    selector: 'app-movimientos',
    templateUrl: './movimientos.html',
    styleUrls: ['./movimientos.css'],
    standalone: true,
    imports: [CommonModule, FormsModule]
})
export class Movimientos implements OnInit {
    movimientos: any[] = [];
    movimientosFiltrados: any[] = [];
    cargando = false;
    procesando = false;

    filtro = '';
    filtroFecha = '';

    movimientoSeleccionado: any = null;
    mostrarModalAnular = false;
    motivoAnulacion = '';

    private apiUrl = appConfig.apiUrl;

    constructor(
        private authService: AuthService,
        private alertService: AlertService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.cargarMovimientos();
    }

    cargarMovimientos(): void {
        this.cargando = true;
        fetch(`${this.apiUrl}api/movimientos`, {
            headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (Array.isArray(data)) {
                    this.movimientos = data;
                } else if (data && Array.isArray(data.content)) {
                    this.movimientos = data.content;
                } else {
                    this.movimientos = [];
                }
                this.aplicarFiltros();
                this.cargando = false;
                this.cdr.detectChanges();
            })
            .catch(error => {
                this.cargando = false;
                this.alertService.error('Error al cargar los movimientos');
                this.cdr.detectChanges();
            });
    }

    aplicarFiltros(): void {
        let resultado = [...this.movimientos];

        if (this.filtro.trim()) {
            const term = this.filtro.toLowerCase();
            resultado = resultado.filter(m =>
                String(m.idVenta).includes(term) ||
                (m.usuarioResponsable || '').toLowerCase().includes(term) ||
                (m.observacion || '').toLowerCase().includes(term) ||
                (m.estadoNuevo || '').toLowerCase().includes(term)
            );
        }

        if (this.filtroFecha) {
            resultado = resultado.filter(m => {
                const fecha = new Date(m.fechaMovimiento);
                const anio = fecha.getFullYear();
                const mes = String(fecha.getMonth() + 1).padStart(2, '0');
                const dia = String(fecha.getDate()).padStart(2, '0');
                return `${anio}-${mes}-${dia}` === this.filtroFecha;
            });
        }

        this.movimientosFiltrados = resultado;
        this.cdr.detectChanges();
    }

    limpiarFiltros(): void {
        this.filtro = '';
        this.filtroFecha = '';
        this.aplicarFiltros();
    }

    seleccionarMovimiento(mov: any): void {
        this.movimientoSeleccionado = this.movimientoSeleccionado?.idMovimiento === mov.idMovimiento ? null : mov;
        this.cdr.detectChanges();
    }

    abrirModalAnular(): void {
        if (!this.movimientoSeleccionado) {
            this.alertService.warning('Seleccione una fila de la tabla para anular la venta');
            return;
        }
        if (this.movimientoSeleccionado.estadoNuevo === 'ANULADA') {
            this.alertService.warning('Esta venta ya está anulada');
            return;
        }
        this.motivoAnulacion = '';
        this.mostrarModalAnular = true;
        this.cdr.detectChanges();
    }

    cerrarModalAnular(): void {
        this.mostrarModalAnular = false;
        this.motivoAnulacion = '';
        this.cdr.detectChanges();
    }

    confirmarAnulacion(): void {
        if (!this.motivoAnulacion.trim()) {
            this.alertService.warning('Ingrese un motivo para la anulación');
            return;
        }

        const usuario = this.authService.obtenerSesion();
        if (!usuario) {
            this.alertService.error('Sesión no válida');
            return;
        }

        this.procesando = true;
        this.cdr.detectChanges();

        const url = `${this.apiUrl}api/movimientos/anular/${this.movimientoSeleccionado.idVenta}`;
        const body = JSON.stringify({ idUsuario: usuario.id, motivo: this.motivoAnulacion });

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
            },
            body: body
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            this.procesando = false;
            if (data.idNotaCredito) {
                this.alertService.success('Venta anulada correctamente. Se generó la nota de crédito.');
                this.cerrarModalAnular();
                this.movimientoSeleccionado = null;
                this.cargarMovimientos();
            } else {
                this.alertService.error(data.mensaje || 'No se pudo anular la venta');
            }
            this.cdr.detectChanges();
        })
        .catch(error => {
            this.procesando = false;
            this.alertService.error('Error al anular la venta');
            this.cdr.detectChanges();
        });
    }

    descargarComprobante(idVenta: number, event: Event): void {
        event.stopPropagation();
        this.descargarPdf(`${this.apiUrl}api/movimientos/comprobante/${idVenta}`, `Comprobante_${idVenta}.pdf`);
    }

    descargarNotaCredito(idNotaCredito: number, event: Event): void {
        event.stopPropagation();
        this.descargarPdf(`${this.apiUrl}api/movimientos/nota-credito/${idNotaCredito}`, `NotaCredito_${idNotaCredito}.pdf`);
    }

    descargarPdf(url: string, nombre: string): void {
        fetch(url, {
            headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') }
        })
        .then(response => {
            if (!response.ok) throw new Error('No autorizado');
            return response.blob();
        })
        .then(blob => {
            const enlace = document.createElement('a');
            enlace.href = URL.createObjectURL(blob);
            enlace.download = nombre;
            enlace.click();
            URL.revokeObjectURL(enlace.href);
        })
        .catch(() => {
            this.alertService.error('No se pudo descargar el documento');
        });
    }

    formatearId(id: number): string {
        if (id === null || id === undefined) return '-';
        return String(id).padStart(4, '0');
    }

    formatearFecha(fechaStr: string): string {
        if (!fechaStr) return '-';
        const fecha = new Date(fechaStr);
        return fecha.toLocaleString('es-PE', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    }

    getEstadoClass(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'PAGADA': return 'badge-pagada';
            case 'PENDIENTE': return 'badge-pendiente';
            case 'ANULADA': return 'badge-anulada';
            default: return 'badge-default';
        }
    }

    puedeDeshacer(): boolean {
        return this.movimientoSeleccionado != null
            && this.movimientoSeleccionado.estadoNuevo === 'ANULADA';
    }

    deshacerAnulacion(): void {
        if (!this.puedeDeshacer() || this.procesando) {
            return;
        }

        const usuario = this.authService.obtenerSesion();
        if (!usuario) {
            this.alertService.error('Sesión no válida');
            return;
        }

        this.procesando = true;
        this.cdr.detectChanges();

        const url = `${this.apiUrl}api/movimientos/restaurar/${this.movimientoSeleccionado.idVenta}`;

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
            },
            body: JSON.stringify({ idUsuario: usuario.id, motivo: '' })
        })
        .then(async response => {
            if (!response.ok) {
                const texto = await response.text();
                let mensaje = 'No se pudo deshacer la anulación';
                try {
                    const err = texto ? JSON.parse(texto) : null;
                    if (err && err.mensaje) {
                        mensaje = err.mensaje;
                    }
                } catch {}
                throw new Error(mensaje);
            }
            return response.json();
        })
        .then(data => {
            this.procesando = false;
            this.alertService.success('Anulación deshecha. La venta volvió a estado PAGADA.');
            this.movimientoSeleccionado = null;
            this.cargarMovimientos();
            this.cdr.detectChanges();
        })
        .catch((error: any) => {
            this.procesando = false;
            this.alertService.error(error && error.message ? error.message : 'No se pudo deshacer la anulación');
            this.cdr.detectChanges();
        });
    }

    puedeAnular(): boolean {
        return this.movimientoSeleccionado && this.movimientoSeleccionado.estadoNuevo !== 'ANULADA';
    }
}