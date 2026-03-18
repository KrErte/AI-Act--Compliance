import { Component, OnInit, signal } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService, AuditLogEntry } from '../../../core/services/admin.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-audit-log',
  standalone: true,
  imports: [TranslateModule, DatePipe],
  template: `
    <div class="admin-audit-log">
      <div class="page-header">
        <h1>{{ 'admin.audit_log_title' | translate }}</h1>
      </div>

      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ 'audit.timestamp' | translate }}</th>
              <th>{{ 'audit.user' | translate }}</th>
              <th>{{ 'audit.entity_type' | translate }}</th>
              <th>{{ 'audit.action' | translate }}</th>
              <th>{{ 'audit.old_value' | translate }}</th>
              <th>{{ 'audit.new_value' | translate }}</th>
            </tr>
          </thead>
          <tbody>
            @for (entry of entries(); track entry.id) {
              <tr>
                <td class="date-cell">{{ entry.createdAt | date:'short' }}</td>
                <td>{{ entry.userName || '—' }}</td>
                <td><span class="entity-badge">{{ entry.entityType }}</span></td>
                <td>
                  <span class="action-badge" [class]="entry.action.toLowerCase()">
                    {{ 'audit.actions.' + entry.action | translate }}
                  </span>
                </td>
                <td class="value-cell">{{ entry.oldValue || '—' }}</td>
                <td class="value-cell">{{ entry.newValue || '—' }}</td>
              </tr>
            }
            @if (entries().length === 0) {
              <tr><td colspan="6" class="empty">{{ 'audit.no_entries' | translate }}</td></tr>
            }
          </tbody>
        </table>

        @if (totalPages() > 1) {
          <div class="pagination">
            <button class="btn-sm" [disabled]="currentPage() === 0" (click)="loadPage(currentPage() - 1)">
              {{ 'common.previous' | translate }}
            </button>
            <span class="page-info">{{ currentPage() + 1 }} / {{ totalPages() }}</span>
            <button class="btn-sm" [disabled]="currentPage() >= totalPages() - 1" (click)="loadPage(currentPage() + 1)">
              {{ 'common.next' | translate }}
            </button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.5rem; h1 { font-size: 1.5rem; } }
    .card {
      background: white; border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden;
    }
    .data-table {
      width: 100%; border-collapse: collapse;
      th { text-align: left; padding: 0.75rem 1rem; font-size: 0.75rem; font-weight: 600;
           text-transform: uppercase; color: var(--text-muted); background: var(--bg-secondary);
           border-bottom: 1px solid var(--border); }
      td { padding: 0.75rem 1rem; font-size: 0.875rem; border-bottom: 1px solid var(--border); }
      tr:last-child td { border-bottom: none; }
    }
    .date-cell { color: var(--text-secondary); font-size: 0.8125rem; white-space: nowrap; }
    .value-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 0.8125rem; color: var(--text-secondary); }
    .entity-badge {
      display: inline-block; padding: 0.125rem 0.5rem; border-radius: var(--radius-sm);
      font-size: 0.75rem; font-weight: 500; background: #f1f5f9; color: #475569;
    }
    .action-badge {
      display: inline-block; padding: 0.125rem 0.5rem; border-radius: var(--radius-sm);
      font-size: 0.75rem; font-weight: 500;
      &.created { background: #dcfce7; color: #166534; }
      &.updated { background: #dbeafe; color: #1e40af; }
      &.deleted, &.deactivated { background: #fee2e2; color: #991b1b; }
      &.classified { background: #f3e8ff; color: #7c3aed; }
      &.status_changed { background: #fef3c7; color: #92400e; }
      &.assigned { background: #e0f2fe; color: #0369a1; }
    }
    .empty { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .pagination {
      display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 1rem;
    }
    .btn-sm {
      padding: 0.375rem 0.75rem; border: 1px solid var(--border); border-radius: var(--radius-sm);
      font-size: 0.8125rem; background: white; cursor: pointer;
      &:disabled { opacity: 0.5; cursor: not-allowed; }
      &:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
    }
    .page-info { font-size: 0.875rem; color: var(--text-secondary); }
  `]
})
export class AdminAuditLogComponent implements OnInit {
  entries = signal<AuditLogEntry[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.adminService.getAuditLog(page).subscribe(res => {
      if (res.success && res.data) {
        this.entries.set(res.data.content);
        this.currentPage.set(res.data.page);
        this.totalPages.set(res.data.totalPages);
      }
    });
  }
}
