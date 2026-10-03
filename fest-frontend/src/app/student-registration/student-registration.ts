import { Component, OnInit, ChangeDetectorRef, NgZone, ApplicationRef } from '@angular/core'; // 1. Import ApplicationRef
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-student-registration',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './student-registration.html',
  styleUrl: './student-registration.css'
})
export class StudentRegistrationComponent implements OnInit {
  student = { rollNumber: '', fullName: '', branch: '' };
  branches: string[] = [];

  isLoading = false;
  isFetchingBranches = true;
  successMessage = '';
  errorMessage = '';
  uniqueCode = '';

  // 2. Inject ApplicationRef in the constructor
  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
    private appRef: ApplicationRef // <--- Added here
  ) {}

  ngOnInit() {
    this.fetchBranches();
  }

  fetchBranches() {
    this.isFetchingBranches = true;

    this.http.get<string[]>('http://localhost:8080/api/public/departments').subscribe({
      next: (data) => {
        this.zone.run(() => {
          this.branches = data;
          this.isFetchingBranches = false;
          this.cdr.detectChanges();
          this.appRef.tick(); // Force immediate global re-paint
        });
      },
      error: (err) => {
        this.zone.run(() => {
          console.error('Failed to load branches', err);
          this.errorMessage = 'Could not load branches. Please check backend connection.';
          this.isFetchingBranches = false;
          this.cdr.detectChanges();
          this.appRef.tick();
        });
      }
    });
  }

  registerStudent() {
    if (!this.student.rollNumber || !this.student.fullName || !this.student.branch) {
      this.errorMessage = 'Please fill in all fields.';
      this.cdr.detectChanges();
      this.appRef.tick();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.detectChanges();
    this.appRef.tick(); // Force UI to show loading spinner instantly

    const payload = {
      rollNumber: this.student.rollNumber.trim(),
      fullName: this.student.fullName.trim(),
      branch: this.student.branch
    };

    this.http.post<any>('http://localhost:8080/api/public/register', payload).subscribe({
      next: (response) => {
        this.zone.run(() => {
          this.successMessage = response.message;
          this.uniqueCode = response.uniqueCode;
          this.isLoading = false;

          this.cdr.detectChanges();
          this.appRef.tick(); // FORCE IMMEDIATE RENDER OF THE TICKET SCREEN
        });
      },
      error: (err) => {
        this.zone.run(() => {
          this.errorMessage = err.error?.message || 'Not a valid student.';
          this.isLoading = false;
          this.cdr.detectChanges();
          this.appRef.tick();
        });
      }
    });
  }
}
