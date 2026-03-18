import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, provideRouter } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have email and password form controls', () => {
    expect(component.form.get('email')).toBeTruthy();
    expect(component.form.get('password')).toBeTruthy();
  });

  it('should have the form initially invalid', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('should have submit button disabled when form is empty', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('should call authService.login and navigate to /dashboard on success', () => {
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'token',
        refreshToken: 'refresh',
        user: { id: '1', email: 'test@example.com', firstName: 'Test', lastName: 'User', role: 'ADMIN', organizationId: 'org-1', organizationName: 'Test' }
      },
      timestamp: new Date().toISOString()
    };
    authServiceSpy.login.and.returnValue(of(mockResponse));

    component.form.setValue({ email: 'test@example.com', password: 'password123' });
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({ email: 'test@example.com', password: 'password123' });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should set error message on login failure', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({ error: { message: 'Invalid credentials' } }))
    );

    component.form.setValue({ email: 'test@example.com', password: 'wrong' });
    component.onSubmit();

    expect(component.errorMessage).toBe('Invalid credentials');
    expect(component.loading).toBeFalse();
  });
});
