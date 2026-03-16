import { Component, OnInit, inject, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

interface DashboardSummary {
  totalAiSystems: number;
  riskDistribution: Record<string, number>;
  overallComplianceScore: number;
  obligationCounts: { total: number; completed: number; inProgress: number; notStarted: number };
  upcomingDeadlines: Array<{ title: string; date: string; aiSystemName: string }>;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [TranslateModule, LoadingSpinnerComponent],
  template: `
    <div class="analytics-page">
      <div class="page-header">
        <h1>{{ 'analytics.title' | translate }}</h1>
      </div>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <!-- KPI Cards -->
        <div class="kpi-grid">
          <div class="card kpi-card">
            <div class="kpi-icon systems">
              <span class="material-icons-outlined">smart_toy</span>
            </div>
            <div class="kpi-data">
              <span class="kpi-value">{{ summary?.totalAiSystems ?? 0 }}</span>
              <span class="kpi-label">{{ 'analytics.total_systems' | translate }}</span>
            </div>
          </div>
          <div class="card kpi-card">
            <div class="kpi-icon compliance">
              <span class="material-icons-outlined">verified</span>
            </div>
            <div class="kpi-data">
              <span class="kpi-value">{{ summary?.overallComplianceScore ?? 0 }}%</span>
              <span class="kpi-label">{{ 'analytics.compliance_rate' | translate }}</span>
            </div>
          </div>
          <div class="card kpi-card">
            <div class="kpi-icon completed">
              <span class="material-icons-outlined">check_circle</span>
            </div>
            <div class="kpi-data">
              <span class="kpi-value">{{ summary?.obligationCounts?.completed ?? 0 }}</span>
              <span class="kpi-label">{{ 'analytics.completed_obligations' | translate }}</span>
            </div>
          </div>
          <div class="card kpi-card">
            <div class="kpi-icon pending">
              <span class="material-icons-outlined">pending_actions</span>
            </div>
            <div class="kpi-data">
              <span class="kpi-value">{{ (summary?.obligationCounts?.notStarted ?? 0) + (summary?.obligationCounts?.inProgress ?? 0) }}</span>
              <span class="kpi-label">{{ 'analytics.pending_obligations' | translate }}</span>
            </div>
          </div>
        </div>

        <!-- Charts Row -->
        <div class="charts-grid">
          <div class="card chart-card">
            <h3>{{ 'analytics.risk_distribution' | translate }}</h3>
            <div class="chart-container">
              <canvas #riskChart></canvas>
            </div>
          </div>
          <div class="card chart-card">
            <h3>{{ 'analytics.obligation_status' | translate }}</h3>
            <div class="chart-container">
              <canvas #obligationChart></canvas>
            </div>
          </div>
        </div>

        <!-- Compliance Score Gauge -->
        <div class="card">
          <h3>{{ 'analytics.compliance_overview' | translate }}</h3>
          <div class="compliance-gauge">
            <div class="gauge-bar">
              <div class="gauge-fill" [style.width.%]="summary?.overallComplianceScore ?? 0"
                   [class.low]="(summary?.overallComplianceScore ?? 0) < 33"
                   [class.medium]="(summary?.overallComplianceScore ?? 0) >= 33 && (summary?.overallComplianceScore ?? 0) < 66"
                   [class.high]="(summary?.overallComplianceScore ?? 0) >= 66">
              </div>
            </div>
            <div class="gauge-labels">
              <span>0%</span>
              <span class="gauge-current">{{ summary?.overallComplianceScore ?? 0 }}%</span>
              <span>100%</span>
            </div>
          </div>
          <div class="obligation-breakdown">
            <div class="breakdown-item">
              <div class="breakdown-dot completed"></div>
              <span>{{ 'status.completed' | translate }}</span>
              <strong>{{ summary?.obligationCounts?.completed ?? 0 }}</strong>
            </div>
            <div class="breakdown-item">
              <div class="breakdown-dot in-progress"></div>
              <span>{{ 'status.in_progress' | translate }}</span>
              <strong>{{ summary?.obligationCounts?.inProgress ?? 0 }}</strong>
            </div>
            <div class="breakdown-item">
              <div class="breakdown-dot not-started"></div>
              <span>{{ 'status.not_started' | translate }}</span>
              <strong>{{ summary?.obligationCounts?.notStarted ?? 0 }}</strong>
            </div>
          </div>
        </div>

        <!-- Deadlines -->
        @if (summary?.upcomingDeadlines?.length) {
          <div class="card">
            <h3>{{ 'analytics.upcoming_deadlines' | translate }}</h3>
            <table class="deadlines-table">
              <thead>
                <tr>
                  <th>{{ 'common.obligation' | translate }}</th>
                  <th>{{ 'common.ai_system' | translate }}</th>
                  <th>{{ 'common.due_date' | translate }}</th>
                  <th>{{ 'common.days_left' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                @for (d of summary!.upcomingDeadlines; track d.title) {
                  <tr [class.overdue]="getDaysLeft(d.date) < 0">
                    <td>{{ d.title }}</td>
                    <td>{{ d.aiSystemName }}</td>
                    <td>{{ d.date }}</td>
                    <td>
                      @if (getDaysLeft(d.date) < 0) {
                        <span class="overdue-badge">{{ getDaysLeft(d.date) }}d</span>
                      } @else {
                        <span class="days-badge">{{ getDaysLeft(d.date) }}d</span>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.5rem; h1 { font-size: 1.5rem; } }
    .kpi-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem; margin-bottom: 1.5rem;
    }
    .kpi-card {
      display: flex; align-items: center; gap: 1rem;
    }
    .kpi-icon {
      width: 48px; height: 48px; border-radius: var(--radius-md);
      display: flex; align-items: center; justify-content: center;
      .material-icons-outlined { font-size: 1.5rem; color: white; }
      &.systems { background: var(--primary); }
      &.compliance { background: var(--success); }
      &.completed { background: #8b5cf6; }
      &.pending { background: var(--warning); }
    }
    .kpi-data { display: flex; flex-direction: column; }
    .kpi-value { font-size: 1.75rem; font-weight: 700; line-height: 1; }
    .kpi-label { font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .charts-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-bottom: 1.5rem;
    }
    .chart-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .chart-container { position: relative; height: 280px; }
    .compliance-gauge { margin: 1.5rem 0; }
    .gauge-bar {
      height: 24px; background: var(--bg-tertiary); border-radius: 12px;
      overflow: hidden;
    }
    .gauge-fill {
      height: 100%; border-radius: 12px; transition: width 0.6s ease;
      &.low { background: var(--danger); }
      &.medium { background: var(--warning); }
      &.high { background: var(--success); }
    }
    .gauge-labels {
      display: flex; justify-content: space-between; margin-top: 0.5rem;
      font-size: 0.75rem; color: var(--text-muted);
    }
    .gauge-current { font-weight: 700; color: var(--text-primary); font-size: 0.875rem; }
    .obligation-breakdown {
      display: flex; gap: 2rem; margin-top: 1rem;
    }
    .breakdown-item {
      display: flex; align-items: center; gap: 0.5rem; font-size: 0.875rem;
    }
    .breakdown-dot {
      width: 10px; height: 10px; border-radius: 50%;
      &.completed { background: var(--success); }
      &.in-progress { background: var(--primary); }
      &.not-started { background: var(--text-muted); }
    }
    .deadlines-table {
      width: 100%; border-collapse: collapse; margin-top: 1rem;
      th, td { padding: 0.75rem; text-align: left; border-bottom: 1px solid var(--border); font-size: 0.875rem; }
      th { font-weight: 600; color: var(--text-secondary); font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.05em; }
      tr.overdue td { color: var(--danger); }
    }
    .overdue-badge {
      background: #fee2e2; color: #991b1b; padding: 0.125rem 0.5rem;
      border-radius: var(--radius-sm); font-size: 0.75rem; font-weight: 600;
    }
    .days-badge {
      background: #dbeafe; color: #1e40af; padding: 0.125rem 0.5rem;
      border-radius: var(--radius-sm); font-size: 0.75rem; font-weight: 600;
    }
    h3 { font-size: 1rem; font-weight: 600; }
    .card { margin-bottom: 1.5rem; }
    @media (max-width: 768px) {
      .charts-grid { grid-template-columns: 1fr; }
      .obligation-breakdown { flex-direction: column; gap: 0.5rem; }
    }
  `]
})
export class AnalyticsComponent implements OnInit, AfterViewInit {
  @ViewChild('riskChart') riskChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('obligationChart') obligationChartRef!: ElementRef<HTMLCanvasElement>;

