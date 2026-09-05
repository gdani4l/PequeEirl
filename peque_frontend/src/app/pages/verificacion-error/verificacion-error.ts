import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-verificacion-error',
    templateUrl: './verificacion-error.html',
    styleUrls: ['./verificacion-error.css'],
    standalone: true,
    imports: [CommonModule, RouterLink]
})
export class VerificacionError {
    constructor() {}
}