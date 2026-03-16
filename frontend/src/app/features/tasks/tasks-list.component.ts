import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

interface TaskObligation {
  id: string;
  articleRef: string;
  articleTitle: string;
  description: string;
  status: string;
  dueDate: string | null;
  aiSystemId: string;
  aiSystemName: string;
}

@Component({
  selector: 'app-tasks-list',
  standalone: true,
  imports: [TranslateModule, RouterLink, LoadingSpinnerComponent, StatusBadgeComponent],
  template: `
    <div class="tasks-page">
      <div class="page-header">
        <h1>{{ 'tasks.title' | translate }}</h1>
        <div class="filter-group">
          <select (change)="filterByStatus($event)">
            <option value="">{{ 'common.all_statuses' | translate }}</option>
            <option value="NOT_STARTED">{{ 'status.not_started' | translate }}</option>
            <option value="IN_PROGRESS">{{ 'status.in_progress' | translate }}</option>
            <option value="COMPLETED">{{ 'status.completed' | translate }}</option>
          </select>
        </div>
      </div>

      @if (loading) {
        <app-loading-spinner />
      } @else if (filteredTasks.length === 0) {
        <div class="card empty-state">
          <span class="material-icons-outlined">task_alt</span>
          <h3>{{ 'tasks.no_tasks' | translate }}</h3>
          <p>{{ 'tasks.no_tasks_desc' | translate }}</p>
        </div>
      } @else {
        <div class="tasks-list">
          @for (task of filteredTasks; track task.id) {
            <div class="card task-card" [class.overdue]="isOverdue(task)">
              <div class="task-header">
                <div class="task-info">
                  <h3>{{ task.articleTitle }}</h3>
                  <span class="article-ref">{{ task.articleRef }}</span>
                </div>
                <app-status-badge [value]="task.status" type="obligation" />
              </div>
              <p class="task-description">{{ task.description }}</p>
              <div class="task-footer">
                <a [routerLink]="['/compliance', task.aiSystemId]" class="system-link">
                  <span class="material-icons-outlined">smart_toy</span>
                  {{ task.aiSystemName }}
                </a>
                @if (task.dueDate) {
                  <span class="due-date" [class.overdue-text]="isOverdue(task)">
                    <span class="material-icons-outlined">event</span>
                    {{ task.dueDate }}
                  </span>
                }
              </div>
              <div class="task-actions">
                @if (task.status !== 'COMPLETED') {
                  <button class="btn btn-sm btn-primary" (click)="updateStatus(task, getNextStatus(task.status))">
                    @if (task.status === 'NOT_STARTED') {
                      {{ 'tasks.start' | translate }}
                    } @else {
                      {{ 'tasks.complete' | translate }}
                    }
                  </button>
                } @else {
                  <span class="completed-label">
                    <span class="material-icons-outlined">check_circle</span>
                    {{ 'status.completed' | translate }}
                  </span>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .page-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; }
    }
    .filter-group select {
      padding: 0.5rem 0.75rem; border: 1px solid var(--border);
      border-radius: var(--radius-md); font-size: 0.875rem; background: white;
    }
    .empty-state {
      text-align: center; padding: 3rem;
      .material-icons-outlined { font-size: 3rem; color: var(--text-muted); }
      h3 { margin: 1rem 0 0.5rem; }
      p { color: var(--text-secondary); }
    }
    .tasks-list { display: flex; flex-direction: column; gap: 1rem; }
    .task-card {
      &.overdue { border-left: 3px solid var(--danger); }
    }
    .task-header {
      display: flex; justify-content: space-between; align-items: flex-start;
      margin-bottom: 0.75rem;
    }
    .task-info h3 { font-size: 1rem; font-weight: 600; }
    .article-ref {
      font-size: 0.75rem; color: var(--primary); font-weight: 500;
      background: var(--primary-light); padding: 0.125rem 0.5rem;
      border-radius: var(--radius-sm);
    }
    .task-description {
      font-size: 0.875rem; color: var(--text-secondary);
      margin-bottom: 0.75rem; line-height: 1.5;
    }
    .task-footer {
      display: flex; justify-content: space-between; align-items: center;
      font-size: 0.8125rem; color: var(--text-secondary);
      margin-bottom: 0.75rem;
    }
    .system-link {
      display: inline-flex; align-items: center; gap: 0.25rem;
      color: var(--primary); text-decoration: none;
      .material-icons-outlined { font-size: 1rem; }
      &:hover { text-decoration: underline; }
    }
    .due-date {
      display: inline-flex; align-items: center; gap: 0.25rem;
      .material-icons-outlined { font-size: 1rem; }
    }
    .overdue-text { color: var(--danger); font-weight: 600; }
    .task-actions { display: flex; justify-content: flex-end; }
    .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8125rem; }
    .completed-label {
      display: inline-flex; align-items: center; gap: 0.25rem;
      color: var(--success); font-size: 0.875rem; font-weight: 500;
      .material-icons-outlined { font-size: 1.125rem; }
    }
  `]
})
export class TasksListComponent implements OnInit {
  private api = inject(ApiService);

  tasks: TaskObligation[] = [];
  filteredTasks: TaskObligation[] = [];
  loading = true;
  statusFilter = '';

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.api.get<TaskObligation[]>('/compliance/my-tasks').subscribe({
      next: res => {
        this.tasks = res.data || [];
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  filterByStatus(event: Event): void {
    this.statusFilter = (event.target as HTMLSelectElement).value;
    this.applyFilter();
  }

  applyFilter(): void {
    this.filteredTasks = this.statusFilter
      ? this.tasks.filter(t => t.status === this.statusFilter)
      : [...this.tasks];
  }

  isOverdue(task: TaskObligation): boolean {
    if (!task.dueDate || task.status === 'COMPLETED') return false;
    return new Date(task.dueDate) < new Date();
  }

  getNextStatus(current: string): string {
    return current === 'NOT_STARTED' ? 'IN_PROGRESS' : 'COMPLETED';
  }

  updateStatus(task: TaskObligation, newStatus: string): void {
    this.api.put(`/compliance/obligations/${task.id}`, { status: newStatus }).subscribe({
      next: () => {
        task.status = newStatus;
        this.applyFilter();
      }
    });
  }
}
