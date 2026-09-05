import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-confirm-modal',
    templateUrl: './confirm-modal.html',
    styleUrls: ['./confirm-modal.css'],
    standalone: true,
    imports: [CommonModule]
})
export class ConfirmModal {
    @Input() titulo: string = 'Confirmar';
    @Input() mensaje: string = '¿Estás seguro de realizar esta acción?';
    @Input() textoSi: string = 'Sí';
    @Input() textoNo: string = 'No';
    @Output() onConfirm = new EventEmitter<void>();
    @Output() onCancel = new EventEmitter<void>();
    
    visible: boolean = false;

    abrir(): void {
        this.visible = true;
    }

    cerrar(): void {
        this.visible = false;
    }

    confirmar(): void {
        this.onConfirm.emit();
        this.cerrar();
    }

    cancelar(): void {
        this.onCancel.emit();
        this.cerrar();
    }
}