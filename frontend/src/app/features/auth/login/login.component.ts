import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  template: `
    <div class="auth-form">
      <h2>{{ 'auth.login_title' | translate }}</h2>
      <p class="subtitle">{{ 'auth.login_subtitle' | translate }}</p>

      @if (errorMessage) {
        <div class="alert alert-error">{{ errorMessage }}</div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label>{{ 'auth.email' | translate }}</label>
          <input type="email" formControlName="email" [class.is-invalid]="isInvalid('email')">
        </div>
        <div class="form-group">
          <label>{{ 'auth.password' | translate }}</label>
          <input type="password" formControlName="password" [class.is-invalid]="isInvalid('password')">
        </div>
        <div class="form-actions">
          <a routerLink="/auth/forgot-password" class="forgot-link">{{ 'auth.forgot_password' | translate }}</a>
        </div>
        <button type="submit" class="btn btn-primary btn-block" [disabled]="loading">
          {{ loading ? ('common.loading' | translate) : ('common.login' | translate) }}
        </button>
      </form>
      <p class="auth-switch">
        {{ 'auth.no_account' | translate }}
        <a routerLink="/auth/register">{{ 'common.sign_up' | translate }}</a>
      </p>
    </div>
  `,
  styles: [`
    .auth-form { max-width: 400px; }
    h2 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .subtitle { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .form-actions { display: flex; justify-content: flex-end; margin-bottom: 1rem; }
    .forgot-link { font-size: 0.875rem; }
    .btn-block { width: 100%; justify-content: center; }
    .auth-switch { text-align: center; margin-top: 1.5rem; font-size: 0.875rem; color: var(--text-secondary); }
    .alert-error { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  loading = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Login failed';
      }
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }
}
