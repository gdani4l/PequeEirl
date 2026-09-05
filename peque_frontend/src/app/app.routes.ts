import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';
import { Inicio } from './pages/inicio/inicio';
import { Ventas } from './pages/ventas/ventas';
import { VerificacionExitosa } from './pages/verificacion-exitosa/verificacion-exitosa';
import { VerificacionError } from './pages/verificacion-error/verificacion-error';
import { PagoResultado } from './pages/pago-resultado/pago-resultado';

export const routes: Routes = [
    { path: 'login', component: Login },
    { path: 'registro', component: Registro },
    { path: 'inicio', component: Inicio, canActivate: [authGuard] },
    { path: 'ventas', component: Ventas, canActivate: [authGuard] },
    { path: 'verificacion-exitosa', component: VerificacionExitosa },
    { path: 'verificacion-error', component: VerificacionError },
    { path: 'pago-resultado', component: PagoResultado, canActivate: [authGuard] },
    { path: '', redirectTo: '/login', pathMatch: 'full' }
];