import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-volunteer-scanner',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './volunteer-scanner.html',
  styleUrl: './volunteer-scanner.css'
})
export class VolunteerScannerComponent {
  uniqueCode: string = '';

  scanResult: any = null;
  errorMessage: string | null = null;
  isProcessing: boolean = false;

  // NEW: Dynamically fetches the token from the browser's storage
  private get token(): string {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('jwt_token') || '';
    }
    return '';
  }

  // 2. INJECT ROUTER IN CONSTRUCTOR
  constructor(private http: HttpClient, private cdr: ChangeDetectorRef, private router: Router) {}

  // 3. ADD LOGOUT METHOD
  logout() {
    localStorage.removeItem('jwt_token');
    this.router.navigate(['/login']);
  }

  verifyCode() {
    if (!this.uniqueCode || this.uniqueCode.trim() === '') {
      return;
    }

    this.isProcessing = true;
    this.errorMessage = null;
    this.scanResult = null;
    this.cdr.detectChanges();

    const cleanCode = this.uniqueCode.trim().toUpperCase();

    // Uses the dynamic token
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.token}`);

    this.http.post<any>(`https://college-fest-app.onrender.com/api/volunteer/checkin/${cleanCode}`, {}, { headers }).subscribe({
      next: (response) => {
        this.scanResult = response;
        this.isProcessing = false;
        this.uniqueCode = ''; // Clear for the next student
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("FULL ERROR DETECTED:", err);
        // Extracts the best available error message
        this.errorMessage = err.error?.message || err.error?.status || err.message || 'INVALID CODE OR SERVER ERROR';
        this.scanResult = err.error; // Displays student details even on rejection if backend sends them
        this.isProcessing = false;
        this.uniqueCode = ''; // Clear for the next student
        this.cdr.detectChanges();
      }
    });
  }

  resetForm() {
    this.scanResult = null;
    this.errorMessage = null;
    this.uniqueCode = '';
    this.cdr.detectChanges();
  }
}
