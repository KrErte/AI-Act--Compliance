import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [TranslateModule, RouterLink],
  template: `
    <header class="header">
      <div class="header-left">
        <h2 class="header-title">{{ 'nav.dashboard' | translate }}</h2>
      </div>
      <div class="header-right">
        <!-- Notification bell -->
        <div class="notification-wrapper">
          <button class="icon-btn" (click)="toggleAlerts()" title="Alerts">
            <span class="material-icons-outlined">notifications</span>
            @if (unreadCount > 0) {
              <span class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            }
          </button>
          @if (showAlerts) {
            <div class="alert-dropdown">
              <div class="alert-header">
                <span>{{ 'alerts.title' | translate }}</span>
                <button class="link-btn" (click)="markAllRead()">{{ 'common.mark_all_read' | translate }}</button>
              </div>
              @if (alerts.length === 0) {
                <p class="no-alerts">{{ 'alerts.no_alerts' | translate }}</p>
              } @else {
                @for (alert of alerts; track alert.id) {
                  <div class="alert-item" [class.unread]="!alert.read" (click)="markRead(alert)">
                    <span class="alert-severity severity-{{ alert.severity.toLowerCase() }}"></span>
                    <div class="alert-content">
                      <div class="alert-title">{{ alert.title }}</div>
                      <div class="alert-message">{{ alert.message }}</div>
                    </div>
                  </div>
                }
              }
              <a routerLink="/alerts" class="view-all" (click)="showAlerts = false">
                {{ 'alerts.view_all' | translate }}
              </a>
            </div>
          }
        </div>

        <!-- Language dropdown -->
        <div class="lang-dropdown-wrapper">
          <button class="lang-toggle" (click)="showLangDropdown = !showLangDropdown">
            {{ currentLang.toUpperCase() }} ▾
          </button>
          @if (showLangDropdown) {
            <div class="lang-dropdown">
              @for (lang of languages; track lang.code) {
                <button class="lang-option" [class.active]="currentLang === lang.code"
                        (click)="switchLanguage(lang.code)">
                  {{ lang.label }}
                </button>
              }
            </div>
          }
        </div>

        <div class="user-menu">
          <a routerLink="/profile" class="user-name">{{ authService.currentUser()?.firstName }}</a>
          <button class="btn btn-secondary btn-sm" (click)="authService.logout()">
            {{ 'common.logout' | translate }}
          </button>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .header {
      position: fixed; top: 0; right: 0; left: var(--sidebar-width);
      height: var(--header-height); background: white;
      border-bottom: 1px solid var(--border);
      display: flex; align-items: center; justify-content: space-between;
      padding: 0 1.5rem; z-index: 100;
    }
    .header-title { font-size: 1.125rem; font-weight: 600; }
    .header-right { display: flex; align-items: center; gap: 1rem; }
    .icon-btn {
      position: relative; background: none; border: none; cursor: pointer;
      padding: 0.375rem; border-radius: var(--radius-sm); color: var(--text-secondary);
    }
    .icon-btn:hover { background: var(--bg-hover, #f1f5f9); }
    .badge {
      position: absolute; top: -2px; right: -2px;
      background: #ef4444; color: white;
      font-size: 0.625rem; font-weight: 700;
      min-width: 16px; height: 16px;
      border-radius: 9999px; display: flex; align-items: center; justify-content: center;
      padding: 0 4px;
    }
    .notification-wrapper { position: relative; }
    .alert-dropdown {
      position: absolute; top: 100%; right: 0; margin-top: 0.5rem;
      width: 380px; background: white; border: 1px solid var(--border);
      border-radius: var(--radius-md); box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      z-index: 300; max-height: 400px; overflow-y: auto;
    }
    .alert-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.75rem 1rem; border-bottom: 1px solid var(--border);
      font-weight: 600; font-size: 0.875rem;
    }
    .link-btn { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 0.75rem; }
    .no-alerts { padding: 1.5rem; text-align: center; color: var(--text-muted); font-size: 0.875rem; }
    .alert-item {
      display: flex; gap: 0.5rem; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-light, #f1f5f9);
      cursor: pointer; font-size: 0.8125rem;
    }
    .alert-item:hover { background: var(--bg-hover, #f8fafc); }
    .alert-item.unread { background: #eff6ff; }
    .alert-severity { width: 8px; height: 8px; border-radius: 50%; margin-top: 6px; flex-shrink: 0; }
    .severity-low { background: #22c55e; }
    .severity-medium { background: #f59e0b; }
    .severity-high { background: #ef4444; }
    .severity-critical { background: #dc2626; }
    .alert-title { font-weight: 500; }
    .alert-message { color: var(--text-secondary); font-size: 0.75rem; margin-top: 0.125rem; }
    .view-all { display: block; text-align: center; padding: 0.75rem; color: var(--primary); font-size: 0.8125rem; border-top: 1px solid var(--border); text-decoration: none; }
    .lang-dropdown-wrapper { position: relative; }
    .lang-toggle {
      padding: 0.375rem 0.75rem; border: 1px solid var(--border);
      border-radius: var(--radius-sm); background: white;
      cursor: pointer; font-weight: 500; font-size: 0.8125rem;
    }
    .lang-dropdown {
      position: absolute; top: 100%; right: 0; margin-top: 0.25rem;
      background: white; border: 1px solid var(--border);
      border-radius: var(--radius-sm); box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      z-index: 300; min-width: 120px;
    }
    .lang-option {
      display: block; width: 100%; text-align: left; padding: 0.5rem 0.75rem;
      border: none; background: none; cursor: pointer; font-size: 0.8125rem;
    }
    .lang-option:hover { background: var(--bg-hover, #f1f5f9); }
    .lang-option.active { font-weight: 600; color: var(--primary); }
    .user-menu { display: flex; align-items: center; gap: 0.75rem; }
    .user-name { font-size: 0.875rem; font-weight: 500; color: var(--text-primary); text-decoration: none; }
    .user-name:hover { color: var(--primary); }
    .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8125rem; }
  `]
})
export class HeaderComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  private translate = inject(TranslateService);
  private api = inject(ApiService);

  currentLang = 'en';
  showAlerts = false;
  showLangDropdown = false;
  unreadCount = 0;
  alerts: any[] = [];
  private pollInterval: any;

  languages = [
    { code: 'en', label: 'English' },
    { code: 'et', label: 'Eesti' },
    { code: 'fi', label: 'Suomi' },
    { code: 'lv', label: 'Latviešu' },
    { code: 'lt', label: 'Lietuvių' },
    { code: 'pl', label: 'Polski' }
  ];

  ngOnInit(): void {
    this.loadUnreadCount();
    this.pollInterval = setInterval(() => this.loadUnreadCount(), 60000);
  }

  ngOnDestroy(): void {
    if (this.pollInterval) clearInterval(this.pollInterval);
  }

  loadUnreadCount(): void {
    this.api.get<any>('/alerts/unread-count').subscribe({
      next: res => { this.unreadCount = res.data?.count || 0; }
    });
  }

  toggleAlerts(): void {
    this.showAlerts = !this.showAlerts;
    this.showLangDropdown = false;
    if (this.showAlerts) {
      this.api.get<any>('/alerts', { size: 10 }).subscribe({
        next: res => { this.alerts = res.data?.content || []; }
      });
    }
  }

  markRead(alert: any): void {
    if (!alert.read) {
      this.api.patch(`/alerts/${alert.id}/read`).subscribe({
        next: () => { alert.read = true; this.unreadCount = Math.max(0, this.unreadCount - 1); }
      });
    }
  }

  markAllRead(): void {
    this.api.post('/alerts/mark-all-read').subscribe({
      next: () => { this.alerts.forEach(a => a.read = true); this.unreadCount = 0; }
    });
  }

  switchLanguage(code: string): void {
    this.currentLang = code;
    this.translate.use(code);
    this.showLangDropdown = false;
  }
}
