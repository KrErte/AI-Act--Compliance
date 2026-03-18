import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ToastService } from '../../shared/services/toast.service';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let toastServiceSpy: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'warning', 'info']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: ToastService, useValue: toastServiceSpy }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should pass through successful requests without showing a toast', () => {
    httpClient.get('/api/v1/test').subscribe(res => {
      expect(res).toBeTruthy();
    });

    const req = httpMock.expectOne('/api/v1/test');
    req.flush({ success: true });

    expect(toastServiceSpy.error).not.toHaveBeenCalled();
  });

  it('should show toast error on 500 server error', () => {
    httpClient.get('/api/v1/test').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/api/v1/test');
    req.flush({}, { status: 500, statusText: 'Internal Server Error' });

    expect(toastServiceSpy.error).toHaveBeenCalledWith('Server error. Please try again later');
  });

  it('should NOT show toast on 401 error', () => {
    httpClient.get('/api/v1/test').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/api/v1/test');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(toastServiceSpy.error).not.toHaveBeenCalled();
  });

  it('should show correct message for 403 forbidden error', () => {
    httpClient.get('/api/v1/test').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/api/v1/test');
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(toastServiceSpy.error).toHaveBeenCalledWith('You do not have permission to perform this action');
  });
});
