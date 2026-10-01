import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('itc_token');
  const isPublicEndpoint = request.url.startsWith('/api/auth/') || request.url.startsWith('/api/setup/');
  if (!token || !request.url.startsWith('/api') || isPublicEndpoint) return next(request);

  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  })).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401) {
      localStorage.removeItem('itc_token');
      localStorage.removeItem('itc_profile');
    }
    return throwError(() => error);
  }));
};