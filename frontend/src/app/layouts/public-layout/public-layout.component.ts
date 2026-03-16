import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, TranslateModule],
  template: `
    <div class="public-layout">
      <header class="public-header">
        <div class="container public-header-inner">
          <a routerLink="/classify" class="logo">AIAudit</a>
          <nav>
            <a routerLink="/auth/login" class="btn btn-secondary">{{ 'common.login' | translate }}</a>
            <a routerLink="/auth/register" class="btn btn-primary">{{ 'common.sign_up' | translate }}</a>
          </nav>
        </div>
      </header>
      <main class="public-content">
        <router-outlet />
      </main>
      <footer class="public-footer">
        <div class="container">
          <p>&copy; 2025 AIAudit. {{ 'common.all_rights_reserved' | translate }}</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .public-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .public-header {
      background: white;
      border-bottom: 1px solid var(--border);
      padding: 0.75rem 0;
    }
    .public-header-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .logo {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--primary);
    }
    nav { display: flex; gap: 0.75rem; }
    .public-content { flex: 1; padding: 2rem 0; }
    .public-footer {
      background: var(--bg-tertiary);
      padding: 1.5rem 0;
      text-align: center;
      color: var(--text-muted);
      font-size: 0.875rem;
    }
  `]
})
export class PublicLayoutComponent {}
