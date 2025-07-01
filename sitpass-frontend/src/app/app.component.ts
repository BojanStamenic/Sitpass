import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, CommonModule], // Uverite se da je CommonModule uključen
  template: `
    <nav class="flex items-center justify-between bg-gray-800 p-4 text-white">
      <div class="flex space-x-4">
        <a
          routerLink="/register"
          (click)="logNavigation('/register')"
          class="hover:underline"
          >Register</a
        >
        <a *ngIf="isLoggedIn" routerLink="/profile" class="hover:underline"
          >Profile</a
        >

        <a *ngIf="isLoggedIn" routerLink="/facilities" class="hover:underline"
          >Home</a
        >
        <a
          *ngIf="!isLoggedIn"
          routerLink="/login"
          (click)="logNavigation('/login')"
          class="hover:underline"
          >Login</a
        >
        <a
          *ngIf="isAdmin"
          routerLink="/admin"
          (click)="logNavigation('/admin')"
          class="hover:underline"
          >Admin</a
        >

        <a
          *ngIf="isAdmin"
          routerLink="/addFacility"
          (click)="logNavigation('/addFacility')"
          class="hover:underline"
          >Add facility</a
        >
      </div>

      <div>
        <button
          *ngIf="isLoggedIn"
          (click)="logout()"
          class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
        >
          Logout
        </button>
      </div>
    </nav>
    <router-outlet></router-outlet>
  `,
})
export class AppComponent implements OnInit {
  isLoggedIn = false;
  isAdmin = false;

  constructor(private router: Router) {}

  ngOnInit() {
    if (typeof window !== 'undefined') {
      this.isLoggedIn = !!localStorage.getItem('accessToken');
      this.isAdmin = localStorage.getItem('role') === 'ROLE_ADMIN';
      console.log(localStorage.getItem('role'));
      console.log('Access Token:', localStorage.getItem('accessToken'));
      console.log('Is Logged In:', this.isLoggedIn);
      console.log('Is Admin:', this.isAdmin);
      console.log(localStorage);
    }
  }

  logNavigation(route: string) {
    console.log('Navigacija na:', route);
  }

  logout() {
    // Ukloni sve podatke iz localStorage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');

    // Ažuriraj stanje
    this.isLoggedIn = false;
    this.isAdmin = false;

    // Preusmeri na login stranicu
    this.router.navigate(['/login']);
    console.log('User logged out and local storage cleared.');
  }
}
