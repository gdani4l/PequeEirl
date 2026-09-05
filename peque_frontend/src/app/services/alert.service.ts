import { Injectable, ApplicationRef, createComponent, EnvironmentInjector } from '@angular/core';
import { Alert } from '../components/alert/alert';

@Injectable({ providedIn: 'root' })
export class AlertService {
    private alertComponent: any = null;

    constructor(private appRef: ApplicationRef, private injector: EnvironmentInjector) {}

    private mostrarAlerta(mensaje: string, tipo: 'success' | 'error' | 'info' | 'warning', duracion: number = 3000): void {
        if (this.alertComponent) {
            this.destroyAlert();
        }

        const componentRef = createComponent(Alert, {
            environmentInjector: this.injector,
        });

        document.body.appendChild(componentRef.location.nativeElement);
        this.appRef.attachView(componentRef.hostView);

        this.alertComponent = componentRef;
        this.alertComponent.instance.mostrar(mensaje, tipo, duracion);
        
        const subscription = this.alertComponent.instance.closed.subscribe(() => {
            this.destroyAlert();
            subscription.unsubscribe();
        });
    }

    success(mensaje: string, duracion: number = 3000): void {
        this.mostrarAlerta(mensaje, 'success', duracion);
    }

    error(mensaje: string, duracion: number = 3000): void {
        this.mostrarAlerta(mensaje, 'error', duracion);
    }

    info(mensaje: string, duracion: number = 3000): void {
        this.mostrarAlerta(mensaje, 'info', duracion);
    }

    warning(mensaje: string, duracion: number = 3000): void {
        this.mostrarAlerta(mensaje, 'warning', duracion);
    }

    private destroyAlert(): void {
        if (this.alertComponent) {
            this.appRef.detachView(this.alertComponent.hostView);
            this.alertComponent.destroy();
            this.alertComponent = null;
        }
    }
}