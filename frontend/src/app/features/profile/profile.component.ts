import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [TranslateModule, ReactiveFormsModule, LoadingSpinnerComponent],
  template: `
    <div class="profile-page">
      <div class="page-header">
        <h1>{{ 'profile.title' | translate }}</h1>
      </div>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <!-- Profile Info -->
        <div class="card">
          <h3>{{ 'profile.personal_info' | translate }}</h3>
          <form [formGroup]="profileForm" (ngSubmit)="saveProfile()">
            <div class="form-row">
              <div class="form-group">
                <label>{{ 'profile.first_name' | translate }}</label>
                <input type="text" formControlName="firstName" />
              </div>
              <div class="form-group">
                <label>{{ 'profile.last_name' | translate }}</label>
                <input type="text" formControlName="lastName" />
              </div>
            </div>
            <div class="form-group">
              <label>{{ 'profile.email' | translate }}</label>
              <input type="email" formControlName="email" readonly class="readonly" />
              <small class="help-text">{{ 'profile.email_readonly' | translate }}</small>
            </div>
            <div class="form-group">
              <label>{{ 'profile.language' | translate }}</label>
              <select formControlName="languagePreference">
                <option value="en">English</option>
                <option value="et">Eesti</option>
                <option value="fi">Suomi</option>
                <option value="lv">Latviešu</option>
                <option value="lt">Lietuvių</option>
                <option value="pl">Polski</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ 'profile.role' | translate }}</label>
              <input type="text" [value]="formatRole(authService.currentUser()?.role)" readonly class="readonly" />
            </div>

            @if (profileSuccess) {
              <div class="alert success">{{ 'profile.save_success' | translate }}</div>
            }

            <button type="submit" class="btn btn-primary" [disabled]="profileForm.invalid || saving">
              @if (saving) {
                {{ 'common.saving' | translate }}
              } @else {
                {{ 'common.save' | translate }}
              }
            </button>
          </form>
        </div>

        <!-- Change Password -->
        <div class="card">
          <h3>{{ 'profile.change_password' | translate }}</h3>
          <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
            <div class="form-group">
              <label>{{ 'profile.current_password' | translate }}</label>
              <input type="password" formControlName="currentPassword" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>{{ 'profile.new_password' | translate }}</label>
                <input type="password" formControlName="newPassword" />
                @if (passwordForm.get('newPassword')?.errors?.['minlength'] && passwordForm.get('newPassword')?.touched) {
                  <span class="error-message">{{ 'profile.password_min_length' | translate }}</span>
                }
              </div>
              <div class="form-group">
                <label>{{ 'profile.confirm_password' | translate }}</label>
                <input type="password" formControlName="confirmPassword" />
              </div>
            </div>

            @if (passwordMismatch) {
              <div class="alert error">{{ 'profile.password_mismatch' | translate }}</div>
            }
            @if (passwordSuccess) {
              <div class="alert success">{{ 'profile.password_success' | translate }}</div>
            }
            @if (passwordError) {
              <div class="alert error">{{ passwordError }}</div>
            }

            <button type="submit" class="btn btn-primary" [disabled]="passwordForm.invalid || savingPassword">
              @if (savingPassword) {
                {{ 'common.saving' | translate }}
              } @else {
                {{ 'profile.update_password' | translate }}
              }
            </button>
          </form>
        </div>

        <!-- Account Info -->
        <div class="card">
          <h3>{{ 'profile.account_info' | translate }}</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">{{ 'profile.organization' | translate }}</span>
              <span class="info-value">{{ authService.currentUser()?.organizationName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ 'profile.user_id' | translate }}</span>
              <span class="info-value mono">{{ authService.currentUser()?.id }}</span>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; }
    }
    .card { margin-bottom: 1.5rem; }
    .card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .readonly {
      background: var(--bg-tertiary) !important;
      color: var(--text-secondary) !important;
      cursor: not-allowed;
    }
    .help-text { font-size: 0.75rem; color: var(--text-muted); margin-top: 0.25rem; display: block; }
    .alert {
      padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem;
      font-size: 0.875rem;
      &.success { background: #dcfce7; color: #166534; }
      &.error { background: #fee2e2; color: #991b1b; }
    }
    .info-grid { display: flex; flex-direction: column; gap: 1rem; }
    .info-item { display: flex; flex-direction: column; gap: 0.25rem; }
    .info-label { font-size: 0.8125rem; color: var(--text-secondary); font-weight: 500; }
    .info-value { font-size: 0.875rem; }
    .mono { font-family: monospace; font-size: 0.8125rem; color: var(--text-muted); }
    @media (max-width: 768px) {
      .form-row { grid-template-columns: 1fr; }
    }
  `]
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  authService = inject(AuthService);

  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  loading = true;
  saving = false;
  savingPassword = false;
  profileSuccess = false;
  passwordSuccess = false;
  passwordMismatch = false;
  passwordError = '';

  ngOnInit(): void {
    const user = this.authService.currentUser();

    this.profileForm = this.fb.group({
      firstName: [user?.firstName || '', Validators.required],
      lastName: [user?.lastName || '', Validators.required],
      email: [user?.email || ''],
      languagePreference: ['en']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    });

    this.api.get<any>('/users/me').subscribe({
      next: res => {
        if (res.data) {
          this.profileForm.patchValue({
            firstName: res.data.firstName,
            lastName: res.data.lastName,
            email: res.data.email,
            languagePreference: res.data.languagePreference || 'en'
          });
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;
    this.saving = true;
    this.profileSuccess = false;

    const { email, ...data } = this.profileForm.value;
    this.api.put('/users/me', data).subscribe({
      next: () => {
        this.saving = false;
        this.profileSuccess = true;
        setTimeout(() => this.profileSuccess = false, 3000);
      },
      error: () => { this.saving = false; }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;
    this.passwordMismatch = false;
    this.passwordError = '';
    this.passwordSuccess = false;

    if (newPassword !== confirmPassword) {
      this.passwordMismatch = true;
      return;
    }

    this.savingPassword = true;
    this.api.post('/auth/change-password', { currentPassword, newPassword }).subscribe({
      next: () => {
        this.savingPassword = false;
        this.passwordSuccess = true;
        this.passwordForm.reset();
        setTimeout(() => this.passwordSuccess = false, 3000);
      },
      error: (err: any) => {
        this.savingPassword = false;
        this.passwordError = err?.error?.message || 'Failed to change password';
      }
    });
  }

  formatRole(role: string | null | undefined): string {
    if (!role) return '';
    return role.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  }
}
