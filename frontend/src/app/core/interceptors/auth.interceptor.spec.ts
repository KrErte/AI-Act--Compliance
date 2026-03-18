import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';
import { of, throwError } from 'rxjs';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj('AuthService', [
      'getAccessToken',
      'getRefreshToken',
      'refreshToken',
      'logout'
    ], {
      isRefreshing: false
    });

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header when token exists', () => {
    authServiceMock.getAccessToken.and.returnValue('my-token');

    httpClient.get('/api/v1/dashboard').subscribe();

    const req = httpMock.expectOne('/api/v1/dashboard');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-token');
    req.flush({});
  });

  it('should skip Authorization header for /auth/ URLs', () => {
    authServiceMock.getAccessToken.and.returnValue('my-token');

    httpClient.get('/api/v1/auth/login').subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should skip Authorization header for /public/ URLs', () => {
    authServiceMock.getAccessToken.and.returnValue('my-token');

    httpClient.get('/api/v1/public/classify').subscribe();

    const req = httpMock.expectOne('/api/v1/public/classify');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should attempt token refresh on 401 error', () => {
    authServiceMock.getAccessToken.and.returnValue('expired-token');
    Object.defineProperty(authServiceMock, 'isRefreshing', {
      get: () => false,
      set: () => {},
      configurable: true
    });
    authServiceMock.refreshToken.and.returnValue(of({
      success: true,
      data: { accessToken: 'new-token', refreshToken: 'new-refresh', user: {} as any },
      timestamp: new Date().toISOString()
    }));

    httpClient.get('/api/v1/dashboard').subscribe();

    const req = httpMock.expectOne('/api/v1/dashboard');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    // After refresh, the interceptor retries with the new token
    const retryReq = httpMock.expectOne('/api/v1/dashboard');
    expect(retryReq.request.headers.get('Authorization')).toBe('Bearer new-token');
    retryReq.flush({ success: true });
  });

  it('should call logout on refresh failure', () => {
    authServiceMock.getAccessToken.and.returnValue('expired-token');
    Object.defineProperty(authServiceMock, 'isRefreshing', {
      get: () => false,
      set: () => {},
      configurable: true
    });
    authServiceMock.refreshToken.and.returnValue(
      throwError(() => new Error('Refresh failed'))
    );

    httpClient.get('/api/v1/dashboard').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/api/v1/dashboard');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(authServiceMock.logout).toHaveBeenCalled();
  });
});
