import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../../shared/services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'An unexpected error occurred';

      if (error.error?.message) {
        message = error.error.message;
      } else if (error.status === 0) {
        message = 'Unable to connect to server';
      } else if (error.status === 403) {
        message = 'You do not have permission to perform this action';
      } else if (error.status === 404) {
        message = 'Resource not found';
      } else if (error.status >= 500) {
        message = 'Server error. Please try again later';
      }

      // Don't show toast for 401 (handled by auth interceptor redirect)
      if (error.status !== 401) {
        toast.error(message);
      }

      console.error(`HTTP Error ${error.status}: ${message}`, error);
      return throwError(() => ({ ...error, userMessage: message }));
    })
  );
};
