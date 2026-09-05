import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ApiPeruService {
    private baseUrl = 'https://apiperu.dev/api';
    private token = '22622654cba706157f55174f5a3fb5c65685edeab04803de966766cda91ba720';

    constructor(private http: HttpClient) {}

    buscarDNI(dni: string): Observable<any> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.token}`
        });
        return this.http.get(`${this.baseUrl}/dni/${dni}`, { headers });
    }

    buscarRUC(ruc: string): Observable<any> {
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${this.token}`
        });
        return this.http.get(`${this.baseUrl}/ruc/${ruc}`, { headers });
    }
}