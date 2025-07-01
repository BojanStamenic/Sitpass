import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = 'http://localhost:8080/SitPass/api';

  constructor(private http: HttpClient) {}

  // Check if the user is logged in by checking for a token
  isLoggedIn(): boolean {
    return (
      typeof window !== 'undefined' && !!localStorage.getItem('accessToken')
    );
  }

  // Get the user's role, assuming it's stored in local storage
  getUserRole(): Observable<string | null> {
    const email = localStorage.getItem('userEmail');

    if (!email) {
      return new Observable<string | null>((observer) => {
        observer.next(null);
        observer.complete();
      });
    }

    // Poziv API-ja za korisnika
    return this.getUserByEmail(email).pipe(
      map((user) => {
        console.log('Korisnik:', user); // Loguje ceo objekat korisnika
        if (user && user.authorities && user.authorities.length > 0) {
          console.log('ULOGA: ' + user.authorities[0].authority); // Loguje ulogu
          localStorage.setItem('role', user.authorities[0].authority); // Čuva ulogu u localStorage
          return user.authorities[0].authority; // Vraća npr. 'ROLE_ADMIN'
        }
        return null; // Ako korisnik nema uloge, vraća null
      }),
      catchError((error) => {
        console.error('Greška tokom preuzimanja korisnika:', error);
        return of(null); // Vraćamo `null` u slučaju greške
      })
    );
  }

  register(userData: any): Observable<any> {
    console.log('Registrujem korisnika:', userData);
    return this.http.post(`${this.apiUrl}/register`, userData).pipe(
      catchError((error) => {
        console.error('Greška prilikom registracije:', error);
        return throwError(error);
      })
    );
  }

  login(credentials: any): Observable<any> {
    console.log('RADIM LOGIN');
    return this.http
      .post(`${this.apiUrl}/login`, credentials, {
        observe: 'response',
      })
      .pipe(
        catchError((error) => {
          console.error('Greška prilikom logina:', error);
          return throwError(error);
        })
      );
  }

  private getAuthHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    if (typeof window !== 'undefined' && localStorage.getItem('accessToken')) {
      const token = localStorage.getItem('accessToken');
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  getUserByEmail(email: string): Observable<any> {
    console.log('Pozivam API za korisnika: ' + email);
    return this.http
      .get(`${this.apiUrl}/users/email/${email}`, {
        headers: this.getAuthHeaders(),
      })
      .pipe(
        catchError((error) => {
          console.error('Error fetching user profile:', error);
          return throwError(error);
        })
      );
  }

  updateUserProfile(profileData: any): Observable<any> {
    return this.http
      .put(`${this.apiUrl}/users/${profileData.id}`, profileData, {
        headers: this.getAuthHeaders(),
      })
      .pipe(
        catchError((error) => {
          console.error('Error updating user profile', error);
          return throwError(error);
        })
      );
  }

  changePassword(
    userId: number,
    oldPassword: string,
    newPassword: string
  ): Observable<any> {
    return this.http
      .put(
        `${this.apiUrl}/users/${userId}/change-password`,
        { oldPassword, newPassword },
        { headers: this.getAuthHeaders() }
      )
      .pipe(
        catchError((error) => {
          console.error('Error changing password', error);
          return throwError(error);
        })
      );
  }
}
