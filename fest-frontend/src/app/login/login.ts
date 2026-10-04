import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h2>Fest Admin/Volunteer Login</h2>
        <input type="text" [(ngModel)]="username" placeholder="Username" />
        <input type="password" [(ngModel)]="password" placeholder="Password" />
        <button (click)="login()" [disabled]="isLoading">
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
        <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: #111827; }
    .login-card { background: white; padding: 2rem; border-radius: 8px; width: 300px; display: flex; flex-direction: column; gap: 1rem; }
    input { padding: 0.8rem; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: 0.8rem; background: #2563eb; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
    .error { color: red; text-align: center; margin: 0; font-size: 0.9rem; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(private http: HttpClient, private router: Router) {}

  login() {
    this.isLoading = true;
    this.errorMessage = '';

    // Update this URL to match your actual Spring Boot login endpoint
    this.http.post<any>('http://localhost:8080/api/auth/login', {
      username: this.username,
      password: this.password
    }).subscribe({
      next: (response) => {
        // Save the token to the browser's memory!
        localStorage.setItem('jwt_token', response.token);

        // Check role and redirect
        if (response.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/scanner']);
        }
      },
      error: () => {
        this.errorMessage = 'Invalid username or password';
        this.isLoading = false;
      }
    });
  }
}
