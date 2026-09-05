import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../services/alert.service';
import { appConfig } from '../../config/appConfig';

@Component({
    selector: 'app-mis-ventas',
    templateUrl: './mis-ventas.html',
    styleUrls: ['./mis-ventas.css'],
    standalone: true,
    imports: [CommonModule, FormsModule]
})
export class MisVentas implements OnInit {
    ventas: any[] = [];
    ventasFiltradas: any[] = [];
    resumen: any = {
        totalVentas: 0, totalIngresos: 0, ticketPromedio: 0,
        ventasHoy: 0, ingresosHoy: 0, ventasAnuladas: 0
    };
    cargando = false;
    filtro = '';
    filtroFecha = '';

    private apiUrl = appConfig.apiUrl;

    constructor(
        private alertService: AlertService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.cargarResumen();
        this.cargarVentas();
    }

    private get(url: string): Promise<any> {
        return fetch(this.apiUrl + url, {
            headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') }
        }).then(r => r.json());
    }

    cargarResumen(): void {
        this.get('api/mis-ventas/resumen').then(data => {
            this.resumen = data;
            this.cdr.detectChanges();
        }).catch(() => {});
    }

    cargarVentas(): void {
        this.cargando = true;
        this.cdr.detectChanges();
        this.get('api/mis-ventas').then(data => {
            this.ventas = Array.isArray(data) ? data : [];
            this.aplicarFiltros();
            this.cargando = false;
            this.cdr.detectChanges();
        }).catch(() => {
            this.cargando = false;
            this.alertService.error('No se pudieron cargar tus ventas');
            this.cdr.detectChanges();
        });
    }

    aplicarFiltros(): void {
        let lista = [...this.ventas];
        const termino = this.filtro.trim().toLowerCase();
        if (termino) {
            lista = lista.filter(v =>
                (v.comprobante || '').toLowerCase().includes(termino) ||
                (v.cliente || '').toLowerCase().includes(termino) ||
                String(v.idVenta).includes(termino));
        }
        if (this.filtroFecha) {
            lista = lista.filter(v => (v.fecha || '').substring(0, 10) === this.filtroFecha);
        }
        this.ventasFiltradas = lista;
        this.cdr.detectChanges();
    }

    limpiarFiltros(): void {
        this.filtro = '';
        this.filtroFecha = '';
        this.aplicarFiltros();
    }

    getEstadoClass(estado: string): string {
        switch ((estado || '').toUpperCase()) {
            case 'PAGADA': return 'badge-pagada';
            case 'ANULADA': return 'badge-anulada';
            default: return 'badge-default';
        }
    }

    descargarBoleta(idVenta: number, event: Event): void {
        event.stopPropagation();
        fetch(`${this.apiUrl}api/mis-ventas/comprobante/${idVenta}`, {
            headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') }
        })
            .then(response => {
                if (!response.ok) throw new Error('No autorizado');
                return response.blob();
            })
            .then(blob => {
                const enlace = document.createElement('a');
                enlace.href = URL.createObjectURL(blob);
                enlace.download = `Boleta_${idVenta}.pdf`;
                enlace.click();
                URL.revokeObjectURL(enlace.href);
            })
            .catch(() => this.alertService.error('No se pudo descargar la boleta'));
    }
}
