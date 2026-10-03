import { Routes } from '@angular/router';

import { LoginComponent } from './login/login';
import { StudentRegistrationComponent } from './student-registration/student-registration';
import { VolunteerScannerComponent } from './volunteer-scanner/volunteer-scanner';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard';
import { authGuard } from './auth.guard'; // 1. Import your auth guard

export const routes: Routes = [
  { path: '', redirectTo: '/register', pathMatch: 'full' },          // Default redirect
  { path: 'register', component: StudentRegistrationComponent },      // Public registration
  { path: 'login', component: LoginComponent },                       // Public login

  // 2. Secured routes using canActivate guard
  {
    path: 'scanner',
    component: VolunteerScannerComponent,
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard]
  },

  { path: '**', redirectTo: '/register' }                             // Redirects unknown URLs to registration
];
