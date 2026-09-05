import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.html',
    styleUrls: ['./alert.css'],
    standalone: true,
    imports: [CommonModule]
})
export class Alert {
    @Input() mensaje: string = '';
    @Input() tipo: 'success' | 'error' | 'info' | 'warning' = 'info';
    @Output() closed = new EventEmitter<void>();
    visible: boolean = false;
    private timeoutId: any;

    mostrar(mensaje: string, tipo: 'success' | 'error' | 'info' | 'warning' = 'info', duracion: number = 3000): void {
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.visible = true;

        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }

        this.timeoutId = setTimeout(() => {
            this.cerrar();
        }, duracion);
    }

    cerrar(): void {
        this.visible = false;
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }
        this.closed.emit();
    }
}