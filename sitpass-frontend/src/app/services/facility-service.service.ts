import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Facility } from '../model/facility.model';
import { Review } from '../model/review.model';

@Injectable({
  providedIn: 'root',
})
export class FacilityService {
  private apiUrl = 'http://localhost:8080/SitPass/api/facilities';

  constructor(private http: HttpClient) {}

  getFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(this.apiUrl, {
      headers: this.getAuthHeaders(),
    });
  }

  deleteFacility(id: number): Observable<Facility[]> {
    return this.http.delete<Facility[]>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getFacilityById(id: number): Observable<Facility> {
    return this.http.get<Facility>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  searchFacilities(query: string): Observable<Facility[]> {
    return this.http.get<Facility[]>(`${this.apiUrl}/search?q=${encodeURIComponent(query)}`, {
      headers: this.getAuthHeaders(),
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }

  addFacility(facility: Facility): Observable<Facility> {
    return this.http.post<Facility>(this.apiUrl, facility, {
      headers: this.getAuthHeaders(),
    });
  }

  editFacility(facilityId: number, facility: Facility): Observable<Facility> {
    return this.http.put<Facility>(`${this.apiUrl}/${facilityId}`, facility, {
      headers: this.getAuthHeaders(),
    });
  }

  addReview(review: Review): Observable<Review> {
    console.log(review);
    return this.http.post<Review>(
      `http://localhost:8080/SitPass/api/reviews`,
      review,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  submitReservation(reservationData: any): Observable<any> {
    return this.http.post(
      'http://localhost:8080/SitPass/api/exercises',
      reservationData,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  downloadFacilityPdf(facilityId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${facilityId}/pdf`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  uploadFacilityPdf(facilityId: number, file: File): Observable<Facility> {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('accessToken');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
    return this.http.post<Facility>(`${this.apiUrl}/${facilityId}/pdf`, formData, {
      headers,
    });
  }
}
