import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.html',
    styleUrls: ['./login.css'],
    standalone: true,
    imports: [FormsModule, CommonModule, RouterLink]
})
export class Login implements OnInit {
    correo = '';
    password = '';
    error = '';
    loading = false;
    mfaPendiente = false;
    codigoMfa = '';

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService,
        private route: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            if (params['emailSent']) {
                this.alertService.info('¡Registro exitoso! Revisa tu correo electrónico para activar tu cuenta.');
            }
            if (params['verified'] === 'true') {
                this.alertService.success('¡Cuenta verificada exitosamente! Ya puedes iniciar sesión.');
            }
            if (params['error']) {
                this.alertService.error(params['message'] || 'Error al verificar la cuenta');
            }
        });
    }

    togglePassword(): void {
        const passwordInput = document.getElementById('login-password') as HTMLInputElement;
        const toggleIcon = document.getElementById('login-toggle-icon');

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleIcon?.classList.remove('bi-eye-slash-fill');
            toggleIcon?.classList.add('bi-eye-fill');
        } else {
            passwordInput.type = 'password';
            toggleIcon?.classList.remove('bi-eye-fill');
            toggleIcon?.classList.add('bi-eye-slash-fill');
        }
    }

    login(): void {
        if (!this.correo || !this.password) {
            this.alertService.warning('Complete todos los campos');
            return;
        }

        this.loading = true;
        this.error = '';
        this.cdr.detectChanges();

        this.authService.login(this.correo, this.password).subscribe({
            next: (response) => {
                this.loading = false;
                if (response.mensaje === 'MFA_REQUERIDO') {
                    this.mfaPendiente = true;
                    this.codigoMfa = '';
                    this.alertService.info('Te enviamos un código de verificación a tu correo');
                } else if (response.mensaje === 'Login exitoso') {
                    this.authService.guardarSesion(response);
                    this.router.navigate(['/inicio']);
                } else {
                    this.alertService.warning(response.mensaje);
                }
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.loading = false;
                let mensaje = 'Error de conexión';
                if (err.error?.mensaje) {
                    mensaje = err.error.mensaje;
                } else if (err.status === 401) {
                    mensaje = 'Credenciales incorrectas';
                }
                this.alertService.warning(mensaje);
                this.cdr.detectChanges();
            }
        });
    }

    restringirCodigoMfa(): void {
        this.codigoMfa = (this.codigoMfa || '').replace(/\D/g, '').slice(0, 6);
    }

    verificarCodigo(): void {
        if (!/^[0-9]{6}$/.test(this.codigoMfa)) {
            this.alertService.warning('Ingrese el código de 6 dígitos enviado a su correo');
            return;
        }

        this.loading = true;
        this.cdr.detectChanges();

        this.authService.verificarMfa(this.correo, this.codigoMfa).subscribe({
            next: (response) => {
                this.loading = false;
                if (response.mensaje === 'Login exitoso') {
                    this.authService.guardarSesion(response);
                    this.router.navigate(['/inicio']);
                } else {
                    this.alertService.warning(response.mensaje);
                }
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.loading = false;
                const mensaje = err.error?.mensaje || 'Código incorrecto o expirado';
                this.alertService.warning(mensaje);
                if (mensaje.includes('Inicie sesión nuevamente')) {
                    this.mfaPendiente = false;
                }
                this.cdr.detectChanges();
            }
        });
    }

    volverAlLogin(): void {
        this.mfaPendiente = false;
        this.codigoMfa = '';
        this.cdr.detectChanges();
    }
}