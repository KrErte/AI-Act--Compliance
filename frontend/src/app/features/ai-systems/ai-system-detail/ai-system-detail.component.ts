import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DocumentListComponent } from '../document-list/document-list.component';

@Component({
  selector: 'app-ai-system-detail',
  standalone: true,
  imports: [RouterLink, TranslateModule, LoadingSpinnerComponent, StatusBadgeComponent, DatePipe, DocumentListComponent],
  template: `
    @if (loading) {
      <app-loading-spinner />
    } @else if (system) {
      <div class="detail-header">
        <div>
          <a routerLink="/ai-systems" class="back-link">&larr; {{ 'ai_systems.title' | translate }}</a>
          <h1>{{ system.name }}</h1>
        </div>
        <div class="detail-actions">
          @if (!system.riskLevel) {
            <a [routerLink]="['/ai-systems', system.id, 'classify']" class="btn btn-primary">
              {{ 'ai_systems.detail.classify_now' | translate }}
            </a>
          }
        </div>
      </div>

      <div class="tabs">
        <button class="tab" [class.active]="activeTab === 'overview'" (click)="activeTab = 'overview'">
          {{ 'ai_systems.detail.overview' | translate }}
        </button>
        <button class="tab" [class.active]="activeTab === 'documents'" (click)="activeTab = 'documents'">
          {{ 'ai_systems.detail.documents' | translate }}
        </button>
        <button class="tab" [class.active]="activeTab === 'audit'" (click)="activeTab = 'audit'; loadAuditLog()">
          {{ 'ai_systems.detail.audit_log' | translate }}
        </button>
      </div>

      @if (activeTab === 'overview') {
        <div class="detail-grid">
          <div class="card">
            <h3>{{ 'ai_systems.detail.overview' | translate }}</h3>
            <div class="detail-info">
              <div class="info-row">
                <span class="label">{{ 'ai_systems.detail.risk_level' | translate }}</span>
                @if (system.riskLevel) {
                  <app-status-badge [value]="system.riskLevel" type="risk" />
                } @else {
                  <span class="text-muted">{{ 'ai_systems.detail.not_classified' | translate }}</span>
                }
              </div>
              <div class="info-row">
                <span class="label">{{ 'ai_systems.detail.compliance_score' | translate }}</span>
                <span class="score">{{ system.complianceScore }}%</span>
              </div>
              <div class="info-row">
                <span class="label">{{ 'common.status' | translate }}</span>
                <span>{{ system.status }}</span>
              </div>
              <div class="info-row">
                <span class="label">{{ 'ai_systems.wizard.vendor' | translate }}</span>
                <span>{{ system.vendor || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">{{ 'ai_systems.wizard.deployment_context' | translate }}</span>
                <span>{{ system.deploymentContext || '-' }}</span>
              </div>
            </div>
          </div>
          <div class="card">
            <h3>{{ 'ai_systems.detail.obligations' | translate }}</h3>
            <a [routerLink]="['/compliance', system.id]" class="btn btn-secondary">
              {{ 'compliance.checklist_title' | translate }}
            </a>
          </div>
        </div>
      }

      @if (activeTab === 'documents') {
        <div class="card">
          <app-document-list [aiSystemId]="system.id" />
        </div>
      }

      @if (activeTab === 'audit') {
        <div class="card">
          <h3>{{ 'audit.title' | translate }}</h3>
          @if (auditLoading) {
            <app-loading-spinner />
          } @else if (auditLogs.length === 0) {
            <p class="text-muted">{{ 'audit.no_entries' | translate }}</p>
          } @else {
            <table class="audit-table">
              <thead>
                <tr>
                  <th>{{ 'audit.action' | translate }}</th>
                  <th>{{ 'audit.user' | translate }}</th>
                  <th>{{ 'audit.old_value' | translate }}</th>
                  <th>{{ 'audit.new_value' | translate }}</th>
                  <th>{{ 'audit.timestamp' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                @for (log of auditLogs; track log.id) {
                  <tr>
                    <td><span class="action-badge">{{ log.action }}</span></td>
                    <td>{{ log.userName || '-' }}</td>
                    <td>{{ log.oldValue || '-' }}</td>
                    <td>{{ log.newValue || '-' }}</td>
                    <td>{{ log.createdAt | date:'short' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      }
    }
  `,
  styles: [`
    .detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .back-link { font-size: 0.875rem; color: var(--text-secondary); }
    h1 { font-size: 1.5rem; margin-top: 0.25rem; }
    .detail-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; }
    h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .detail-info { display: flex; flex-direction: column; gap: 0.75rem; }
    .info-row { display: flex; justify-content: space-between; align-items: center; }
    .label { color: var(--text-secondary); font-size: 0.875rem; }
    .score { font-size: 1.25rem; font-weight: 700; }
    .text-muted { color: var(--text-muted); font-size: 0.875rem; }
    .tabs { display: flex; gap: 0; border-bottom: 1px solid var(--border); margin-bottom: 1.5rem; }
    .tab {
      padding: 0.75rem 1.25rem; border: none; background: none; cursor: pointer;
      font-size: 0.875rem; font-weight: 500; color: var(--text-secondary);
      border-bottom: 2px solid transparent; transition: all 0.15s;
    }
    .tab:hover { color: var(--text-primary); }
    .tab.active { color: var(--primary); border-bottom-color: var(--primary); }
    .audit-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .audit-table th { text-align: left; padding: 0.5rem; color: var(--text-secondary); font-weight: 500; border-bottom: 1px solid var(--border); }
    .audit-table td { padding: 0.5rem; border-bottom: 1px solid var(--border-light, #f1f5f9); }
    .action-badge { padding: 0.125rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; background: #eff6ff; color: #1d4ed8; }
  `]
})
export class AiSystemDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);

  system: any = null;
  loading = true;
  activeTab = 'overview';
  auditLogs: any[] = [];
  auditLoading = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.get<any>(`/ai-systems/${id}`).subscribe({
        next: res => { this.system = res.data; this.loading = false; },
        error: () => { this.loading = false; }
      });
    }
  }

  loadAuditLog(): void {
    if (this.auditLogs.length > 0 || !this.system) return;
    this.auditLoading = true;
    this.api.get<any>(`/ai-systems/${this.system.id}/audit-log`).subscribe({
      next: res => {
        this.auditLogs = res.data?.content || [];
        this.auditLoading = false;
      },
      error: () => { this.auditLoading = false; }
    });
  }
}
