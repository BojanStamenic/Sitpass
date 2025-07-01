import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: true,
  imports: [FormsModule, CommonModule], // Removed HttpClientModule from imports
})
export class RegisterComponent {
  email = '';
  password = '';
  createdAt = new Date().toISOString().substring(0, 10); // Sets the current date
  address = '';
  status = 'PENDING';
  rejectionReason = '';

  constructor(private userService: UserService, private http: HttpClient) {}

  register() {
    const userData = {
      email: this.email,
      password: this.password,
      createdAt: this.createdAt,
      address: this.address,
      status: this.status,
      rejectionReason: this.rejectionReason,
    };

    this.http
      .post<any>('http://localhost:8080/SitPass/api/register', userData)
      .subscribe(
        (response) => {
          console.log('Registration successful', response);
          window.location.reload();
        },
        (error) => {
          console.log(userData);
          console.error('Registration failed', error);
        }
      );
  }
}
