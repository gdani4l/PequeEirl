import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';
import { appConfig } from '../../config/appConfig';
import { Pago } from '../../components/pago/pago';

@Component({
  selector: 'app-ventas',
  templateUrl: './ventas.html',
  styleUrls: ['./ventas.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, Pago],
})
export class Ventas implements OnInit {
  productos: any[] = [];
  productosFiltrados: any[] = [];
  listaVenta: any[] = [];

  filtro = {
    nombre: '',
    id_categoria: 0,
    id_presentacion: 0,
  };

  idUsuario: number = 0;

  categorias: any[] = [];
  presentaciones: any[] = [];

  productoSeleccionado: any = null;
  cantidad: number = 1;

  subtotal: number = 0;
  igv: number = 0;
  total: number = 0;

  cargando: boolean = false;
  mostrarPago: boolean = false;

  private apiUrl = appConfig.apiUrl;

  constructor(
    private authService: AuthService,
    private alertService: AlertService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarPresentaciones();
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando = true;
    fetch(`${this.apiUrl}api/productos`)
      .then((response) => response.json())
      .then((data) => {
        this.productos = data;
        this.productosFiltrados = [...this.productos];
        this.cargando = false;
        this.cdr.detectChanges();
      })
      .catch((error) => {
        console.error('Error cargando productos:', error);
        this.cargando = false;
      });
  }

  cargarCategorias(): void {
    fetch(`${this.apiUrl}api/productos/categorias`)
      .then((response) => response.json())
      .then((data) => {
        this.categorias = data;
        this.cdr.detectChanges();
      })
      .catch((error) => console.error('Error cargando categorías:', error));
  }

  cargarPresentaciones(): void {
    fetch(`${this.apiUrl}api/productos/presentaciones`)
      .then((response) => response.json())
      .then((data) => {
        this.presentaciones = data;
        this.cdr.detectChanges();
      })
      .catch((error) => console.error('Error cargando presentaciones:', error));
  }

  getCategoriaNombre(p: any): string {
    if (p && p.categoria) {
      return p.categoria;
    }
    const cat = this.categorias.find((c) => Number(c.id_categoria) === Number(p?.id_categoria));
    return cat ? cat.nombre : '';
  }

  onFiltroChange(): void {
    let filtrados = [...this.productos];

    if (this.filtro.nombre && this.filtro.nombre.trim() !== '') {
      const termino = this.filtro.nombre.toLowerCase();
      filtrados = filtrados.filter((p) => p.nombre.toLowerCase().includes(termino));
    }

    const idCategoria = Number(this.filtro.id_categoria);
    if (idCategoria > 0) {
      filtrados = filtrados.filter((p) => Number(p.id_categoria) === idCategoria);
    }

    const idPresentacion = Number(this.filtro.id_presentacion);
    if (idPresentacion > 0) {
      filtrados = filtrados.filter((p) => Number(p.id_presentacion) === idPresentacion);
    }

    this.productosFiltrados = filtrados;
    this.cdr.detectChanges();
  }

  limpiarFiltros(): void {
    this.filtro = { nombre: '', id_categoria: 0, id_presentacion: 0 };
    this.productosFiltrados = [...this.productos];
    this.productoSeleccionado = null;
    this.cantidad = 1;
    this.cdr.detectChanges();
  }

  seleccionarProducto(producto: any): void {
    this.productoSeleccionado = producto;
    this.cantidad = 1;
  }

  onCantidadChange(): void {
    if (this.cantidad < 1) this.cantidad = 1;
    if (this.productoSeleccionado && this.cantidad > this.productoSeleccionado.stock) {
      this.cantidad = this.productoSeleccionado.stock;
    }
  }

  agregarALista(): void {
    if (!this.productoSeleccionado) {
      this.alertService.warning('Seleccione un producto');
      return;
    }

    if (this.cantidad <= 0) {
      this.alertService.warning('La cantidad debe ser mayor a 0');
      return;
    }

    if (this.cantidad > this.productoSeleccionado.stock) {
      this.alertService.warning(
        `Stock insuficiente. Stock disponible: ${this.productoSeleccionado.stock}`,
      );
      return;
    }

    const itemExistente = this.listaVenta.find(
      (item) => item.id_producto === this.productoSeleccionado.id_producto,
    );

    if (itemExistente) {
      const nuevaCantidad = itemExistente.cantidad + this.cantidad;
      if (nuevaCantidad > this.productoSeleccionado.stock) {
        this.alertService.warning(
          `Stock insuficiente. Stock disponible: ${this.productoSeleccionado.stock}`,
        );
        return;
      }
      itemExistente.cantidad = nuevaCantidad;
    } else {
      this.listaVenta.push({
        id_producto: this.productoSeleccionado.id_producto,
        nombre: this.productoSeleccionado.nombre,
        categoria: this.productoSeleccionado.categoria,
        presentacion: this.productoSeleccionado.presentacion,
        precio_unitario: this.productoSeleccionado.precio_unitario,
        cantidad: this.cantidad,
        stock: this.productoSeleccionado.stock,
        fecha_vencimiento: this.productoSeleccionado.fecha_vencimiento,
      });
    }

    this.calcularTotales();
    this.productoSeleccionado = null;
    this.cantidad = 1;
    this.cdr.detectChanges();
  }

  eliminarDeLista(index: number): void {
    this.listaVenta.splice(index, 1);
    this.calcularTotales();
    this.cdr.detectChanges();
  }

  actualizarCantidad(item: any, nuevaCantidad: number): void {
    if (nuevaCantidad <= 0) {
      const index = this.listaVenta.indexOf(item);
      if (index !== -1) this.eliminarDeLista(index);
      return;
    }
    if (nuevaCantidad > item.stock) {
      this.alertService.warning(`Stock insuficiente. Stock disponible: ${item.stock}`);
      return;
    }
    item.cantidad = nuevaCantidad;
    this.calcularTotales();
    this.cdr.detectChanges();
  }

  calcularTotales(): void {
    this.subtotal = this.listaVenta.reduce(
      (sum, item) => sum + item.precio_unitario * item.cantidad,
      0,
    );
    this.igv = this.subtotal * 0.18;
    this.total = this.subtotal + this.igv;
  }

  irAPagar(): void {
    if (this.listaVenta.length === 0) {
      this.alertService.warning('Agregue productos a la lista');
      return;
    }
    const usuario = this.authService.obtenerSesion();
    if (!usuario) {
      this.alertService.error('Debe iniciar sesión');
      return;
    }
    fetch(`${this.apiUrl}api/sesion/verificar`, {
      headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') },
    })
      .then((res) => {
        if (!res.ok) {
          return;
        }
        this.idUsuario = usuario.id;
        this.mostrarPago = true;
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.alertService.error('No se pudo verificar la sesión');
      });
  }

  confirmarPago(event: any): void {
    this.alertService.success('Pago procesado correctamente');
    this.mostrarPago = false;
    this.listaVenta = [];
    this.calcularTotales();
    this.cargarProductos();
    this.cdr.detectChanges();
  }
}