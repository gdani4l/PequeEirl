import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';
import { ApiPeruService } from '../../services/api-peru.service';
import { appConfig } from '../../config/appConfig';
import { ubigeoINEI, ubigeo } from 'peru-utils';

interface UbigeoItem {
    code: string;
    name: string;
}

@Component({
    selector: 'app-admin-cuentas',
    templateUrl: './admin-cuentas.html',
    styleUrls: ['./admin-cuentas.css'],
    standalone: true,
    imports: [CommonModule, FormsModule]
})
export class AdminCuentas implements OnInit {
    usuarios: any[] = [];
    usuario: any = {
        id_usuario: null,
        nombre: '',
        segundo_nombre: '',
        apellido_pat: '',
        apellido_mat: '',
        correo: '',
        rol: '',
        telefono: '',
        activo: true,
        departamento: '',
        provincia: '',
        distrito: '',
        calle: '',
        referencia: '',
        numero_documento: '',
        id_tipo_documento: 3,
        departamentoCode: '',
        provinciaCode: '',
        distritoCode: '',
        codigo_ubigeo: ''
    };

    departamentos: UbigeoItem[] = [];
    provincias: UbigeoItem[] = [];
    distritos: UbigeoItem[] = [];

    cargando: boolean = false;
    editando: boolean = false;
    buscando: boolean = false;
    usuarioSeleccionado: any = null;
    
    private apiUrl = appConfig.apiUrl;

    constructor(
        private authService: AuthService,
        private alertService: AlertService,
        private apiPeru: ApiPeruService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.cargarDepartamentos();
        this.cargarUsuarios();
    }

    cargarUsuarios(): void {
        this.cargando = true;
        fetch(`${this.apiUrl}api/usuarios`)
            .then(response => response.json())
            .then(data => {
                this.usuarios = data;
                this.cargando = false;
                this.cdr.detectChanges();
            })
            .catch(error => {
                this.cargando = false;
                this.alertService.error('Error al cargar los usuarios');
            });
    }


    cargarDepartamentos(): void {
        const result = ubigeoINEI.getDepartments();
        this.departamentos = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        this.cdr.detectChanges();
    }

    onDepartamentoChange(): void {
        this.usuario.provinciaCode = '';
        this.usuario.distritoCode = '';
        this.provincias = [];
        this.distritos = [];

        if (this.usuario.departamentoCode) {
            const result = ubigeoINEI.getProvince(this.usuario.departamentoCode);
            this.provincias = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        }
        this.cdr.detectChanges();
    }

    onProvinciaChange(): void {
        this.usuario.distritoCode = '';
        this.distritos = [];

        if (this.usuario.departamentoCode && this.usuario.provinciaCode) {
            const result = ubigeoINEI.getDistrict(this.usuario.provinciaCode);
            this.distritos = Array.isArray(result) ? result.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        }
        this.cdr.detectChanges();
    }

    restringirUbigeo(): void {
        this.usuario.codigo_ubigeo = (this.usuario.codigo_ubigeo || '').replace(/\D/g, '').slice(0, 6);
    }

    buscarUbigeo(): void {
        const codigo = this.usuario.codigo_ubigeo;
        if (!codigo || codigo.length !== 6) {
            this.alertService.warning('Ingrese un ubigeo válido de 6 dígitos');
            return;
        }

        try {
            const result = ubigeo.findByIdUbigeo(codigo);
            if (result && result.id) {
                const details = ubigeoINEI.getUbigeoDetails(result.id) as any;
                if (details && details.department && details.province && details.district) {
                    this.seleccionarUbicacionPorNombres(details.department, details.province, details.district);
                    if (this.usuario.distritoCode) {
                        this.alertService.success('Ubicación encontrada correctamente');
                        this.cdr.detectChanges();
                        return;
                    }
                }
            }
            this.buscarUbigeoManual(codigo);
        } catch (e) {
            this.buscarUbigeoManual(codigo);
        }
        this.cdr.detectChanges();
    }

