import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  template: `
    <div class="auth-form">
      <h2>{{ 'auth.register_title' | translate }}</h2>
      <p class="subtitle">{{ 'auth.register_subtitle' | translate }}</p>

      @if (errorMessage) {
        <div class="alert alert-error">{{ errorMessage }}</div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="form-row">
          <div class="form-group">
            <label>{{ 'auth.first_name' | translate }}</label>
            <input type="text" formControlName="firstName">
          </div>
          <div class="form-group">
            <label>{{ 'auth.last_name' | translate }}</label>
            <input type="text" formControlName="lastName">
          </div>
        </div>
        <div class="form-group">
          <label>{{ 'auth.organization_name' | translate }}</label>
          <input type="text" formControlName="organizationName">
        </div>
        <div class="form-group">
          <label>{{ 'auth.email' | translate }}</label>
          <input type="email" formControlName="email">
        </div>
        <div class="form-group">
          <label>{{ 'auth.password' | translate }}</label>
          <input type="password" formControlName="password">
        </div>
        <button type="submit" class="btn btn-primary btn-block" [disabled]="loading">
          {{ loading ? ('common.loading' | translate) : ('common.sign_up' | translate) }}
        </button>
      </form>
      <p class="auth-switch">
        {{ 'auth.have_account' | translate }}
        <a routerLink="/auth/login">{{ 'common.login' | translate }}</a>
      </p>
    </div>
  `,
  styles: [`
    .auth-form { max-width: 400px; }
    h2 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .subtitle { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .btn-block { width: 100%; justify-content: center; }
    .auth-switch { text-align: center; margin-top: 1.5rem; font-size: 0.875rem; color: var(--text-secondary); }
    .alert-error { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    organizationName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  loading = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.form.value).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed';
      }
    });
  }
}
