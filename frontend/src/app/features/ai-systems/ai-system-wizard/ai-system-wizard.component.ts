import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-ai-system-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule],
  template: `
    <div class="wizard">
      <div class="wizard-steps">
        @for (step of steps; track step; let i = $index) {
          <div class="step" [class.active]="currentStep === i" [class.completed]="currentStep > i">
            <span class="step-number">{{ i + 1 }}</span>
            <span class="step-label">{{ step | translate }}</span>
          </div>
        }
      </div>

      <div class="card wizard-content">
        <form [formGroup]="form">
          @switch (currentStep) {
            @case (0) {
              <h2>{{ 'ai_systems.wizard.basic_info' | translate }}</h2>
              <div class="form-group">
                <label>{{ 'ai_systems.wizard.system_name' | translate }} *</label>
                <input type="text" formControlName="name">
              </div>
              <div class="form-group">
                <label>{{ 'ai_systems.wizard.system_description' | translate }}</label>
                <textarea formControlName="description" rows="3"></textarea>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>{{ 'ai_systems.wizard.vendor' | translate }}</label>
                  <input type="text" formControlName="vendor">
                </div>
                <div class="form-group">
                  <label>{{ 'ai_systems.wizard.version' | translate }}</label>
                  <input type="text" formControlName="version">
                </div>
              </div>
            }
            @case (1) {
              <h2>{{ 'ai_systems.wizard.deployment' | translate }}</h2>
              <div class="form-group">
                <label>{{ 'ai_systems.wizard.deployment_context' | translate }}</label>
                <select formControlName="deploymentContext">
                  <option value="">-- Select --</option>
                  <option value="INTERNAL">Internal</option>
                  <option value="CUSTOMER_FACING">Customer Facing</option>
                  <option value="EMBEDDED">Embedded</option>
                  <option value="STANDALONE">Standalone</option>
                </select>
              </div>
              <div class="form-group">
                <label>{{ 'ai_systems.wizard.purpose' | translate }}</label>
                <textarea formControlName="purpose" rows="3"></textarea>
              </div>
            }
            @case (2) {
              <h2>{{ 'ai_systems.wizard.role' | translate }}</h2>
              <div class="form-group">
                <label>{{ 'ai_systems.wizard.organization_role' | translate }}</label>
                <select formControlName="organizationRole">
                  <option value="">-- Select --</option>
                  <option value="PROVIDER">Provider (develop/place on market)</option>
                  <option value="DEPLOYER">Deployer (use under own authority)</option>
                  <option value="BOTH">Both</option>
                </select>
              </div>
            }
            @case (3) {
              <h2>{{ 'ai_systems.wizard.summary' | translate }}</h2>
              <div class="summary">
                <div class="summary-row">
                  <strong>{{ 'ai_systems.wizard.system_name' | translate }}:</strong>
                  <span>{{ form.value.name }}</span>
                </div>
                <div class="summary-row">
                  <strong>{{ 'ai_systems.wizard.vendor' | translate }}:</strong>
                  <span>{{ form.value.vendor || '-' }}</span>
                </div>
                <div class="summary-row">
                  <strong>{{ 'ai_systems.wizard.deployment_context' | translate }}:</strong>
                  <span>{{ form.value.deploymentContext || '-' }}</span>
                </div>
                <div class="summary-row">
                  <strong>{{ 'ai_systems.wizard.organization_role' | translate }}:</strong>
                  <span>{{ form.value.organizationRole || '-' }}</span>
                </div>
              </div>
            }
          }
        </form>

        <div class="wizard-actions">
          @if (currentStep > 0) {
            <button class="btn btn-secondary" (click)="prev()">{{ 'common.back' | translate }}</button>
          }
          @if (currentStep < 3) {
            <button class="btn btn-primary" (click)="next()" [disabled]="!canProceed()">
              {{ 'common.next' | translate }}
            </button>
          } @else {
            <button class="btn btn-primary" (click)="submit()" [disabled]="loading">
              {{ loading ? ('common.loading' | translate) : ('common.create' | translate) }}
            </button>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .wizard-steps {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
    }
    .step {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border-radius: var(--radius-md);
      font-size: 0.875rem;
      color: var(--text-muted);
      background: var(--bg-tertiary);
    }
    .step.active { background: var(--primary); color: white; }
    .step.completed { background: var(--success); color: white; }
    .step-number { font-weight: 700; }
    h2 { font-size: 1.25rem; margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .summary { display: flex; flex-direction: column; gap: 0.75rem; }
    .summary-row { display: flex; gap: 1rem; }
    .summary-row strong { min-width: 160px; }
    .wizard-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border); }
  `]
})
export class AiSystemWizardComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);

  steps = [
    'ai_systems.wizard.basic_info',
    'ai_systems.wizard.deployment',
    'ai_systems.wizard.role',
    'ai_systems.wizard.summary'
  ];

  currentStep = 0;
  loading = false;

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    vendor: [''],
    version: [''],
    purpose: [''],
    deploymentContext: [''],
    organizationRole: ['']
  });

  next(): void {
    if (this.canProceed()) this.currentStep++;
  }

  prev(): void {
    if (this.currentStep > 0) this.currentStep--;
  }

  canProceed(): boolean {
    if (this.currentStep === 0) return this.form.get('name')?.valid ?? false;
    return true;
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;

    const value = { ...this.form.value };
    if (!value.deploymentContext) delete value.deploymentContext;
    if (!value.organizationRole) delete value.organizationRole;

    this.api.post<any>('/ai-systems', value).subscribe({
      next: res => {
        if (res.data?.id) {
          this.router.navigate(['/ai-systems', res.data.id]);
        }
      },
      error: () => { this.loading = false; }
    });
  }
}
