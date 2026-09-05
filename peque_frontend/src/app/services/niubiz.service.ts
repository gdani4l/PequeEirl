import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { appConfig } from '../config/appConfig';

declare var VisanetCheckout: any;

@Injectable({ providedIn: 'root' })
export class NiubizService {

    private apiUrl = appConfig.apiUrl;
    private scriptUrl = 'https://static-content-qas.vnforapps.com/v2/js/checkout.js?qa=true';
    private scriptCargado = false;

    constructor(private http: HttpClient) {}

    crearSesion(monto: number, venta: any): Observable<any> {
        return this.http.post(this.apiUrl + 'api/niubiz/sesion', { monto, venta });
    }

    crearQr(monto: number, venta: any, tipo: string = 'yape'): Observable<any> {
        return this.http.post(this.apiUrl + 'api/niubiz/qr/crear?tipo=' + tipo, { monto, venta });
    }

    estadoQr(purchaseNumber: number): Observable<any> {
        return this.http.get(this.apiUrl + 'api/niubiz/qr/estado/' + purchaseNumber);
    }

    cargarScript(): Promise<void> {
        return new Promise((resolve, reject) => {
            if (this.scriptCargado && typeof VisanetCheckout !== 'undefined') {
                resolve();
                return;
            }
            const script = document.createElement('script');
            script.src = this.scriptUrl;
            script.onload = () => {
                this.scriptCargado = true;
                resolve();
            };
            script.onerror = () => reject(new Error('No se pudo cargar el formulario de Niubiz'));
            document.head.appendChild(script);
        });
    }

    abrirCheckout(config: {
        sessionKey: string;
        merchantId: string;
        purchaseNumber: number;
        monto: number;
        email?: string;
    }): void {
        const actionUrl = this.apiUrl + 'api/niubiz/respuesta?purchase=' + config.purchaseNumber;
        const timeoutUrl = window.location.origin + '/pago-resultado?estado=timeout';

        VisanetCheckout.configure({
            action: actionUrl,
            sessiontoken: config.sessionKey,
            channel: 'web',
            merchantid: config.merchantId,
            purchasenumber: config.purchaseNumber,
            amount: config.monto.toFixed(2),
            expirationminutes: '20',
            timeouturl: timeoutUrl,
            merchantname: 'Peque POS',
            formbuttoncolor: '#2563eb',
            cardholderemail: config.email || ''
        });
        VisanetCheckout.open();
    }
}
