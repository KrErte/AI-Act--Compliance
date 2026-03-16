import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  template: `
    <div class="auth-form">
      <h2>{{ 'auth.reset_title' | translate }}</h2>
      <p class="subtitle">{{ 'auth.reset_subtitle' | translate }}</p>

      @if (success) {
        <div class="alert alert-success">{{ 'auth.reset_success' | translate }}</div>
        <a routerLink="/auth/login" class="btn btn-primary btn-block">{{ 'common.login' | translate }}</a>
      } @else {
        @if (errorMessage) {
          <div class="alert alert-error">{{ errorMessage }}</div>
        }
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>{{ 'auth.password' | translate }}</label>
            <input type="password" formControlName="password">
          </div>
          <div class="form-group">
            <label>{{ 'auth.confirm_password' | translate }}</label>
            <input type="password" formControlName="confirmPassword">
          </div>
          <button type="submit" class="btn btn-primary btn-block" [disabled]="loading">
            {{ loading ? ('common.loading' | translate) : ('auth.reset_password' | translate) }}
          </button>
        </form>
      }
    </div>
  `,
  styles: [`
    .auth-form { max-width: 400px; }
    h2 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .subtitle { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .btn-block { width: 100%; justify-content: center; text-align: center; }
    .alert-success { background: #dcfce7; color: #166534; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
    .alert-error { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  form: FormGroup = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  });

  token = '';
  loading = false;
  success = false;
  errorMessage = '';

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    if (this.form.value.password !== this.form.value.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    this.authService.resetPassword(this.token, this.form.value.password).subscribe({
      next: () => { this.success = true; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Reset failed';
      }
    });
  }
}
