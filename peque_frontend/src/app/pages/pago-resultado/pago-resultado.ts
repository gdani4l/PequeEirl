import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'app-pago-resultado',
    templateUrl: './pago-resultado.html',
    styleUrls: ['./pago-resultado.css'],
    standalone: true,
    imports: [CommonModule]
})
export class PagoResultado implements OnInit {

    estado: string = '';
    serie: string = '';
    numero: string = '';
    monto: string = '';
    tarjeta: string = '';
    marca: string = '';
    descripcion: string = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            this.estado = params['estado'] || 'error';
            this.serie = params['serie'] || '';
            this.numero = params['numero'] || '';
            this.monto = params['monto'] || '';
            this.tarjeta = params['tarjeta'] || '';
            this.marca = params['marca'] || '';
            this.descripcion = params['descripcion'] || '';
            this.cdr.detectChanges();
        });
    }

    volver(): void {
        this.router.navigate(['/inicio']);
    }
}
