import { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const HttpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'An unexpected error occurred';

      if (error.status === 0) {
        message = 'Cannot connect to the server. Please check your internet connection.';
      }
	  else if (error.status === 400) {
        message = 'Invalid request. Please check the submitted data.';
      }
	  else if (error.status === 401) {
        message = 'Invalid credentials';
      }
	  else if (error.status === 403) {
		message = 'Forbidden. You dont have access to this page'
	  }
	  else if (error.status === 409) {
        message = 'User already exists.';
      }
	  else if (error.error?.message) {
        message = error.error.message;
      }

      return throwError(() => new Error(message));
    })
  );
};