import { Component, OnInit, inject, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

export interface DashboardSummary {
  totalAiSystems: number;
  riskDistribution: Record<string, number>;
  overallComplianceScore: number;
  obligationCounts: { total: number; completed: number; inProgress: number; notStarted: number };
  upcomingDeadlines: Array<{ title: string; date: string; aiSystemName: string }>;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [TranslateModule, RouterLink, LoadingSpinnerComponent, StatusBadgeComponent],
  template: `
    <div class="dashboard">
      <div class="dashboard-header">
        <h1>{{ 'dashboard.title' | translate }}</h1>
        <a routerLink="/ai-systems/new" class="btn btn-primary">
          <span class="material-icons-outlined">add</span>
          {{ 'dashboard.add_ai_system' | translate }}
        </a>
      </div>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <!-- Stat Cards -->
        <div class="stats-grid">
          <div class="card stat-card stat-systems">
            <div class="stat-icon">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ summary?.totalAiSystems ?? 0 }}</div>
              <div class="stat-label">{{ 'dashboard.total_ai_systems' | translate }}</div>
            </div>
          </div>
          <div class="card stat-card stat-compliance">
            <div class="stat-icon">
              <span class="material-icons-outlined">verified</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ summary?.overallComplianceScore ?? 0 }}%</div>
              <div class="stat-label">{{ 'dashboard.compliance_score' | translate }}</div>
            </div>
            <div class="stat-progress">
              <div class="progress-bar">
                <div class="progress-fill" [style.width.%]="summary?.overallComplianceScore ?? 0"
                     [class.low]="(summary?.overallComplianceScore ?? 0) < 33"
                     [class.medium]="(summary?.overallComplianceScore ?? 0) >= 33 && (summary?.overallComplianceScore ?? 0) < 66"
                     [class.high]="(summary?.overallComplianceScore ?? 0) >= 66"></div>
              </div>
            </div>
          </div>
          <div class="card stat-card stat-obligations">
            <div class="stat-icon">
              <span class="material-icons-outlined">checklist</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ summary?.obligationCounts?.completed ?? 0 }}<span class="stat-total">/{{ summary?.obligationCounts?.total ?? 0 }}</span>
              </div>
              <div class="stat-label">{{ 'dashboard.obligations_completed' | translate }}</div>
            </div>
          </div>
          <div class="card stat-card stat-highrisk">
            <div class="stat-icon">
              <span class="material-icons-outlined">warning</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ summary?.riskDistribution?.['HIGH'] ?? 0 }}</div>
              <div class="stat-label">{{ 'dashboard.high_risk_systems' | translate }}</div>
            </div>
          </div>
        </div>

        <!-- Charts Row -->
        <div class="dashboard-grid">
          <div class="card chart-card">
            <h3>{{ 'dashboard.risk_distribution' | translate }}</h3>
            <div class="chart-wrapper">
              <canvas #riskChart></canvas>
            </div>
            <div class="risk-legend">
              @for (entry of riskEntries; track entry[0]) {
                <div class="legend-item">
                  <app-status-badge [value]="entry[0]" type="risk" />
                  <span class="risk-count">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </div>

          <div class="card">
            <h3>{{ 'dashboard.upcoming_deadlines' | translate }}</h3>
            @if (summary?.upcomingDeadlines?.length) {
              @for (deadline of summary!.upcomingDeadlines; track deadline.title) {
                <div class="deadline-row">
                  <div class="deadline-info">
                    <strong>{{ deadline.title }}</strong>
                    <span class="deadline-system">{{ deadline.aiSystemName }}</span>
                  </div>
                  <span class="deadline-date" [class.overdue]="isOverdue(deadline.date)">
                    <span class="material-icons-outlined">event</span>
                    {{ deadline.date }}
                  </span>
                </div>
              }
            } @else {
              <div class="empty-deadlines">
                <span class="material-icons-outlined">event_available</span>
                <p>{{ 'dashboard.no_deadlines' | translate }}</p>
              </div>
            }

            <!-- Quick Actions -->
            <div class="quick-actions">
              <h4>{{ 'dashboard.quick_actions' | translate }}</h4>
              <div class="action-links">
                <a routerLink="/ai-systems/new" class="action-link">
                  <span class="material-icons-outlined">add_circle</span>
                  {{ 'ai_systems.new_system' | translate }}
                </a>
                <a routerLink="/compliance" class="action-link">
                  <span class="material-icons-outlined">fact_check</span>
                  {{ 'compliance.title' | translate }}
                </a>
                <a routerLink="/classify" class="action-link">
                  <span class="material-icons-outlined">category</span>
                  {{ 'classification.title' | translate }}
                </a>
                <a routerLink="/analytics" class="action-link">
                  <span class="material-icons-outlined">insights</span>
                  {{ 'analytics.title' | translate }}
                </a>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .dashboard-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; }
    }
    .stats-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem; margin-bottom: 1.5rem;
    }
    .stat-card {
      display: flex; flex-wrap: wrap; align-items: center; gap: 1rem;
    }
    .stat-icon {
      width: 48px; height: 48px; border-radius: var(--radius-md);
      display: flex; align-items: center; justify-content: center;
      .material-icons-outlined { font-size: 1.5rem; color: white; }
    }
    .stat-systems .stat-icon { background: var(--primary); }
    .stat-compliance .stat-icon { background: var(--success); }
    .stat-obligations .stat-icon { background: #8b5cf6; }
    .stat-highrisk .stat-icon { background: #f97316; }
    .stat-content { flex: 1; }
    .stat-value { font-size: 1.75rem; font-weight: 700; line-height: 1.2; }
    .stat-total { font-size: 1rem; font-weight: 400; color: var(--text-muted); }
    .stat-label { font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.125rem; }
    .stat-progress { width: 100%; }
    .progress-bar {
      height: 6px; background: var(--bg-tertiary); border-radius: 3px; overflow: hidden;
    }
    .progress-fill {
      height: 100%; border-radius: 3px; transition: width 0.6s ease;
      &.low { background: var(--danger); }
      &.medium { background: var(--warning); }
      &.high { background: var(--success); }
    }
    .dashboard-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;
    }
    .chart-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .chart-wrapper { position: relative; height: 220px; margin-bottom: 1rem; }
    .risk-legend { display: flex; flex-direction: column; gap: 0.5rem; }
    .legend-item { display: flex; justify-content: space-between; align-items: center; }
    .risk-count { font-weight: 600; font-size: 0.875rem; }
    .deadline-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.75rem 0; border-bottom: 1px solid var(--border);
    }
    .deadline-info {
      display: flex; flex-direction: column;
      strong { font-size: 0.875rem; }
    }
    .deadline-system { font-size: 0.75rem; color: var(--text-muted); margin-top: 0.125rem; }
    .deadline-date {
      display: inline-flex; align-items: center; gap: 0.25rem;
      font-size: 0.8125rem; color: var(--text-secondary); white-space: nowrap;
      .material-icons-outlined { font-size: 1rem; }
      &.overdue { color: var(--danger); font-weight: 600; }
    }
    .empty-deadlines {
      text-align: center; padding: 2rem 1rem; color: var(--text-muted);
      .material-icons-outlined { font-size: 2.5rem; }
      p { margin-top: 0.5rem; font-size: 0.875rem; }
    }
    .quick-actions {
      margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid var(--border);
      h4 { font-size: 0.875rem; font-weight: 600; margin-bottom: 0.75rem; }
    }
    .action-links { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; }
    .action-link {
      display: flex; align-items: center; gap: 0.5rem;
      padding: 0.5rem 0.75rem; border-radius: var(--radius-md);
      font-size: 0.8125rem; color: var(--text-secondary);
      text-decoration: none; transition: all 0.15s ease;
      .material-icons-outlined { font-size: 1.125rem; color: var(--primary); }
      &:hover { background: var(--bg-tertiary); color: var(--text-primary); }
    }
    h3 { font-size: 1rem; font-weight: 600; }
    @media (max-width: 768px) {
      .dashboard-grid { grid-template-columns: 1fr; }
      .action-links { grid-template-columns: 1fr; }
    }
  `]
})
export class DashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('riskChart') riskChartRef!: ElementRef<HTMLCanvasElement>;

  private api = inject(ApiService);
  summary: DashboardSummary | null = null;
  loading = true;
  riskEntries: [string, number][] = [];
  private chartsReady = false;
  private dataReady = false;

  ngOnInit(): void {
    this.api.get<DashboardSummary>('/dashboard/summary').subscribe({
      next: res => {
        if (res.data) {
          this.summary = res.data;
          this.riskEntries = Object.entries(res.data.riskDistribution);
        }
        this.loading = false;
        this.dataReady = true;
        if (this.chartsReady) this.renderChart();
      },
      error: () => { this.loading = false; }
    });
  }

  ngAfterViewInit(): void {
    this.chartsReady = true;
    if (this.dataReady) {
      setTimeout(() => this.renderChart());
    }
  }

  renderChart(): void {
    if (!this.riskChartRef?.nativeElement || !this.summary) return;
    const dist = this.summary.riskDistribution;
    const data = [
      dist['UNACCEPTABLE'] || 0,
      dist['HIGH'] || 0,
      dist['LIMITED'] || 0,
      dist['MINIMAL'] || 0,
      dist['UNCLASSIFIED'] || 0
    ];

    if (data.every(v => v === 0)) return;

    new Chart(this.riskChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Unacceptable', 'High', 'Limited', 'Minimal', 'Unclassified'],
        datasets: [{
          data,
          backgroundColor: ['#dc2626', '#f97316', '#eab308', '#22c55e', '#94a3b8'],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: { display: false }
        }
      }
    });
  }

  isOverdue(date: string): boolean {
    return new Date(date) < new Date();
  }
}
