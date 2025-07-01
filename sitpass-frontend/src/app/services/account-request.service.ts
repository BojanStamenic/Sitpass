import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AccountRequestService {
  private apiUrl = 'http://localhost:8080/SitPass/api'; // Izmenite prema vašem URL-u

  constructor(private http: HttpClient) {}

  getAllAccountRequests(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/account-requests`, { headers });
  }

  acceptRequest(id: number, headers: HttpHeaders): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/approve/${id}`, {}, { headers });
  }

  rejectRequest(
    id: number,
    rejectionReason: string,
    headers: HttpHeaders
  ): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/admin/reject/${id}`,
      { rejectionReason },
      { headers }
    );
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }
}
