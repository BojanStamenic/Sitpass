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

  searchFacilities(
    query: string,
    minReviews?: number | null,
    maxReviews?: number | null
  ): Observable<Facility[]> {
    const params: string[] = [`q=${encodeURIComponent(query || '')}`];
    if (minReviews !== null && minReviews !== undefined) {
      params.push(`minReviews=${encodeURIComponent(String(minReviews))}`);
    }
    if (maxReviews !== null && maxReviews !== undefined) {
      params.push(`maxReviews=${encodeURIComponent(String(maxReviews))}`);
    }

    return this.http.get<Facility[]>(`${this.apiUrl}/search?${params.join('&')}`, {
      headers: this.getAuthHeaders(),
    });
  }

  advancedSearchFacilities(filters: {
    q?: string;
    nameQuery?: string;
    descriptionQuery?: string;
    pdfQuery?: string;
    minReviews?: number | null;
    maxReviews?: number | null;
    ratingCategory?: string;
    minCategoryRating?: number | null;
    maxCategoryRating?: number | null;
    operator?: 'AND' | 'OR';
    sortByName?: 'asc' | 'desc' | '';
  }): Observable<Facility[]> {
    const params: string[] = [];

    if (filters.q) params.push(`q=${encodeURIComponent(filters.q)}`);
    if (filters.nameQuery) params.push(`nameQuery=${encodeURIComponent(filters.nameQuery)}`);
    if (filters.descriptionQuery) params.push(`descriptionQuery=${encodeURIComponent(filters.descriptionQuery)}`);
    if (filters.pdfQuery) params.push(`pdfQuery=${encodeURIComponent(filters.pdfQuery)}`);
    if (filters.minReviews !== null && filters.minReviews !== undefined) {
      params.push(`minReviews=${encodeURIComponent(String(filters.minReviews))}`);
    }
    if (filters.maxReviews !== null && filters.maxReviews !== undefined) {
      params.push(`maxReviews=${encodeURIComponent(String(filters.maxReviews))}`);
    }
    if (filters.ratingCategory) params.push(`ratingCategory=${encodeURIComponent(filters.ratingCategory)}`);
    if (filters.minCategoryRating !== null && filters.minCategoryRating !== undefined) {
      params.push(`minCategoryRating=${encodeURIComponent(String(filters.minCategoryRating))}`);
    }
    if (filters.maxCategoryRating !== null && filters.maxCategoryRating !== undefined) {
      params.push(`maxCategoryRating=${encodeURIComponent(String(filters.maxCategoryRating))}`);
    }
    if (filters.operator) params.push(`operator=${encodeURIComponent(filters.operator)}`);
    if (filters.sortByName) params.push(`sortByName=${encodeURIComponent(filters.sortByName)}`);

    const queryString = params.length ? `?${params.join('&')}` : '';
    return this.http.get<Facility[]>(`${this.apiUrl}/search/advanced${queryString}`, {
      headers: this.getAuthHeaders(),
    });
  }

  moreLikeThis(facilityId: number, sortByName: 'asc' | 'desc' | '' = ''): Observable<Facility[]> {
    const query = sortByName ? `?sortByName=${encodeURIComponent(sortByName)}` : '';
    return this.http.get<Facility[]>(`${this.apiUrl}/search/more-like-this/${facilityId}${query}`, {
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
