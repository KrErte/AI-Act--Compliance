import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslateModule],
  template: `
    <div class="auth-form">
      <h2>{{ 'auth.forgot_title' | translate }}</h2>
      <p class="subtitle">{{ 'auth.forgot_subtitle' | translate }}</p>

      @if (sent) {
        <div class="alert alert-success">{{ 'auth.reset_sent' | translate }}</div>
      } @else {
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>{{ 'auth.email' | translate }}</label>
            <input type="email" formControlName="email">
          </div>
          <button type="submit" class="btn btn-primary btn-block" [disabled]="loading">
            {{ loading ? ('common.loading' | translate) : ('auth.send_reset_link' | translate) }}
          </button>
        </form>
      }
      <p class="auth-switch">
        <a routerLink="/auth/login">{{ 'common.back' | translate }} {{ 'common.login' | translate }}</a>
      </p>
    </div>
  `,
  styles: [`
    .auth-form { max-width: 400px; }
    h2 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .subtitle { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .btn-block { width: 100%; justify-content: center; }
    .auth-switch { text-align: center; margin-top: 1.5rem; font-size: 0.875rem; }
    .alert-success { background: #dcfce7; color: #166534; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
  `]
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  loading = false;
  sent = false;

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;

    this.authService.forgotPassword(this.form.value.email).subscribe({
      next: () => { this.sent = true; this.loading = false; },
      error: () => { this.sent = true; this.loading = false; }
    });
  }
}
