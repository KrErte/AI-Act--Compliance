import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-gpai-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, FormsModule, LoadingSpinnerComponent],
  template: `
    <div class="page-header">
      <h1>{{ 'gpai.title' | translate }}</h1>
      <button class="btn btn-primary" (click)="showForm = !showForm">{{ 'gpai.new_model' | translate }}</button>
    </div>

    @if (showForm) {
      <div class="card form-card">
        <h3>{{ 'gpai.new_model' | translate }}</h3>
        <div class="form-grid">
          <div class="form-group">
            <label>{{ 'gpai.model_name' | translate }}</label>
            <input type="text" [(ngModel)]="form.name" class="form-control">
          </div>
          <div class="form-group">
            <label>{{ 'gpai.provider' | translate }}</label>
            <input type="text" [(ngModel)]="form.provider" class="form-control">
          </div>
          <div class="form-group">
            <label>{{ 'gpai.model_type' | translate }}</label>
            <select [(ngModel)]="form.modelType" class="form-control">
              <option value="GENERAL">{{ 'gpai.types.GENERAL' | translate }}</option>
              <option value="FOUNDATION">{{ 'gpai.types.FOUNDATION' | translate }}</option>
              <option value="OPEN_SOURCE">{{ 'gpai.types.OPEN_SOURCE' | translate }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ 'gpai.systemic_risk' | translate }}</label>
            <label class="checkbox"><input type="checkbox" [(ngModel)]="form.hasSystemicRisk"> Yes</label>
          </div>
        </div>
        <div class="form-group" style="margin-top: 0.5rem;">
          <label>{{ 'common.description' | translate }}</label>
          <textarea [(ngModel)]="form.description" class="form-control" rows="3"></textarea>
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" (click)="createModel()" [disabled]="!form.name">{{ 'common.create' | translate }}</button>
          <button class="btn btn-secondary" (click)="showForm = false">{{ 'common.cancel' | translate }}</button>
        </div>
      </div>
    }

    @if (loading) {
      <app-loading-spinner />
    } @else if (models.length === 0) {
      <div class="card empty">
        <p>{{ 'gpai.no_models' | translate }}</p>
        <p class="text-muted">{{ 'gpai.no_models_desc' | translate }}</p>
      </div>
    } @else {
      <div class="model-grid">
        @for (model of models; track model.id) {
          <a [routerLink]="[model.id]" class="card model-card">
            <h3>{{ model.name }}</h3>
            <div class="model-meta">
              <span>{{ model.provider || '-' }}</span>
              <span class="type-badge">{{ model.modelType || 'GENERAL' }}</span>
              @if (model.hasSystemicRisk) {
                <span class="risk-badge">{{ 'gpai.detail.has_systemic_risk' | translate }}</span>
              }
            </div>
          </a>
        }
      </div>
    }
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.5rem; font-weight: 700; }
    .form-card { margin-bottom: 1.5rem; }
    .form-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8125rem; font-weight: 500; color: var(--text-secondary); }
    .form-control { padding: 0.5rem 0.75rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.875rem; }
    textarea.form-control { resize: vertical; }
    .form-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
    .checkbox { display: flex; align-items: center; gap: 0.5rem; font-size: 0.875rem; }
    .empty { text-align: center; padding: 3rem; }
    .text-muted { color: var(--text-muted); font-size: 0.875rem; }
    .model-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1rem; }
    .model-card { text-decoration: none; color: inherit; transition: border-color 0.15s; }
    .model-card:hover { border-color: var(--primary); }
    .model-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 0.5rem; }
    .model-meta { display: flex; gap: 0.5rem; font-size: 0.75rem; color: var(--text-secondary); align-items: center; }
    .type-badge { background: #f0f9ff; color: #0369a1; padding: 0.125rem 0.5rem; border-radius: 9999px; font-weight: 500; }
    .risk-badge { background: #fef2f2; color: #dc2626; padding: 0.125rem 0.5rem; border-radius: 9999px; font-weight: 500; }
  `]
})
export class GpaiListComponent implements OnInit {
  private api = inject(ApiService);
  models: any[] = [];
  loading = true;
  showForm = false;
  form: any = { name: '', provider: '', modelType: 'GENERAL', hasSystemicRisk: false, description: '' };

  ngOnInit(): void { this.loadModels(); }

  loadModels(): void {
    this.api.get<any[]>('/gpai-models').subscribe({
      next: res => { this.models = res.data || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  createModel(): void {
    this.api.post<any>('/gpai-models', this.form).subscribe({
      next: () => { this.showForm = false; this.form = { name: '', provider: '', modelType: 'GENERAL', hasSystemicRisk: false, description: '' }; this.loadModels(); }
    });
  }
}
