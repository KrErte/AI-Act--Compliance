import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-gpai-detail',
  standalone: true,
  imports: [RouterLink, TranslateModule, LoadingSpinnerComponent],
  template: `
    @if (loading) {
      <app-loading-spinner />
    } @else if (model) {
      <div class="detail-header">
        <div>
          <a routerLink="/gpai-models" class="back-link">&larr; {{ 'gpai.title' | translate }}</a>
          <h1>{{ model.name }}</h1>
        </div>
      </div>

      <div class="detail-grid">
        <div class="card">
          <h3>{{ 'gpai.detail.overview' | translate }}</h3>
          <div class="detail-info">
            <div class="info-row"><span class="label">{{ 'gpai.provider' | translate }}</span><span>{{ model.provider || '-' }}</span></div>
            <div class="info-row"><span class="label">{{ 'gpai.model_type' | translate }}</span><span>{{ model.modelType || '-' }}</span></div>
            <div class="info-row">
              <span class="label">{{ 'gpai.systemic_risk' | translate }}</span>
              <span [class]="model.hasSystemicRisk ? 'risk-yes' : 'risk-no'">
                {{ model.hasSystemicRisk ? ('gpai.detail.has_systemic_risk' | translate) : ('gpai.detail.no_systemic_risk' | translate) }}
              </span>
            </div>
          </div>
          @if (model.description) {
            <p class="description">{{ model.description }}</p>
          }
        </div>

        <div class="card">
          <h3>{{ 'gpai.obligations' | translate }}</h3>
          @if (obligations.length === 0) {
            <p class="text-muted">No obligations</p>
          } @else {
            <div class="obligations-list">
              @for (ob of obligations; track ob.id) {
                <div class="obligation-item">
                  <div class="ob-header">
                    <span class="ob-ref">{{ ob.articleRef }}</span>
                    <select class="status-select" [value]="ob.status" (change)="updateStatus(ob, $event)">
                      <option value="NOT_STARTED">Not Started</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="COMPLETED">Completed</option>
                    </select>
                  </div>
                  <div class="ob-title">{{ ob.title }}</div>
                  <div class="ob-desc">{{ ob.description }}</div>
                </div>
              }
            </div>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .detail-header { margin-bottom: 1.5rem; }
    .back-link { font-size: 0.875rem; color: var(--text-secondary); }
    h1 { font-size: 1.5rem; margin-top: 0.25rem; }
    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .detail-info { display: flex; flex-direction: column; gap: 0.75rem; }
    .info-row { display: flex; justify-content: space-between; align-items: center; }
    .label { color: var(--text-secondary); font-size: 0.875rem; }
    .risk-yes { color: #dc2626; font-weight: 500; }
    .risk-no { color: #16a34a; font-weight: 500; }
    .description { margin-top: 1rem; font-size: 0.875rem; color: var(--text-secondary); }
    .obligations-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .obligation-item { padding: 0.75rem; border: 1px solid var(--border); border-radius: var(--radius-sm); }
    .ob-header { display: flex; justify-content: space-between; align-items: center; }
    .ob-ref { font-size: 0.75rem; color: var(--primary); font-weight: 600; }
    .status-select { padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.75rem; }
    .ob-title { font-weight: 500; font-size: 0.875rem; margin-top: 0.25rem; }
    .ob-desc { font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.125rem; }
    .text-muted { color: var(--text-muted); font-size: 0.875rem; }
  `]
})
export class GpaiDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);

  model: any = null;
  obligations: any[] = [];
  loading = true;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.get<any>(`/gpai-models/${id}`).subscribe({
        next: res => { this.model = res.data; this.loading = false; this.loadObligations(id); },
        error: () => { this.loading = false; }
      });
    }
  }

  loadObligations(modelId: string): void {
    this.api.get<any[]>(`/gpai-models/${modelId}/obligations`).subscribe({
      next: res => { this.obligations = res.data || []; }
    });
  }

  updateStatus(ob: any, event: Event): void {
    const status = (event.target as HTMLSelectElement).value;
    this.api.patch(`/gpai-models/${this.model.id}/obligations/${ob.id}`, { status }).subscribe({
      next: res => { ob.status = status; }
    });
  }
}
