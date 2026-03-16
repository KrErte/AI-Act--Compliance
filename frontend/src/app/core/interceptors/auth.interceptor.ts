import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  // Skip auth header for public/auth endpoints
  if (req.url.includes('/auth/') || req.url.includes('/public/')) {
    return next(req);
  }

  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        if (!authService.isRefreshing) {
          authService.isRefreshing = true;
          return authService.refreshToken().pipe(
            switchMap(res => {
              authService.isRefreshing = false;
              if (res.success && res.data) {
                const retryReq = req.clone({
                  setHeaders: { Authorization: `Bearer ${res.data.accessToken}` }
                });
                return next(retryReq);
              }
              authService.logout();
              return throwError(() => error);
            }),
            catchError(refreshError => {
              authService.isRefreshing = false;
              authService.logout();
              return throwError(() => refreshError);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
};
