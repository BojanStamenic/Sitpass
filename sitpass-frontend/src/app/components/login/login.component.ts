import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [FormsModule, HttpClientModule, CommonModule],
})
export class LoginComponent {
  email = '';
  password = '';

  constructor(private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    return;
  }

  onSubmit() {
    const credentials = { email: this.email, password: this.password };
    console.log('lalala' + credentials);
    this.userService.login(credentials).subscribe(
      (response) => {
        const body = response.body;

        if (body) {
          // Čuvanje access token-a i podataka u localStorage
          const accessToken = body.accessToken; // Pristupanje token-u iz tela odgovora
          if (accessToken) {
            localStorage.setItem('accessToken', accessToken);
            console.log('Access token sačuvan:', accessToken);
          }

          // Sačuvaj ostale podatke, ako ih imaš
          const email = body.email; // Pretpostavljamo da telo odgovora sadrži email
          if (email) {
            localStorage.setItem('userEmail', email);
            console.log('Email sačuvan:', email);
          }

          this.userService.getUserRole().subscribe(
            (role) => {
              console.log('Korisnička uloga:', role); // Proverite da li se uloga vraća
            },
            (error) => {
              console.error('Greška pri pozivanju API-ja:', error);
            }
          );

          // Ažuriranje stanja i preusmeravanje
          // Preusmeravanje nakon uspešne prijave

          window.location.reload();
        }
      },
      (error) => {
        console.error('Login failed', error);
        alert('Incorrect password or email!');
      }
    );
  }
}
