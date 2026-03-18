import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService, AdminUser } from '../../../core/services/admin.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [TranslateModule, DatePipe],
  template: `
    <div class="admin-users">
      <div class="page-header">
        <h1>{{ 'admin.manage_users' | translate }}</h1>
      </div>

      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ 'common.name' | translate }}</th>
              <th>{{ 'auth.email' | translate }}</th>
              <th>{{ 'team.role' | translate }}</th>
              <th>{{ 'common.status' | translate }}</th>
              <th>{{ 'team.joined' | translate }}</th>
              <th>{{ 'common.actions' | translate }}</th>
            </tr>
          </thead>
          <tbody>
            @for (user of users(); track user.id) {
              <tr>
                <td class="name-cell">{{ user.firstName }} {{ user.lastName }}</td>
                <td>{{ user.email }}</td>
                <td>
                  <select class="role-select"
                          [value]="user.role"
                          (change)="onRoleChange(user, $event)">
                    <option value="OWNER">{{ 'enums.role.OWNER' | translate }}</option>
                    <option value="ADMIN">{{ 'enums.role.ADMIN' | translate }}</option>
                    <option value="COMPLIANCE_MANAGER">{{ 'enums.role.COMPLIANCE_MANAGER' | translate }}</option>
                    <option value="VIEWER">{{ 'enums.role.VIEWER' | translate }}</option>
                  </select>
                </td>
                <td>
                  <span class="status-badge" [class.active]="user.enabled" [class.disabled]="!user.enabled">
                    {{ user.enabled ? ('admin.active' | translate) : ('admin.disabled' | translate) }}
                  </span>
                </td>
                <td class="date-cell">{{ user.createdAt | date:'mediumDate' }}</td>
                <td>
                  @if (user.enabled) {
                    <button class="btn-icon danger" (click)="deactivate(user)" title="{{ 'admin.deactivate' | translate }}">
                      <span class="material-icons-outlined">person_off</span>
                    </button>
                  } @else {
                    <button class="btn-icon success" (click)="activate(user)" title="{{ 'admin.activate' | translate }}">
                      <span class="material-icons-outlined">person</span>
                    </button>
                  }
                </td>
              </tr>
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
    .name-cell { font-weight: 500; }
    .date-cell { color: var(--text-secondary); font-size: 0.8125rem; }
    .role-select {
      padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm);
      font-size: 0.8125rem; background: white; cursor: pointer;
    }
    .status-badge {
      display: inline-block; padding: 0.125rem 0.5rem; border-radius: 9999px;
      font-size: 0.75rem; font-weight: 500;
      &.active { background: #dcfce7; color: #166534; }
      &.disabled { background: #fee2e2; color: #991b1b; }
    }
    .btn-icon {
      background: none; border: none; cursor: pointer; padding: 0.25rem; border-radius: var(--radius-sm);
      .material-icons-outlined { font-size: 1.25rem; }
      &.danger { color: #dc2626; &:hover { background: #fee2e2; } }
      &.success { color: #16a34a; &:hover { background: #dcfce7; } }
    }
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
export class AdminUsersComponent implements OnInit {
  users = signal<AdminUser[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);

  constructor(private adminService: AdminService, private toast: ToastService) {}

  ngOnInit() {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.adminService.getUsers(page).subscribe(res => {
      if (res.success && res.data) {
        this.users.set(res.data.content);
        this.currentPage.set(res.data.page);
        this.totalPages.set(res.data.totalPages);
      }
    });
  }

  onRoleChange(user: AdminUser, event: Event) {
    const role = (event.target as HTMLSelectElement).value;
    this.adminService.updateUser(user.id, { role }).subscribe({
      next: res => {
        if (res.success) {
          this.toast.success(res.message || 'User updated');
          this.loadPage(this.currentPage());
        }
      },
      error: () => this.loadPage(this.currentPage())
    });
  }

  deactivate(user: AdminUser) {
    if (!confirm('Deactivate ' + user.firstName + ' ' + user.lastName + '?')) return;
    this.adminService.deactivateUser(user.id).subscribe({
      next: () => {
        this.toast.success('User deactivated');
        this.loadPage(this.currentPage());
      }
    });
  }

  activate(user: AdminUser) {
    this.adminService.updateUser(user.id, { enabled: true }).subscribe({
      next: res => {
        if (res.success) {
          this.toast.success('User activated');
          this.loadPage(this.currentPage());
        }
      }
    });
  }
}
