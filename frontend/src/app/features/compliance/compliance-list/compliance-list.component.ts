import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-compliance-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, LoadingSpinnerComponent, StatusBadgeComponent],
  template: `
    <div class="page">
      <h1>{{ 'compliance.title' | translate }}</h1>
      <p class="subtitle">{{ 'compliance.select_system' | translate }}</p>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <div class="systems-grid">
          @for (system of systems; track system.id) {
            <a [routerLink]="['/compliance', system.id]" class="card system-card">
              <div class="system-header">
                <h3>{{ system.name }}</h3>
                @if (system.riskLevel) {
                  <app-status-badge [value]="system.riskLevel" type="risk" />
                }
              </div>
              <div class="system-score">
                <div class="score-bar">
                  <div class="score-fill" [style.width.%]="system.complianceScore"></div>
                </div>
                <span>{{ system.complianceScore }}%</span>
              </div>
            </a>
          }
        </div>

        @if (systems.length === 0) {
          <div class="card empty">
            <p>{{ 'compliance.no_obligations' | translate }}</p>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .subtitle { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .systems-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; }
    .system-card { text-decoration: none; color: inherit; cursor: pointer; }
    .system-card:hover { box-shadow: var(--shadow-md); }
    .system-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .system-header h3 { font-size: 1rem; }
    .system-score { display: flex; align-items: center; gap: 0.75rem; }
    .score-bar { flex: 1; height: 8px; background: var(--bg-tertiary); border-radius: 4px; }
    .score-fill { height: 100%; background: var(--primary); border-radius: 4px; transition: width 0.3s; }
    .system-score span { font-weight: 600; font-size: 0.875rem; min-width: 3rem; text-align: right; }
    .empty { text-align: center; padding: 2rem; color: var(--text-secondary); }
  `]
})
export class ComplianceListComponent implements OnInit {
  private api = inject(ApiService);
  systems: any[] = [];
  loading = true;

  ngOnInit(): void {
    this.api.getPaged<any>('/ai-systems', { size: 100 }).subscribe({
      next: res => {
        this.systems = (res.data?.content ?? []).filter((s: any) => s.riskLevel);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
