import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/services/toast.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-organization',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, LoadingSpinnerComponent],
  template: `
    <div class="page">
      <h1>{{ 'organization.title' | translate }}</h1>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <div class="card">
          @if (successMessage) {
            <div class="alert alert-success">{{ successMessage }}</div>
          }
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <div class="form-group">
              <label>{{ 'organization.name' | translate }}</label>
              <input type="text" formControlName="name">
            </div>
            <div class="form-group">
              <label>{{ 'organization.industry' | translate }}</label>
              <input type="text" formControlName="industry">
            </div>
            <div class="form-group">
              <label>{{ 'organization.country' | translate }}</label>
              <input type="text" formControlName="country">
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              {{ saving ? ('common.loading' | translate) : ('common.save' | translate) }}
            </button>
          </form>
        </div>
      }
    </div>
  `,
  styles: [`
    .page h1 { font-size: 1.5rem; margin-bottom: 1.5rem; }
    .card { max-width: 600px; }
    .alert-success { background: #dcfce7; color: #166534; padding: 0.75rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem; }
  `]
})
export class OrganizationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private toast = inject(ToastService);

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    industry: [''],
    country: ['']
  });

  loading = true;
  saving = false;
  successMessage = '';

  ngOnInit(): void {
    this.api.get<any>('/organizations/me').subscribe({
      next: res => {
        if (res.data) {
          this.form.patchValue(res.data);
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving = true;
    this.successMessage = '';

    this.api.put<any>('/organizations/me', this.form.value).subscribe({
      next: () => {
        this.saving = false;
        this.toast.success('Organization updated successfully');
      },
      error: () => { this.saving = false; }
    });
  }
}