  private api = inject(ApiService);
  summary: DashboardSummary | null = null;
  loading = true;
  private chartsReady = false;
  private dataReady = false;

  ngOnInit(): void {
    this.api.get<DashboardSummary>('/dashboard/summary').subscribe({
      next: res => {
        this.summary = res.data ?? null;
        this.loading = false;
        this.dataReady = true;
        if (this.chartsReady) this.renderCharts();
      },
      error: () => { this.loading = false; }
    });
  }

  ngAfterViewInit(): void {
    this.chartsReady = true;
    if (this.dataReady) {
      setTimeout(() => this.renderCharts());
    }
  }

  renderCharts(): void {
    if (!this.summary) return;
    this.renderRiskChart();
    this.renderObligationChart();
  }

  renderRiskChart(): void {
    if (!this.riskChartRef?.nativeElement || !this.summary) return;
    const dist = this.summary.riskDistribution;
    new Chart(this.riskChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Unacceptable', 'High', 'Limited', 'Minimal', 'Unclassified'],
        datasets: [{
          data: [
            dist['UNACCEPTABLE'] || 0,
            dist['HIGH'] || 0,
            dist['LIMITED'] || 0,
            dist['MINIMAL'] || 0,
            dist['UNCLASSIFIED'] || 0
          ],
          backgroundColor: ['#dc2626', '#f97316', '#eab308', '#22c55e', '#94a3b8'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
        }
      }
    });
  }

  renderObligationChart(): void {
    if (!this.obligationChartRef?.nativeElement || !this.summary) return;
    const counts = this.summary.obligationCounts;
    new Chart(this.obligationChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Completed', 'In Progress', 'Not Started'],
        datasets: [{
          data: [counts.completed, counts.inProgress, counts.notStarted],
          backgroundColor: ['#16a34a', '#2563eb', '#94a3b8'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
        }
      }
    });
  }

  getDaysLeft(date: string): number {
    const diff = new Date(date).getTime() - new Date().getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }
}