    buscarUbigeoManual(codigo: string): void {
        for (const dep of this.departamentos) {
            const provinciasResult = ubigeoINEI.getProvince(dep.code) as UbigeoItem[];
            if (!provinciasResult) continue;

            for (const prov of provinciasResult) {
                const distritosResult = ubigeoINEI.getDistrict(prov.code) as UbigeoItem[];
                if (!distritosResult) continue;

                for (const dist of distritosResult) {
                    if (dist.code === codigo) {
                        this.usuario.departamentoCode = dep.code;
                        this.provincias = Array.isArray(provinciasResult) ? provinciasResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];

                        this.usuario.provinciaCode = prov.code;
                        this.distritos = Array.isArray(distritosResult) ? distritosResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];

                        this.usuario.distritoCode = dist.code;
                        this.alertService.success('Ubicación encontrada correctamente');
                        this.cdr.detectChanges();
                        return;
                    }
                }
            }
        }
        this.alertService.error('Ubigeo no encontrado');
        this.cdr.detectChanges();
    }

    seleccionarUbicacionPorNombres(depName: string, provName: string, distName: string): void {
        const dep = this.departamentos.find(d => d.name === depName);
        if (!dep) {
            return;
        }
        this.usuario.departamentoCode = dep.code;

        const provinciasResult = ubigeoINEI.getProvince(dep.code);
        this.provincias = Array.isArray(provinciasResult) ? provinciasResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        const prov = this.provincias.find(p => p.name === provName);
        if (!prov) {
            return;
        }
        this.usuario.provinciaCode = prov.code;

        const distritosResult = ubigeoINEI.getDistrict(prov.code);
        this.distritos = Array.isArray(distritosResult) ? distritosResult.filter((item): item is UbigeoItem => item !== undefined && item !== null) : [];
        const dist = this.distritos.find(d => d.name === distName);
        if (dist) {
            this.usuario.distritoCode = dist.code;
        }
    }

    aplicarNombresUbicacion(): void {
        const dep = this.departamentos.find(d => d.code === this.usuario.departamentoCode);
        const prov = this.provincias.find(p => p.code === this.usuario.provinciaCode);
        const dist = this.distritos.find(d => d.code === this.usuario.distritoCode);
        this.usuario.departamento = dep ? dep.name : '';
        this.usuario.provincia = prov ? prov.name : '';
        this.usuario.distrito = dist ? dist.name : '';
    }

    restringirDni(): void {
        this.usuario.numero_documento = (this.usuario.numero_documento || '').replace(/\D/g, '').slice(0, 8);
    }

    restringirTelefono(): void {
        let valor = (this.usuario.telefono || '').replace(/\D/g, '');
        if (valor.length > 0 && valor.charAt(0) !== '9') {
            valor = '';
        }
        this.usuario.telefono = valor.slice(0, 9);
    }

    buscarDni(): void {
        const documento = this.usuario.numero_documento;
        if (!documento || !/^[0-9]{8}$/.test(documento)) {
            this.alertService.warning('Ingrese un DNI válido');
            return;
        }

        this.buscando = true;
        this.cdr.detectChanges();

        this.apiPeru.buscarDNI(documento).subscribe({
            next: (response) => {
                this.buscando = false;
                if (response && response.success !== false) {
                    const data = response.data || response;
                    const nombres = data.nombres || data.nombre || data.nombres_completos || '';
                    const partes = nombres.trim().split(/\s+/);
                    this.usuario.nombre = this.capitalizar(partes[0] || '');
                    this.usuario.segundo_nombre = partes.length > 1 ? this.capitalizar(partes.slice(1).join(' ')) : '';
                    this.usuario.apellido_pat = this.capitalizar(data.apellidoPaterno || data.apellido_paterno || data.paterno || '');
                    this.usuario.apellido_mat = this.capitalizar(data.apellidoMaterno || data.apellido_materno || data.materno || '');
                    if (!this.usuario.nombre && !this.usuario.apellido_pat) {
                        this.alertService.warning('DNI encontrado pero no se pudieron obtener los datos');
                    } else {
                        this.alertService.success('Datos encontrados correctamente');
                    }
                } else {
                    this.alertService.error(response.message || 'DNI no encontrado');
                }
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

    seleccionarUsuario(usuario: any): void {
        this.usuarioSeleccionado = usuario;
        this.usuario = {
            id_usuario: usuario.id_usuario,
            nombre: usuario.nombre || '',
            segundo_nombre: usuario.segundo_nombre || '',
            apellido_pat: usuario.apellido_pat || '',
            apellido_mat: usuario.apellido_mat || '',
            correo: usuario.correo || '',
            rol: usuario.rol_nombre || '',
            telefono: usuario.telefono || '',
            activo: usuario.activo,
            departamento: usuario.departamento || '',
            provincia: usuario.provincia || '',
            distrito: usuario.distrito || '',
            calle: usuario.calle || '',
            referencia: usuario.referencia || '',
            numero_documento: usuario.numero_documento || '',
            id_tipo_documento: usuario.id_tipo_documento || 3,
            departamentoCode: '',
            provinciaCode: '',
            distritoCode: '',
            codigo_ubigeo: ''
        };
        this.provincias = [];
        this.distritos = [];
        if (usuario.departamento) {
            this.seleccionarUbicacionPorNombres(usuario.departamento, usuario.provincia || '', usuario.distrito || '');
        }
        this.editando = true;
        this.cdr.detectChanges();
    }

    cancelarEdicion(): void {
        this.editando = false;
        this.usuarioSeleccionado = null;
        this.usuario = {
            id_usuario: null,
            nombre: '',
            segundo_nombre: '',
            apellido_pat: '',
            apellido_mat: '',
            correo: '',
            rol: '',
            telefono: '',
            activo: true,
            departamento: '',
            provincia: '',
            distrito: '',
            calle: '',
            referencia: '',
            numero_documento: '',
            id_tipo_documento: 3
        };
        this.cdr.detectChanges();
    }

    actualizarUsuario(): void {
        if (!this.validarCampos()) {
            return;
        }

        this.aplicarNombresUbicacion();
        if (!this.usuario.departamento || !this.usuario.provincia || !this.usuario.distrito) {
            this.alertService.warning('Seleccione departamento, provincia y distrito');
            return;
        }

        this.cargando = true;
        this.cdr.detectChanges();
        
        const data = {
            nombre: this.usuario.nombre,
            segundo_nombre: this.usuario.segundo_nombre,
            apellido_pat: this.usuario.apellido_pat,
            apellido_mat: this.usuario.apellido_mat,
            correo: this.usuario.correo,
            rol: this.usuario.rol,
            telefono: this.usuario.telefono,
            activo: this.usuario.activo,
            departamento: this.usuario.departamento,
            provincia: this.usuario.provincia,
            distrito: this.usuario.distrito,
            calle: this.usuario.calle,
            referencia: this.usuario.referencia,
            numero_documento: this.usuario.numero_documento,
            id_tipo_documento: this.usuario.id_tipo_documento
        };
        
        this.authService.actualizarUsuario(this.usuario.id_usuario, data).subscribe({
            next: (response) => {
                setTimeout(() => {
                    this.cargando = false;
                    this.cdr.detectChanges();
                    this.alertService.success(response.mensaje);
                    this.cargarUsuarios();
                    this.cancelarEdicion();
                }, 100);
            },
            error: (err) => {
                setTimeout(() => {
                    this.cargando = false;
                    this.cdr.detectChanges();
                    this.alertService.error(err.error?.mensaje || 'Error al actualizar');
                }, 100);
            }
        });
    }

    validarCampos(): boolean {
        if (!this.usuario.nombre || this.usuario.nombre.trim() === '') {
            this.alertService.warning('Ingrese el primer nombre');
            return false;
        }
        if (!this.usuario.apellido_pat || this.usuario.apellido_pat.trim() === '') {
            this.alertService.warning('Ingrese el apellido paterno');
            return false;
        }
        if (!this.usuario.apellido_mat || this.usuario.apellido_mat.trim() === '') {
            this.alertService.warning('Ingrese el apellido materno');
            return false;
        }
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!this.usuario.correo || !emailRegex.test(this.usuario.correo)) {
            this.alertService.warning('Ingrese un correo válido');
            return false;
        }
        if (!this.usuario.rol) {
            this.alertService.warning('Seleccione un rol');
            return false;
        }
        if (!this.usuario.telefono || !/^9[0-9]{8}$/.test(this.usuario.telefono)) {
            this.alertService.warning('Ingrese un número de teléfono válido');
            return false;
        }
        if (!this.usuario.departamento || this.usuario.departamento.trim() === '') {
            this.alertService.warning('Ingrese el departamento');
            return false;
        }
        if (!this.usuario.provincia || this.usuario.provincia.trim() === '') {
            this.alertService.warning('Ingrese la provincia');
            return false;
        }
        if (!this.usuario.distrito || this.usuario.distrito.trim() === '') {
            this.alertService.warning('Ingrese el distrito');
            return false;
        }
        if (!this.usuario.calle || this.usuario.calle.trim() === '') {
            this.alertService.warning('Ingrese la dirección');
            return false;
        }
        return true;
    }
}