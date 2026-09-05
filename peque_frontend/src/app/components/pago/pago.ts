import { Component, Input, Output, EventEmitter, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as QRCode from 'qrcode';
import { ApiPeruService } from '../../services/api-peru.service';
import { AlertService } from '../../services/alert.service';
import { NiubizService } from '../../services/niubiz.service';

@Component({
    selector: 'app-pago',
    templateUrl: './pago.html',
    styleUrls: ['./pago.css'],
    standalone: true,
    imports: [CommonModule, FormsModule]
})
export class Pago implements OnDestroy {
    @Input() listaVenta: any[] = [];
    @Input() subtotal: number = 0;
    @Input() igv: number = 0;
    @Input() total: number = 0;
    @Input() idUsuario!: number;
    @Output() volver = new EventEmitter<void>();
    @Output() pagoConfirmado = new EventEmitter<any>();

    tipoComprobante: string = 'BOLETA';
    metodoPago: string = 'EFECTIVO';
    efectivoRecibido: number = 0;
    vuelto: number = 0;
    vueltoCalculado: boolean = false;
    
    clienteRUC: string = '';
    clienteRazonSocial: string = '';
    clienteDNI: string = '';
    clienteNombreCompleto: string = '';
    clienteNombre: string = '';
    clienteApellido: string = '';
    clienteEmail: string = '';
    buscando: boolean = false;
    procesando: boolean = false;

    qrVisible: boolean = false;
    qrDataUrl: string = '';
    qrPurchaseNumber: number = 0;
    qrTipo: string = 'yape';
    private qrPollTimer: any = null;

    constructor(
        private apiPeru: ApiPeruService,
        private alertService: AlertService,
        private niubizService: NiubizService,
        private cdr: ChangeDetectorRef
    ) {}

    restringirDni(): void {
        this.clienteDNI = (this.clienteDNI || '').replace(/\D/g, '').slice(0, 8);
    }

    restringirRuc(): void {
        this.clienteRUC = (this.clienteRUC || '').replace(/\D/g, '').slice(0, 11);
    }

    calcularVuelto(): void {
        if (this.efectivoRecibido >= this.total) {
            this.vuelto = this.efectivoRecibido - this.total;
            this.vueltoCalculado = true;
        } else {
            this.vuelto = 0;
            this.vueltoCalculado = false;
        }
    }

    reiniciarVuelto(): void {
        this.efectivoRecibido = 0;
        this.vuelto = 0;
        this.vueltoCalculado = false;
    }

    buscarClienteRUC(): void {
        if (!this.clienteRUC || this.clienteRUC.length !== 11) {
            this.alertService.warning('Ingrese un RUC válido de 11 dígitos');
            return;
        }

        this.buscando = true;
        this.cdr.detectChanges();
        this.apiPeru.buscarRUC(this.clienteRUC).subscribe({
            next: (response) => {
                this.buscando = false;
                if (response && response.success !== false) {
                    const data = response.data || response;
                    this.clienteRazonSocial = data.nombre_o_razon_social || data.razon_social || data.razonSocial || data.nombre || '';
                    if (!this.clienteRazonSocial) {
                        this.alertService.warning('RUC encontrado pero no se pudo obtener la razón social');
                    }
                } else {
                    this.alertService.error(response.message || 'RUC no encontrado');
                    this.clienteRazonSocial = '';
                }
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.buscando = false;
                this.alertService.error('Error al buscar el RUC');
                this.clienteRazonSocial = '';
                this.cdr.detectChanges();
            }
        });
    }

    buscarClienteDNI(): void {
        if (!this.clienteDNI || this.clienteDNI.length !== 8) {
            this.alertService.warning('Ingrese un DNI válido de 8 dígitos');
            return;
        }

        this.buscando = true;
        this.cdr.detectChanges();
        this.apiPeru.buscarDNI(this.clienteDNI).subscribe({
            next: (response) => {
                this.buscando = false;
                if (response && response.success !== false) {
                    const data = response.data || response;
                    const nombres = data.nombres || data.nombre || '';
                    const apellidoPaterno = data.apellidoPaterno || data.apellido_paterno || data.paterno || '';
                    const apellidoMaterno = data.apellidoMaterno || data.apellido_materno || data.materno || '';
                    
                    this.clienteNombre = nombres;
                    this.clienteApellido = (apellidoPaterno + ' ' + apellidoMaterno).trim();
                    this.clienteNombreCompleto = (this.clienteNombre + ' ' + this.clienteApellido).trim();
                    
                    if (!this.clienteNombre && !this.clienteApellido) {
                        this.alertService.warning('DNI encontrado pero no se pudo obtener los datos');
                    }
                } else {
                    this.alertService.error(response.message || 'DNI no encontrado');
                    this.clienteNombre = '';
                    this.clienteApellido = '';
                    this.clienteNombreCompleto = '';
                }
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.buscando = false;
                this.alertService.error('Error al buscar el DNI');
                this.clienteNombre = '';
                this.clienteApellido = '';
                this.clienteNombreCompleto = '';
                this.cdr.detectChanges();
            }
        });
    }

    confirmarPago(): void {
        if (this.tipoComprobante === 'FACTURA' && !this.clienteRUC) {
            this.alertService.warning('Ingrese el RUC del cliente');
            return;
        }
        if (this.tipoComprobante === 'FACTURA' && !this.clienteRazonSocial) {
            this.alertService.warning('La razón social es obligatoria para factura');
            return;
        }
        if (this.tipoComprobante === 'BOLETA') {
            if (!this.clienteDNI || !/^[0-9]{8}$/.test(this.clienteDNI)) {
                this.alertService.warning('Ingrese el DNI del cliente');
                return;
            }
            const nombreCliente = this.clienteNombreCompleto || (this.clienteNombre + ' ' + this.clienteApellido).trim();
            if (!nombreCliente) {
                this.alertService.warning('Ingrese nombres del cliente');
                return;
            }
        }
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!this.clienteEmail || !emailRegex.test(this.clienteEmail)) {
            this.alertService.warning('Ingrese un correo electrónico');
            return;
        }
        if (this.metodoPago === 'EFECTIVO' && !this.vueltoCalculado) {
            this.alertService.warning('Calcule el vuelto primero');
            return;
        }
        if (this.metodoPago === 'EFECTIVO' && this.efectivoRecibido < this.total) {
            this.alertService.warning('El efectivo recibido es insuficiente');
            return;
        }

        if (this.metodoPago === 'TARJETA') {
            this.pagarConQr('tarjeta');
            return;
        }

        if (this.metodoPago === 'YAPE') {
            this.pagarConQr('yape');
            return;
        }

        this.procesando = true;
        this.cdr.detectChanges();

        const requestData = this.construirRequestVenta();

        fetch('http://localhost:8080/api/ventas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then((err: any) => {
                    throw new Error(err && err.mensaje ? err.mensaje : 'Error al registrar la venta');
                });
            }
            return response.json();
        })
        .then(data => {
            this.procesando = false;
            this.alertService.success('Venta registrada exitosamente');
            this.pagoConfirmado.emit({
                tipoComprobante: this.tipoComprobante,
                metodoPago: this.metodoPago,
                cliente: this.tipoComprobante === 'FACTURA' ? {
                    ruc: this.clienteRUC,
                    razonSocial: this.clienteRazonSocial
                } : this.clienteDNI ? {
                    dni: this.clienteDNI,
                    nombre: this.clienteNombreCompleto || (this.clienteNombre + ' ' + this.clienteApellido).trim()
                } : null,
                email: this.clienteEmail,
                vuelto: this.metodoPago === 'EFECTIVO' ? this.vuelto : 0,
                venta: data
            });
            this.cdr.detectChanges();
        })
        .catch(error => {
            this.procesando = false;
            this.alertService.error(error && error.message ? error.message : 'Error al registrar la venta');
            this.cdr.detectChanges();
        });
    }

    construirRequestVenta(): any {
        const detalles = this.listaVenta.map(item => ({
            idProducto: item.id_producto,
            cantidad: item.cantidad,
            precioUnitario: item.precio_unitario
        }));

        const requestData: any = {
            tipoComprobante: this.tipoComprobante,
            metodoPago: this.metodoPago,
            subtotal: this.subtotal,
            igv: this.igv,
            total: this.total,
            detalles: detalles,
            idUsuario: this.idUsuario,
            clienteEmail: this.clienteEmail
        };

        if (this.tipoComprobante === 'FACTURA') {
            requestData.clienteRUC = this.clienteRUC;
            requestData.clienteRazonSocial = this.clienteRazonSocial;
        } else if (this.tipoComprobante === 'BOLETA' && this.clienteDNI) {
            requestData.clienteDNI = this.clienteDNI;
            if (this.clienteNombreCompleto) {
                requestData.clienteNombre = this.clienteNombreCompleto;
            } else if (this.clienteNombre || this.clienteApellido) {
                requestData.clienteNombre = (this.clienteNombre + ' ' + this.clienteApellido).trim();
            }
        }

        return requestData;
    }

    pagarConQr(tipo: string): void {
        this.procesando = true;
        this.qrTipo = tipo;
        this.cdr.detectChanges();

        const requestData = this.construirRequestVenta();

        this.niubizService.crearQr(this.total, requestData, tipo).subscribe({
            next: (orden) => {
                QRCode.toDataURL(orden.urlPago, { width: 260, margin: 1 })
                    .then((dataUrl: string) => {
                        this.procesando = false;
                        this.qrDataUrl = dataUrl;
                        this.qrPurchaseNumber = orden.purchaseNumber;
                        this.qrVisible = true;
                        this.iniciarPollingQr();
                        this.cdr.detectChanges();
                    })
                    .catch(() => {
                        this.procesando = false;
                        this.alertService.error('No se pudo generar el código QR');
                        this.cdr.detectChanges();
                    });
            },
            error: () => {
                this.procesando = false;
                this.alertService.error('No se pudo generar el QR de pago');
                this.cdr.detectChanges();
            }
        });
    }

    private iniciarPollingQr(): void {
        this.detenerPollingQr();
        this.qrPollTimer = setInterval(() => {
            this.niubizService.estadoQr(this.qrPurchaseNumber).subscribe({
                next: (data) => {
                    if (data.estado === 'APROBADO') {
                        this.detenerPollingQr();
                        this.qrVisible = false;
                        this.alertService.success('Pago QR confirmado. Venta registrada.');
                        this.pagoConfirmado.emit({
                            tipoComprobante: this.tipoComprobante,
                            metodoPago: this.qrTipo === 'tarjeta' ? 'TARJETA' : 'YAPE',
                            cliente: this.tipoComprobante === 'FACTURA' ? {
                                ruc: this.clienteRUC,
                                razonSocial: this.clienteRazonSocial
                            } : this.clienteDNI ? {
                                dni: this.clienteDNI,
                                nombre: this.clienteNombreCompleto || (this.clienteNombre + ' ' + this.clienteApellido).trim()
                            } : null,
                            email: this.clienteEmail,
                            vuelto: 0,
                            venta: data.venta
                        });
                        this.cdr.detectChanges();
                    } else if (data.estado === 'EXPIRADO' || data.estado === 'RECHAZADO'
                        || data.estado === 'APROBADO_SIN_VENTA') {
                        this.detenerPollingQr();
                        this.qrVisible = false;
                        this.alertService.error(data.estado === 'EXPIRADO'
                            ? 'El código QR expiró. Genere uno nuevo.'
                            : data.estado === 'APROBADO_SIN_VENTA'
                                ? 'El pago fue aprobado pero falló el registro de la venta. Contacte al administrador.'
                                : 'El pago no se pudo procesar. Intente nuevamente.');
                        this.cdr.detectChanges();
                    }
                },
                error: () => {}
            });
        }, 2500);
    }

    private detenerPollingQr(): void {
        if (this.qrPollTimer) {
            clearInterval(this.qrPollTimer);
            this.qrPollTimer = null;
        }
    }

    cancelarQr(): void {
        this.detenerPollingQr();
        this.qrVisible = false;
        this.qrDataUrl = '';
        this.cdr.detectChanges();
    }

    ngOnDestroy(): void {
        this.detenerPollingQr();
    }

    volverALista(): void {
        this.volver.emit();
    }
}