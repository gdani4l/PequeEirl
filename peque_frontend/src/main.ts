import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/interceptors/auth.interceptor';

const fetchOriginal = window.fetch;
window.fetch = (entrada: RequestInfo | URL, opciones: RequestInit = {}) => {
  const url =
    typeof entrada === 'string'
      ? entrada
      : entrada instanceof Request
        ? entrada.url
        : entrada.toString();
  if (url.includes('localhost:8080') && !url.includes('/api/auth/')) {
    const token = localStorage.getItem('token');
    if (token) {
      opciones = {
        ...opciones,
        headers: { ...(opciones.headers || {}), Authorization: `Bearer ${token}` },
      };
    }
  }
  return fetchOriginal(entrada, opciones).then((res) => {
    if ((res.status === 401 || res.status === 403) && url.includes('localhost:8080') && !url.includes('/api/auth/')) {
      localStorage.removeItem('usuario');
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return res;
  });
};

bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes), provideHttpClient(withInterceptors([authInterceptor]))],
}).catch((err) => console.error(err));
