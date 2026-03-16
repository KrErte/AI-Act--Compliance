import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

interface Obligation {
  id: string;
  articleRef: string;
  articleTitle: string;
  description: string;
  status: string;
  dueDate: string | null;
  notes: string | null;
  sortOrder: number;
}

@Component({
  selector: 'app-compliance-checklist',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LoadingSpinnerComponent],
  template: `
    @if (loading) {
      <app-loading-spinner />
    } @else {
      <div class="page">
        <a routerLink="/compliance" class="back-link">&larr; {{ 'compliance.title' | translate }}</a>
        <h1>{{ 'compliance.checklist_title' | translate }}</h1>

        <div class="progress-section card">
          <span>{{ 'compliance.progress' | translate }}:</span>
          <div class="progress-bar">
            <div class="progress-fill" [style.width.%]="progressPercent"></div>
          </div>
          <span class="progress-value">{{ completedCount }}/{{ obligations.length }}</span>
        </div>

        <div class="obligations">
          @for (obligation of obligations; track obligation.id) {
            <div class="card obligation-row" [class.completed]="obligation.status === 'COMPLETED'">
              <div class="obligation-header">
                <div class="article-ref">{{ obligation.articleRef }}</div>
                <select [ngModel]="obligation.status" (ngModelChange)="updateStatus(obligation, $event)">
                  <option value="NOT_STARTED">Not Started</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="NOT_APPLICABLE">N/A</option>
                </select>
              </div>
              <h3>{{ obligation.articleTitle }}</h3>
              @if (obligation.description) {
                <p class="obligation-desc">{{ obligation.description }}</p>
              }
            </div>
          }
        </div>

        @if (obligations.length === 0) {
          <div class="card empty">
            <p>{{ 'compliance.no_obligations' | translate }}</p>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .back-link { font-size: 0.875rem; color: var(--text-secondary); display: inline-block; margin-bottom: 0.5rem; }
    h1 { font-size: 1.5rem; margin-bottom: 1rem; }
    .progress-section { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; font-size: 0.875rem; }
    .progress-bar { flex: 1; height: 8px; background: var(--bg-tertiary); border-radius: 4px; }
    .progress-fill { height: 100%; background: var(--success); border-radius: 4px; transition: width 0.3s; }
    .progress-value { font-weight: 600; }
    .obligations { display: flex; flex-direction: column; gap: 0.75rem; }
    .obligation-row { transition: opacity 0.2s; }
    .obligation-row.completed { opacity: 0.6; }
    .obligation-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .article-ref { font-size: 0.75rem; font-weight: 600; color: var(--primary); background: var(--primary-light); padding: 0.125rem 0.5rem; border-radius: var(--radius-sm); }
    .obligation-header select { padding: 0.375rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.8125rem; }
    h3 { font-size: 0.9375rem; font-weight: 600; }
    .obligation-desc { font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.375rem; }
    .empty { text-align: center; padding: 2rem; color: var(--text-secondary); }
  `]
})
export class ComplianceChecklistComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);

  aiSystemId = '';
  obligations: Obligation[] = [];
  loading = true;

  get completedCount(): number {
    return this.obligations.filter(o => o.status === 'COMPLETED').length;
  }

  get progressPercent(): number {
    return this.obligations.length ? (this.completedCount / this.obligations.length) * 100 : 0;
  }

  ngOnInit(): void {
    this.aiSystemId = this.route.snapshot.paramMap.get('aiSystemId') ?? '';
    this.loadObligations();
  }

  loadObligations(): void {
    this.api.get<Obligation[]>(`/ai-systems/${this.aiSystemId}/obligations`).subscribe({
      next: res => { this.obligations = res.data ?? []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  updateStatus(obligation: Obligation, newStatus: string): void {
    this.api.patch<Obligation>(`/ai-systems/${this.aiSystemId}/obligations/${obligation.id}`, { status: newStatus })
      .subscribe({
        next: res => {
          if (res.data) {
            obligation.status = res.data.status;
          }
        }
      });
  }
}
