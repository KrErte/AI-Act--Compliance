import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslateModule],
  template: `
    <aside class="sidebar">
      <div class="sidebar-brand">
        <h1>AIAudit</h1>
      </div>
      <nav class="sidebar-nav">
        @for (item of navItems; track item.path) {
          <a [routerLink]="item.path"
             routerLinkActive="active"
             [routerLinkActiveOptions]="{ exact: item.exact ?? false }"
             class="nav-item">
            <span class="material-icons-outlined">{{ item.icon }}</span>
            <span>{{ item.label | translate }}</span>
          </a>
        }
      </nav>
    </aside>
  `,
  styles: [`
    .sidebar {
      position: fixed;
      left: 0;
      top: 0;
      bottom: 0;
      width: var(--sidebar-width);
      background: #0f172a;
      color: white;
      display: flex;
      flex-direction: column;
      z-index: 200;
    }
    .sidebar-brand {
      padding: 1.25rem 1.5rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      h1 {
        font-size: 1.375rem;
        font-weight: 700;
        color: white;
      }
    }
    .sidebar-nav {
      padding: 1rem 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      color: #94a3b8;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.15s ease;
      text-decoration: none;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
        color: white;
      }

      &.active {
        background: var(--primary);
        color: white;
      }

      .material-icons-outlined {
        font-size: 1.25rem;
      }
    }
  `]
})
export class SidebarComponent {
  navItems = [
    { path: '/dashboard', icon: 'dashboard', label: 'nav.dashboard', exact: true },
    { path: '/ai-systems', icon: 'smart_toy', label: 'nav.ai_systems' },
    { path: '/compliance', icon: 'verified', label: 'nav.compliance' },
    { path: '/gpai-models', icon: 'model_training', label: 'nav.gpai_models' },
    { path: '/my-tasks', icon: 'task_alt', label: 'nav.my_tasks' },
    { path: '/analytics', icon: 'analytics', label: 'nav.analytics' },
    { path: '/regulations', icon: 'gavel', label: 'nav.regulations' },
    { path: '/team', icon: 'group', label: 'nav.team' },
    { path: '/organization', icon: 'business', label: 'nav.organization' },
    { path: '/settings', icon: 'settings', label: 'nav.settings' }
  ];
}
