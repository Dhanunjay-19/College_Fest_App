import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router'; // 1. IMPORT ROUTER

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboardComponent implements OnInit {
  stats = { totalEligible: 0, totalRegistered: 0, totalCheckedIn: 0 };
  selectedFile: File | null = null;

  isUploading = false;
  uploadMessage = '';
  isError = false;
  isDownloading = false;

  // NEW: Dynamically fetches the token from the browser's storage
  private get token(): string {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('jwt_token') || '';
    }
    return '';
  }

  // 2. INJECT ROUTER IN CONSTRUCTOR
  constructor(private http: HttpClient, private cdr: ChangeDetectorRef, private router: Router) {}

  ngOnInit() {
    this.fetchStats();
  }

  // 3. ADD LOGOUT METHOD
  logout() {
    localStorage.removeItem('jwt_token');
    this.router.navigate(['/login']);
  }

  fetchStats() {
    // Uses the dynamic token
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.token}`);

    this.http.get<any>('http://college-fest-app.onrender.com/api/admin/stats', { headers }).subscribe({
      next: (data) => {
        this.stats = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load stats', err)
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadMessage = '';
      this.cdr.detectChanges();
    }
  }

  uploadFile() {
    if (!this.selectedFile) {
      this.uploadMessage = 'Please select a file first.';
      this.isError = true;
      return;
    }

    this.isUploading = true;
    this.uploadMessage = '';
    this.isError = false;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    // Uses the dynamic token
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.token}`);

    this.http.post<any>('http://college-fest-app.onrender.com/api/admin/students/upload', formData, { headers }).subscribe({
      next: (response) => {
        this.uploadMessage = response.message || 'File uploaded successfully!';

        // Prevents duplicate upload successes from looking green
        if (this.uploadMessage.toLowerCase().includes('already uploaded') || this.uploadMessage.toLowerCase().includes('failed')) {
          this.isError = true;
        } else {
          this.isError = false;
          const fileInput = document.getElementById('fileUpload') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
          this.fetchStats();
        }

        this.isUploading = false;
        this.selectedFile = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.uploadMessage = err.error?.message || 'Failed to upload file.';
        this.isError = true;
        this.isUploading = false;
        this.cdr.detectChanges();
      }
    });
  }

  downloadReport() {
    this.isDownloading = true;
    this.cdr.detectChanges();

    // Uses the dynamic token
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.token}`);

    this.http.get('http://college-fest-app.onrender.com/api/admin/export', { headers, responseType: 'blob' }).subscribe({
      next: (blob) => {
        try {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'Fest_Master_Report.xlsx';
          document.body.appendChild(a);
          a.click();

          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        } catch (e) {
          console.error('Error saving file:', e);
        }
      },
      error: (err) => {
        console.error('Download failed', err);
        alert("Failed to download report. Check backend console.");
        this.isDownloading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        this.isDownloading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
