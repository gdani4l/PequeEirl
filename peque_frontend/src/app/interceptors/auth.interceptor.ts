import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);
    const token = localStorage.getItem('token');

    let request = req;
    if (token && !req.url.includes('/api/auth/') && !req.url.includes('apiperu')) {
        request = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
    }

    return next(request).pipe(
        catchError((error: HttpErrorResponse) => {
            if ((error.status === 401 || error.status === 403) && !req.url.includes('/api/auth/')) {
                localStorage.removeItem('usuario');
                localStorage.removeItem('token');
                router.navigate(['/login']);
            }
            return throwError(() => error);
        })
    );
};
