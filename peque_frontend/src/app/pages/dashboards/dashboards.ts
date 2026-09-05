import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';
import { appConfig } from '../../config/appConfig';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboards',
  templateUrl: './dashboards.html',
  styleUrls: ['./dashboards.css'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class Dashboards implements OnInit {

  secciones = [
    { id: 'ventas', nombre: 'Ventas', icono: 'bi-graph-up' },
    { id: 'productos', nombre: 'Productos más vendidos', icono: 'bi-box-seam' },
    { id: 'horaPunta', nombre: 'Hora punta', icono: 'bi-clock-history' },
    { id: 'vendedores', nombre: 'Mejores vendedores', icono: 'bi-trophy' },
    { id: 'pagos', nombre: 'Métodos de pago', icono: 'bi-credit-card' },
    { id: 'asesor', nombre: 'Asesor IA', icono: 'bi-robot' }
  ];
  seccionActiva: string = 'ventas';

  resumen: any = { totalVentas: 0, totalIngresos: 0, ticketPromedio: 0, vendedoresActivos: 0, ventasHoy: 0, ingresosHoy: 0 };

  periodo: string = 'semana';
  mesSeleccionado: number = new Date().getMonth() + 1;
  anioSeleccionado: number = new Date().getFullYear();
  meses = [
    { value: 1, nombre: 'Enero' }, { value: 2, nombre: 'Febrero' }, { value: 3, nombre: 'Marzo' },
    { value: 4, nombre: 'Abril' }, { value: 5, nombre: 'Mayo' }, { value: 6, nombre: 'Junio' },
    { value: 7, nombre: 'Julio' }, { value: 8, nombre: 'Agosto' }, { value: 9, nombre: 'Septiembre' },
    { value: 10, nombre: 'Octubre' }, { value: 11, nombre: 'Noviembre' }, { value: 12, nombre: 'Diciembre' }
  ];
  anios: number[] = [];

  topVendedores: any[] = [];
  topProductos: any[] = [];
  horaPuntaResumen: string = '';

  asesorResumen: string = '';
  asesorRecomendaciones: any[] = [];
  asesorCargando: boolean = false;
  asesorError: string = '';
  asesorGenerado: boolean = false;

  cargando: boolean = false;
  private charts: { [key: string]: Chart } = {};
  private apiUrl = appConfig.apiUrl;

  private colores = {
    indigo: '#4f46e5', indigoSuave: 'rgba(79, 70, 229, 0.15)',
    esmeralda: '#10b981', esmeraldaSuave: 'rgba(16, 185, 129, 0.15)',
    ambar: '#f59e0b', rosa: '#f43f5e', cielo: '#0ea5e9', violeta: '#8b5cf6',
    gris: '#94a3b8'
  };

  constructor(private cdr: ChangeDetectorRef) {
    const anioActual = new Date().getFullYear();
    for (let i = anioActual - 5; i <= anioActual; i++) {
      this.anios.push(i);
    }
  }

  ngOnInit(): void {
    this.cargarResumen();
    this.cargarSeccion('ventas');
  }

  seleccionarSeccion(id: string): void {
    this.seccionActiva = id;
    this.cdr.detectChanges();
    this.cargarSeccion(id);
  }

  cargarSeccion(id: string): void {
    if (id === 'ventas') this.cargarVentas();
    if (id === 'productos') this.cargarTopProductos();
    if (id === 'horaPunta') this.cargarHoraPunta();
    if (id === 'vendedores') this.cargarTopVendedores();
    if (id === 'pagos') this.cargarDistribucion();
  }

  private get(url: string): Promise<any> {
    return fetch(this.apiUrl + url, {
      headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') }
    }).then(r => r.json());
  }

  generarAsesor(): void {
    this.asesorCargando = true;
    this.asesorError = '';
    this.cdr.detectChanges();
    this.get('api/reportes/asesor').then(data => {
      this.asesorCargando = false;
      this.asesorGenerado = true;
      if (data && data.error) {
        this.asesorError = data.error;
        this.asesorResumen = '';
        this.asesorRecomendaciones = [];
      } else {
        this.asesorResumen = data.resumen || '';
        this.asesorRecomendaciones = data.recomendaciones || [];
      }
      this.cdr.detectChanges();
    }).catch(() => {
      this.asesorCargando = false;
      this.asesorError = 'No se pudo generar el análisis en este momento.';
      this.cdr.detectChanges();
    });
  }

  cargarResumen(): void {
    this.get('api/reportes/resumen').then(data => {
      this.resumen = data;
      this.cdr.detectChanges();
    }).catch(() => {});
  }

  cargarVentas(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    let url = `api/reportes/ventas?periodo=${this.periodo}`;
    if (this.periodo === 'mes') {
      url += `&mes=${this.mesSeleccionado}&anio=${this.anioSeleccionado}`;
    }
    this.get(url).then(data => {
      this.cargando = false;
      this.cdr.detectChanges();
      this.dibujarVentas(data);
    }).catch(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  cargarTopProductos(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    this.get('api/reportes/top-productos?limite=8').then(data => {
      this.topProductos = data;
      this.cargando = false;
      this.cdr.detectChanges();
      this.dibujarTopProductos(data);
    }).catch(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  cargarHoraPunta(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    this.get('api/reportes/hora-punta').then(data => {
      this.cargando = false;
      const max = data.reduce((a: any, b: any) => (b.ventas > a.ventas ? b : a), data[0]);
      this.horaPuntaResumen = max && max.ventas > 0
        ? `La hora con más ventas es a las ${max.label} (${max.ventas} ventas)`
        : 'Aún no hay ventas registradas';
      this.cdr.detectChanges();
      this.dibujarHoraPunta(data);
    }).catch(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  cargarTopVendedores(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    this.get('api/reportes/top-vendedores').then(data => {
      this.topVendedores = data;
      this.cargando = false;
      this.cdr.detectChanges();
      this.dibujarTopVendedores(data);
    }).catch(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  cargarDistribucion(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    this.get('api/reportes/distribucion').then(data => {
      this.cargando = false;
      this.cdr.detectChanges();
      this.dibujarMetodosPago(data.metodosPago || []);
      this.dibujarComprobantes(data.comprobantes || []);
    }).catch(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  private dibujar(canvasId: string, config: any): void {
    setTimeout(() => {
      const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
      if (!canvas) return;
      if (this.charts[canvasId]) {
        this.charts[canvasId].destroy();
      }
      this.charts[canvasId] = new Chart(canvas, config);
    }, 0);
  }

  dibujarVentas(data: any[]): void {
    this.dibujar('chartVentas', {
      type: 'bar',
      data: {
        labels: data.map(d => d.label),
        datasets: [
          {
            type: 'line',
            label: 'Monto (S/)',
            data: data.map(d => d.monto),
            borderColor: this.colores.esmeralda,
            backgroundColor: this.colores.esmeraldaSuave,
            fill: true,
            tension: 0.35,
            yAxisID: 'yMonto',
            pointRadius: 3
          },
          {
            type: 'bar',
            label: 'N° de ventas',
            data: data.map(d => d.ventas),
            backgroundColor: this.colores.indigoSuave,
            borderColor: this.colores.indigo,
            borderWidth: 1.5,
            borderRadius: 6,
            yAxisID: 'yVentas'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } },
        scales: {
          yVentas: { beginAtZero: true, position: 'left', ticks: { precision: 0 } },
          yMonto: { beginAtZero: true, position: 'right', grid: { drawOnChartArea: false } }
        }
      }
    });
  }

  dibujarTopProductos(data: any[]): void {
    this.dibujar('chartProductos', {
      type: 'bar',
      data: {
        labels: data.map(d => d.nombre),
        datasets: [{
          label: 'Unidades vendidas',
          data: data.map(d => d.unidades),
          backgroundColor: [
            this.colores.indigo, this.colores.esmeralda, this.colores.ambar, this.colores.cielo,
            this.colores.violeta, this.colores.rosa, '#14b8a6', '#f97316'
          ],
          borderRadius: 6
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              afterLabel: (ctx: any) => 'Monto: S/ ' + (data[ctx.dataIndex]?.monto ?? 0)
            }
          }
        },
        scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
      }
    });
  }

  dibujarHoraPunta(data: any[]): void {
    const maxVentas = Math.max(...data.map(d => d.ventas));
    this.dibujar('chartHoraPunta', {
      type: 'bar',
      data: {
        labels: data.map(d => d.label),
        datasets: [{
          label: 'Ventas por hora (histórico)',
          data: data.map(d => d.ventas),
          backgroundColor: data.map(d =>
            d.ventas === maxVentas && maxVentas > 0 ? this.colores.ambar : this.colores.indigoSuave),
          borderColor: data.map(d =>
            d.ventas === maxVentas && maxVentas > 0 ? this.colores.ambar : this.colores.indigo),
          borderWidth: 1.5,
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
      }
    });
  }

  dibujarTopVendedores(data: any[]): void {
    this.dibujar('chartVendedores', {
      type: 'bar',
      data: {
        labels: data.map(d => d.nombre),
        datasets: [{
          label: 'Monto vendido (S/)',
          data: data.map(d => d.monto),
          backgroundColor: this.colores.esmeraldaSuave,
          borderColor: this.colores.esmeralda,
          borderWidth: 1.5,
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              afterLabel: (ctx: any) => 'Ventas: ' + (data[ctx.dataIndex]?.ventas ?? 0)
            }
          }
        },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  dibujarMetodosPago(data: any[]): void {
    this.dibujar('chartMetodos', {
      type: 'doughnut',
      data: {
        labels: data.map(d => `${d.nombre} (S/ ${d.monto})`),
        datasets: [{
          data: data.map(d => d.ventas),
          backgroundColor: [this.colores.esmeralda, this.colores.violeta, this.colores.cielo, this.colores.ambar],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }

  dibujarComprobantes(data: any[]): void {
    this.dibujar('chartComprobantes', {
      type: 'doughnut',
      data: {
        labels: data.map(d => d.nombre),
        datasets: [{
          data: data.map(d => d.ventas),
          backgroundColor: [this.colores.indigo, this.colores.rosa],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }
}
