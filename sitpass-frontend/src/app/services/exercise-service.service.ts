// exercise-service.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exercise } from '../model/exercise.model';

@Injectable({
  providedIn: 'root',
})
export class ExerciseService {
  private apiUrl = 'http://localhost:8080/SitPass/api/exercises'; // Primer URL-a

  constructor(private http: HttpClient) {}

  getExercisesByUserId(userId: number): Observable<Exercise[]> {
    return this.http.get<Exercise[]>(`${this.apiUrl}/user/${userId}`, {
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
}
