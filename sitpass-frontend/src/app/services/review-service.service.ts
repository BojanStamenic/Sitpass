import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Review } from '../model/review.model';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = 'http://localhost:8080/SitPass/api/reviews'; // Update with your backend URL

  constructor(private http: HttpClient) {}

  getReviewsByFacilityId(facilityId: number): Observable<Review[]> {
    console.log(facilityId);
    return this.http
      .get<Review[]>(`${this.apiUrl}/facility/${facilityId}`, {
        headers: this.getAuthHeaders(),
      })
      .pipe(
        catchError((error) => {
          console.error('Error review:', error);
          return throwError(error);
        })
      );
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }
  // Add other methods if needed, such as addReview, updateReview, etc.
}
