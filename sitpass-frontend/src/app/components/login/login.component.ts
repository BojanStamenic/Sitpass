import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { CommonModule } from '@angular/common';

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

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    return;
  }

  onSubmit() {
    const credentials = { email: this.email, password: this.password };

    this.userService.login(credentials).subscribe(
      (response) => {
        const body = response.body;

        if (!body) {
          return;
        }

        const accessToken = body.accessToken;
        if (accessToken) {
          localStorage.setItem('accessToken', accessToken);
        }

        const email = body.email;
        if (email) {
          localStorage.setItem('userEmail', email);
        }

        this.userService.getUserRole().subscribe(
          (role) => {
            if (!role) {
              localStorage.removeItem('role');
            }
            window.location.assign('/facilities');
          },
          () => {
            localStorage.removeItem('role');
            window.location.assign('/facilities');
          }
        );
      },
      (error) => {
        console.error('Login failed', error);
        alert('Incorrect password or email!');
      }
    );
  }
}
