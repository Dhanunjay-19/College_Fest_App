import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const authGuard = () => {
  const router = inject(Router);

  // Safely check if we are running in the browser before using localStorage
  let token: string | null = null;
  if (typeof window !== 'undefined' && window.localStorage) {
    token = localStorage.getItem('jwt_token');
  }

  if (token) {
    return true; // Allow access if token exists
  }

  // Otherwise, redirect to login
  router.navigate(['/login']);
  return false;
};
