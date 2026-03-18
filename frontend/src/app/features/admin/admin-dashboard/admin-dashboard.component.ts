import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService, AdminStats } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  template: `
    <div class="admin-dashboard">
      <div class="page-header">
        <h1>{{ 'admin.title' | translate }}</h1>
        <p class="subtitle">{{ 'admin.subtitle' | translate }}</p>
      </div>

      @if (stats()) {
        <div class="stats-grid">
          <div class="stat-card">
            <span class="material-icons-outlined stat-icon users">group</span>
            <div class="stat-value">{{ stats()!.totalUsers }}</div>
            <div class="stat-label">{{ 'admin.total_users' | translate }}</div>
          </div>
          <div class="stat-card">
            <span class="material-icons-outlined stat-icon systems">smart_toy</span>
            <div class="stat-value">{{ stats()!.totalAiSystems }}</div>
            <div class="stat-label">{{ 'admin.total_ai_systems' | translate }}</div>
          </div>
          <div class="stat-card">
            <span class="material-icons-outlined stat-icon score">verified</span>
            <div class="stat-value">{{ stats()!.overallComplianceScore }}%</div>
            <div class="stat-label">{{ 'admin.compliance_score' | translate }}</div>
          </div>
          <div class="stat-card">
            <span class="material-icons-outlined stat-icon obligations">task_alt</span>
            <div class="stat-value">{{ stats()!.completedObligations }}/{{ stats()!.totalObligations }}</div>
            <div class="stat-label">{{ 'admin.obligations_completed' | translate }}</div>
          </div>
        </div>

        <div class="cards-row">
          <div class="card">
            <h3>{{ 'admin.users_by_role' | translate }}</h3>
            <div class="role-list">
              @for (entry of roleEntries(); track entry[0]) {
                <div class="role-row">
                  <span class="role-name">{{ 'enums.role.' + entry[0] | translate }}</span>
                  <span class="role-count">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </div>
          <div class="card">
            <h3>{{ 'admin.risk_distribution' | translate }}</h3>
            <div class="role-list">
              @for (entry of riskEntries(); track entry[0]) {
                <div class="role-row">
                  <span class="role-name">{{ 'enums.risk.' + entry[0] | translate }}</span>
                  <span class="role-count">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </div>
        </div>
      }

      <div class="quick-links">
        <a routerLink="/admin/users" class="quick-link card">
          <span class="material-icons-outlined">manage_accounts</span>
          <span>{{ 'admin.manage_users' | translate }}</span>
        </a>
        <a routerLink="/admin/audit-log" class="quick-link card">
          <span class="material-icons-outlined">history</span>
          <span>{{ 'admin.view_audit_log' | translate }}</span>
        </a>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
      .subtitle { color: var(--text-secondary); font-size: 0.875rem; }
    }
    .stats-grid {
      display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 1.5rem;
    }
    .stat-card {
      background: white; border: 1px solid var(--border); border-radius: var(--radius-lg);
      padding: 1.25rem; text-align: center;
    }
    .stat-icon { font-size: 2rem; margin-bottom: 0.5rem; }
    .stat-icon.users { color: #6366f1; }
    .stat-icon.systems { color: #8b5cf6; }
    .stat-icon.score { color: #10b981; }
    .stat-icon.obligations { color: #f59e0b; }
    .stat-value { font-size: 1.75rem; font-weight: 700; color: var(--text-primary); }
    .stat-label { font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .cards-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem; }
    .card {
      background: white; border: 1px solid var(--border); border-radius: var(--radius-lg);
      padding: 1.25rem;
      h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    }
    .role-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .role-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.5rem 0.75rem; background: var(--bg-secondary); border-radius: var(--radius-sm);
    }
    .role-name { font-size: 0.875rem; color: var(--text-primary); }
    .role-count { font-size: 0.875rem; font-weight: 600; color: var(--primary); }
    .quick-links { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .quick-link {
      display: flex; align-items: center; gap: 0.75rem; text-decoration: none;
      color: var(--text-primary); cursor: pointer; transition: border-color 0.15s;
      &:hover { border-color: var(--primary); }
      .material-icons-outlined { font-size: 1.5rem; color: var(--primary); }
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  stats = signal<AdminStats | null>(null);
  roleEntries = signal<[string, number][]>([]);
  riskEntries = signal<[string, number][]>([]);

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.adminService.getStats().subscribe(res => {
      if (res.success && res.data) {
        this.stats.set(res.data);
        this.roleEntries.set(Object.entries(res.data.usersByRole));
        this.riskEntries.set(Object.entries(res.data.riskDistribution));
      }
    });
  }
}
