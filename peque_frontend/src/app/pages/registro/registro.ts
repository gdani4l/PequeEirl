import { Component, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';
import { ApiPeruService } from '../../services/api-peru.service';
import { ubigeoINEI, ubigeo } from 'peru-utils';

interface UbigeoItem {
    code: string;
    name: string;
}

@Component({
    selector: 'app-registro',
    templateUrl: './registro.html',
    styleUrls: ['./registro.css'],
    standalone: true,
    imports: [FormsModule, CommonModule, RouterLink]
})
export class Registro {
    usuario = {
        nombre: '',
        segundoNombre: '',
        apellidoPat: '',
        apellidoMat: '',
        telefono: '',
        correo: '',
        password: '',
        tipoDocumento: 'DNI',
        numeroDocumento: '',
        departamento: '',
        departamentoCode: '',
        provincia: '',
        provinciaCode: '',
        distrito: '',
        distritoCode: '',
        calle: '',
        referencia: '',
        codigo_postal: ''
    };
    confirmPassword = '';
    mensaje = '';
    error = '';
    loading = false;
    buscando = false;

    departamentos: UbigeoItem[] = [];
    provincias: UbigeoItem[] = [];
    distritos: UbigeoItem[] = [];

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService,
        private cdr: ChangeDetectorRef,
        private apiPeru: ApiPeruService
    ) {}

    ngOnInit(): void {
        this.cargarDepartamentos();
    }

    cargarDepartamentos(): void {
        const result = ubigeoINEI.getDepartments();
        this.departamentos = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        this.cdr.detectChanges();
    }

    restringirDni(): void {
        this.usuario.numeroDocumento = (this.usuario.numeroDocumento || '').replace(/\D/g, '').slice(0, 8);
    }

    restringirTelefono(): void {
        let valor = (this.usuario.telefono || '').replace(/\D/g, '');
        if (valor.length > 0 && valor.charAt(0) !== '9') {
            valor = '';
        }
        this.usuario.telefono = valor.slice(0, 9);
    }

    buscarDocumento(): void {
        const documento = this.usuario.numeroDocumento;
        if (!documento || !/^[0-9]{8}$/.test(documento)) {
            this.alertService.warning('Ingrese un DNI válido de 8 dígitos');
            return;
        }

        this.buscando = true;
        this.cdr.detectChanges();

        this.apiPeru.buscarDNI(documento).subscribe({
            next: (response) => {
                this.procesarRespuesta(response);
                this.buscando = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.buscando = false;
                this.alertService.error('DNI no encontrado');
                this.cdr.detectChanges();
            }
        });
    }

    capitalizar(texto: string): string {
        if (!texto) return '';
        return texto.toLowerCase().replace(/\b\w/g, (letra) => letra.toUpperCase());
    }

    separarNombres(nombreCompleto: string): { primerNombre: string; segundoNombre: string } {
        if (!nombreCompleto) return { primerNombre: '', segundoNombre: '' };
        const nombres = nombreCompleto.trim().split(/\s+/);
        const primerNombre = this.capitalizar(nombres[0] || '');
        const segundoNombre = nombres.length > 1 ? this.capitalizar(nombres.slice(1).join(' ')) : '';
        return { primerNombre, segundoNombre };
    }

    procesarRespuesta(data: any): void {

        if (data.success === false) {
            this.alertService.error(data.message || 'Documento no encontrado');
            return;
        }

        const persona = data.data || data;

        let nombresCompletos = '';
        if (persona.nombres) {
            nombresCompletos = persona.nombres;
        } else if (persona.nombre) {
            nombresCompletos = persona.nombre;
        } else if (persona.nombres_completos) {
            nombresCompletos = persona.nombres_completos;
        }

        if (nombresCompletos) {
            const { primerNombre, segundoNombre } = this.separarNombres(nombresCompletos);
            this.usuario.nombre = primerNombre;
            this.usuario.segundoNombre = segundoNombre;
        }

        const apellidoPaterno = persona.apellidoPaterno || persona.apellido_paterno || persona.paterno || '';
        const apellidoMaterno = persona.apellidoMaterno || persona.apellido_materno || persona.materno || '';

        this.usuario.apellidoPat = this.capitalizar(apellidoPaterno);
        this.usuario.apellidoMat = this.capitalizar(apellidoMaterno);

        if (!apellidoPaterno && !apellidoMaterno && persona.apellidos) {
            const apellidos = persona.apellidos.trim().split(/\s+/);
            if (apellidos.length >= 1) {
                this.usuario.apellidoPat = this.capitalizar(apellidos[0]);
            }
            if (apellidos.length >= 2) {
                this.usuario.apellidoMat = this.capitalizar(apellidos.slice(1).join(' '));
            }
        }

        this.alertService.success('Datos encontrados correctamente');
    }

    buscarPorUbigeo(ubigeoCode: string): void {

        try {
            const result = ubigeo.findByIdUbigeo(ubigeoCode);

            if (result && result.id) {
                const details = ubigeoINEI.getUbigeoDetails(result.id) as any;

                if (details && details.department) {
                    const depName = details.department;
                    const provName = details.province;
                    const distName = details.district;

                    this.usuario.departamento = depName;
                    this.usuario.provincia = provName;
                    this.usuario.distrito = distName;

                    const dep = this.departamentos.find(d => d.name === depName);
                    if (dep) {
                        this.usuario.departamentoCode = dep.code;
                        const provinciasResult = ubigeoINEI.getProvince(dep.code) as UbigeoItem[];
                        this.provincias = Array.isArray(provinciasResult) ? provinciasResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
                        this.cdr.detectChanges();

                        const prov = this.provincias.find(p => p.name === provName);
                        if (prov) {
                            this.usuario.provinciaCode = prov.code;
                            const distritosResult = ubigeoINEI.getDistrict(prov.code) as UbigeoItem[];
                            this.distritos = Array.isArray(distritosResult) ? distritosResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
                            this.cdr.detectChanges();

                            const dist = this.distritos.find(d => d.name === distName);
                            if (dist) {
                                this.usuario.distritoCode = dist.code;
                                this.cdr.detectChanges();
                                return;
                            }
                        }
                    }
                    this.buscarPorNombreUbicacion(depName, provName, distName);
                } else {
                    this.buscarUbigeoManual(ubigeoCode);
                }
            } else {
                this.buscarUbigeoManual(ubigeoCode);
            }
        } catch (error) {
            this.buscarUbigeoManual(ubigeoCode);
        }
    }

    buscarUbigeoManual(ubigeoCode: string): void {
        try {
            for (const dep of this.departamentos) {
                const provincias = ubigeoINEI.getProvince(dep.code) as UbigeoItem[];
                if (!provincias) continue;

                for (const prov of provincias) {
                    const distritos = ubigeoINEI.getDistrict(prov.code) as UbigeoItem[];
                    if (!distritos) continue;

                    for (const dist of distritos) {
                        if (dist.code === ubigeoCode) {

                            this.usuario.departamento = dep.name;
                            this.usuario.departamentoCode = dep.code;
                            this.usuario.provincia = prov.name;
                            this.usuario.provinciaCode = prov.code;
                            this.usuario.distrito = dist.name;
                            this.usuario.distritoCode = dist.code;

                            const provinciasResult = ubigeoINEI.getProvince(dep.code) as UbigeoItem[];
                            this.provincias = Array.isArray(provinciasResult) ? provinciasResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];

                            const distritosResult = ubigeoINEI.getDistrict(prov.code) as UbigeoItem[];
                            this.distritos = Array.isArray(distritosResult) ? distritosResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];

                            this.cdr.detectChanges();
                            return;
                        }
                    }
                }
            }
            this.alertService.warning('Ubigeo no encontrado, seleccione manualmente');
        } catch (error) {
            this.alertService.warning('Error al buscar el ubigeo');
        }
    }

    buscarPorNombreUbicacion(departamento: string, provincia: string, distrito: string): void {

        if (!departamento || !provincia || !distrito) {
            return;
        }

        const depName = this.capitalizar(departamento);
        const provName = this.capitalizar(provincia);
        const distName = this.capitalizar(distrito);

        const dep = this.departamentos.find(d => d.name === depName);
        if (!dep) {
            this.alertService.warning('Departamento no encontrado');
            return;
        }

        this.usuario.departamento = depName;
        this.usuario.departamentoCode = dep.code;

        const provinciasResult = ubigeoINEI.getProvince(dep.code) as UbigeoItem[];
        this.provincias = Array.isArray(provinciasResult) ? provinciasResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        this.cdr.detectChanges();

        const prov = this.provincias.find(p => p.name === provName);
        if (!prov) {
            this.alertService.warning('Provincia no encontrada');
            return;
        }

        this.usuario.provincia = provName;
        this.usuario.provinciaCode = prov.code;

        const distritosResult = ubigeoINEI.getDistrict(prov.code) as UbigeoItem[];
        this.distritos = Array.isArray(distritosResult) ? distritosResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        this.cdr.detectChanges();

        const dist = this.distritos.find(d => d.name === distName);
        if (dist) {
            this.usuario.distrito = distName;
            this.usuario.distritoCode = dist.code;
            this.cdr.detectChanges();
        } else {
            this.alertService.warning('Distrito no encontrado');
        }
    }

    onDepartamentoChange(): void {
        this.usuario.provincia = '';
        this.usuario.provinciaCode = '';
        this.usuario.distrito = '';
        this.usuario.distritoCode = '';
        this.provincias = [];
        this.distritos = [];

        if (this.usuario.departamentoCode) {
            const result = ubigeoINEI.getProvince(this.usuario.departamentoCode);
            this.provincias = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
            this.cdr.detectChanges();
        }
    }

    onProvinciaChange(): void {
        this.usuario.distrito = '';
        this.usuario.distritoCode = '';
        this.distritos = [];

        if (this.usuario.departamentoCode && this.usuario.provinciaCode) {
            const result = ubigeoINEI.getDistrict(this.usuario.provinciaCode);
            this.distritos = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
            this.cdr.detectChanges();
        }
    }

    buscarPorUbigeoManual(): void {
        const ubigeoCode = this.usuario.codigo_postal;
        if (!ubigeoCode || ubigeoCode.length < 6) {
            this.alertService.warning('Ingrese un ubigeo válido (6 dígitos)');
            return;
        }
        this.buscarPorUbigeo(ubigeoCode);
    }

    togglePassword(fieldId: string): void {
        const input = document.getElementById(fieldId) as HTMLInputElement;
        input.type = input.type === 'password' ? 'text' : 'password';
    }

    validar(): boolean {
        if (!this.usuario.nombre || this.usuario.nombre.trim() === '') {
            this.alertService.warning('Ingrese su nombre');
            return false;
        }
        if (!this.usuario.apellidoPat || this.usuario.apellidoPat.trim() === '') {
            this.alertService.warning('Ingrese su apellido paterno');
            return false;
        }
        if (!this.usuario.apellidoMat || this.usuario.apellidoMat.trim() === '') {
            this.alertService.warning('Ingrese su apellido materno');
            return false;
        }
        const telefonoRegex = /^9[0-9]{8}$/;
        if (!this.usuario.telefono || !telefonoRegex.test(this.usuario.telefono)) {
            this.alertService.warning('Ingrese un teléfono válido de 9 dígitos que empiece por 9');
            return false;
        }
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!this.usuario.correo || !emailRegex.test(this.usuario.correo)) {
            this.alertService.warning('Ingrese un correo electrónico válido');
            return false;
        }
        if (!this.usuario.password || this.usuario.password.length < 8) {
            this.alertService.warning('La contraseña debe tener al menos 8 caracteres');
            return false;
        }
        if (this.usuario.password !== this.confirmPassword) {
            this.alertService.warning('Las contraseñas no coinciden');
            return false;
        }
        const dniRegex = /^[0-9]{8}$/;
        if (!this.usuario.numeroDocumento || !dniRegex.test(this.usuario.numeroDocumento)) {
            this.alertService.warning('Ingrese un DNI válido de 8 dígitos');
            return false;
        }
        if (!this.usuario.departamentoCode) {
            this.alertService.warning('Seleccione un departamento');
            return false;
        }
        if (!this.usuario.provinciaCode) {
            this.alertService.warning('Seleccione una provincia');
            return false;
        }
        if (!this.usuario.distritoCode) {
            this.alertService.warning('Seleccione un distrito');
            return false;
        }
        if (!this.usuario.calle || this.usuario.calle.trim() === '') {
            this.alertService.warning('Ingrese su calle');
            return false;
        }
        return true;
    }

    registrar(): void {
        if (!this.validar()) {
            return;
        }

        const dep = this.departamentos.find(d => d.code === this.usuario.departamentoCode);
        const prov = this.provincias.find(p => p.code === this.usuario.provinciaCode);
        const dist = this.distritos.find(d => d.code === this.usuario.distritoCode);

        this.usuario.departamento = dep ? dep.name : '';
        this.usuario.provincia = prov ? prov.name : '';
        this.usuario.distrito = dist ? dist.name : '';

        this.loading = true;
        this.cdr.detectChanges();
        this.error = '';
        this.mensaje = '';

        this.authService.registro(this.usuario).subscribe({
            next: (response) => {
                setTimeout(() => {
                    this.loading = false;
                    this.cdr.detectChanges();
                    if (response.mensaje && response.mensaje.includes('revisa tu correo')) {
                        this.alertService.success(response.mensaje);
                        this.router.navigate(['/login'], { queryParams: { emailSent: true } });
                    } else {
                        this.alertService.success(response.mensaje);
                        setTimeout(() => {
                            this.router.navigate(['/login']);
                        }, 2000);
                    }
                }, 100);
            },
            error: (err) => {
                setTimeout(() => {
                    this.loading = false;
                    this.cdr.detectChanges();
                    const mensaje = err.error?.mensaje || 'Error en el registro';
                    this.alertService.error(mensaje);
                }, 100);
            }
        });
    }
}