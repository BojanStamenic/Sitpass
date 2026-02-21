import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, CommonModule],
  template: `
    <header class="sticky top-0 z-50 border-b border-slate-800 bg-slate-900 text-slate-100 shadow-lg">
      <div class="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-3">
        <a
          routerLink="/facilities"
          class="text-xl font-semibold tracking-wide text-cyan-300"
          >SitPass</a
        >
        <nav class="flex flex-wrap items-center gap-2 text-sm">
          <a
            *ngIf="isLoggedIn"
            routerLink="/facilities"
            routerLinkActive="bg-slate-700 text-white"
            [routerLinkActiveOptions]="{ exact: true }"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Objekti</a
          >
          <a
            *ngIf="isLoggedIn"
            routerLink="/profile"
            routerLinkActive="bg-slate-700 text-white"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Profil</a
          >
          <a
            *ngIf="isAdmin"
            routerLink="/admin"
            routerLinkActive="bg-slate-700 text-white"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Admin</a
          >
          <a
            *ngIf="isAdmin"
            routerLink="/addFacility"
            routerLinkActive="bg-slate-700 text-white"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Dodaj objekat</a
          >
          <a
            *ngIf="!isLoggedIn"
            routerLink="/register"
            routerLinkActive="bg-slate-700 text-white"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Registracija</a
          >
          <a
            *ngIf="!isLoggedIn"
            routerLink="/login"
            routerLinkActive="bg-slate-700 text-white"
            class="rounded-md px-3 py-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
            >Login</a
          >
          <button
            *ngIf="isLoggedIn"
            (click)="logout()"
            class="ml-2 rounded-md bg-rose-600 px-3 py-2 font-medium text-white transition hover:bg-rose-500"
          >
            Logout
          </button>
        </nav>
      </div>
    </header>
    <main class="mx-auto w-full max-w-6xl p-4">
      <router-outlet></router-outlet>
    </main>
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
    }
  }

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('role');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');

    this.isLoggedIn = false;
    this.isAdmin = false;
    this.router.navigate(['/login']);
  }
}
