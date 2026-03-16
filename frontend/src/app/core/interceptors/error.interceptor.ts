import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
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

      console.error(`HTTP Error ${error.status}: ${message}`, error);
      return throwError(() => ({ ...error, userMessage: message }));
    })
  );
};
