import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard, roleGuard } from './auth.guard';

describe('authGuard', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['hasAnyRole'], {
      isAuthenticated: jasmine.createSpy('isAuthenticated'),
      currentUser: jasmine.createSpy('currentUser')
    });
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('should allow access when user is authenticated', () => {
    (authServiceMock.isAuthenticated as jasmine.Spy).and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('should deny access and redirect to /auth/login when not authenticated', () => {
    (authServiceMock.isAuthenticated as jasmine.Spy).and.returnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});

describe('roleGuard', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['hasAnyRole'], {
      isAuthenticated: jasmine.createSpy('isAuthenticated'),
      currentUser: jasmine.createSpy('currentUser')
    });
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('should allow access when user has the required role', () => {
    authServiceMock.hasAnyRole.and.returnValue(true);

    const guard = roleGuard('ADMIN', 'AUDITOR');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBeTrue();
    expect(authServiceMock.hasAnyRole).toHaveBeenCalledWith('ADMIN', 'AUDITOR');
  });

  it('should deny access and redirect to /dashboard when user lacks role', () => {
    authServiceMock.hasAnyRole.and.returnValue(false);

    const guard = roleGuard('ADMIN');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should work with a single role argument', () => {
    authServiceMock.hasAnyRole.and.returnValue(true);

    const guard = roleGuard('ADMIN');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBeTrue();
    expect(authServiceMock.hasAnyRole).toHaveBeenCalledWith('ADMIN');
  });
});
