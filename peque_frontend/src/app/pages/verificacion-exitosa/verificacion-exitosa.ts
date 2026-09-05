import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-verificacion-exitosa',
    templateUrl: './verificacion-exitosa.html',
    styleUrls: ['./verificacion-exitosa.css'],
    standalone: true,
    imports: [CommonModule, RouterLink]
})
export class VerificacionExitosa {
    constructor() {}
}