import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService, LoginRequest, RegisterRequest, AuthResponse, AuthUser } from './auth.service';
import { environment } from '@env';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  const API_URL = `${environment.apiUrl}/auth`;

  const mockUser: AuthUser = {
    id: 'user-1',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    role: 'ADMIN',
    organizationId: 'org-1',
    organizationName: 'Test Org'
  };

  const mockAuthResponse: AuthResponse = {
    accessToken: 'access-token-123',
    refreshToken: 'refresh-token-456',
    user: mockUser
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('login', () => {
    it('should POST to /auth/login and store auth data on success', () => {
      const request: LoginRequest = { email: 'test@example.com', password: 'password123' };

      service.login(request).subscribe(res => {
        expect(res.success).toBeTrue();
        expect(res.data).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${API_URL}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ success: true, data: mockAuthResponse, timestamp: new Date().toISOString() });

      expect(localStorage.getItem('access_token')).toBe('access-token-123');
      expect(localStorage.getItem('refresh_token')).toBe('refresh-token-456');
      expect(service.currentUser()).toEqual(mockUser);
    });

    it('should not store auth data when response is not successful', () => {
      const request: LoginRequest = { email: 'test@example.com', password: 'wrong' };

      service.login(request).subscribe(res => {
        expect(res.success).toBeFalse();
      });

      const req = httpMock.expectOne(`${API_URL}/login`);
      req.flush({ success: false, message: 'Invalid credentials', timestamp: new Date().toISOString() });

      expect(localStorage.getItem('access_token')).toBeNull();
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('register', () => {
    it('should POST to /auth/register and store auth data on success', () => {
      const request: RegisterRequest = {
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        organizationName: 'New Org'
      };

      service.register(request).subscribe(res => {
        expect(res.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${API_URL}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ success: true, data: mockAuthResponse, timestamp: new Date().toISOString() });

      expect(localStorage.getItem('access_token')).toBe('access-token-123');
      expect(service.currentUser()).toEqual(mockUser);
    });
  });

  describe('refreshToken', () => {
    it('should POST to /auth/refresh with the stored refresh token', () => {
      localStorage.setItem('refresh_token', 'old-refresh-token');

      service.refreshToken().subscribe(res => {
        expect(res.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${API_URL}/refresh`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
      req.flush({ success: true, data: mockAuthResponse, timestamp: new Date().toISOString() });

      expect(localStorage.getItem('access_token')).toBe('access-token-123');
    });
  });

  describe('forgotPassword', () => {
    it('should POST to /auth/forgot-password with email', () => {
      service.forgotPassword('test@example.com').subscribe();

      const req = httpMock.expectOne(`${API_URL}/forgot-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'test@example.com' });
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });

  describe('resetPassword', () => {
    it('should POST to /auth/reset-password with token and new password', () => {
      service.resetPassword('reset-token', 'newPassword123').subscribe();

      const req = httpMock.expectOne(`${API_URL}/reset-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ token: 'reset-token', password: 'newPassword123' });
      req.flush({ success: true, timestamp: new Date().toISOString() });
    });
  });

  describe('logout', () => {
    it('should clear localStorage, reset currentUser, and navigate to /auth/login', () => {
      localStorage.setItem('access_token', 'some-token');
      localStorage.setItem('refresh_token', 'some-refresh');
      localStorage.setItem('user', JSON.stringify(mockUser));

      service.logout();

      expect(localStorage.getItem('access_token')).toBeNull();
      expect(localStorage.getItem('refresh_token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
      expect(service.currentUser()).toBeNull();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
    });
  });

  describe('getAccessToken / getRefreshToken', () => {
    it('should return null when no tokens are stored', () => {
      expect(service.getAccessToken()).toBeNull();
      expect(service.getRefreshToken()).toBeNull();
    });

    it('should return stored tokens from localStorage', () => {
      localStorage.setItem('access_token', 'my-access');
      localStorage.setItem('refresh_token', 'my-refresh');

      expect(service.getAccessToken()).toBe('my-access');
      expect(service.getRefreshToken()).toBe('my-refresh');
    });
  });

  describe('isAuthenticated', () => {
    it('should return false when no user is set', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });

    it('should return true when a user is set', () => {
      service.currentUser.set(mockUser);
      expect(service.isAuthenticated()).toBeTrue();
    });
  });

  describe('hasRole / hasAnyRole', () => {
    it('should return true when user has the specified role', () => {
      service.currentUser.set(mockUser);
      expect(service.hasRole('ADMIN')).toBeTrue();
      expect(service.hasRole('USER')).toBeFalse();
    });

    it('should return true when user has any of the specified roles', () => {
      service.currentUser.set(mockUser);
      expect(service.hasAnyRole('ADMIN', 'USER')).toBeTrue();
      expect(service.hasAnyRole('USER', 'AUDITOR')).toBeFalse();
    });

    it('should return false when no user is set', () => {
      expect(service.hasRole('ADMIN')).toBeFalse();
      expect(service.hasAnyRole('ADMIN', 'USER')).toBeFalse();
    });
  });
});
