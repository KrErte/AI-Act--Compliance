import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  template: `
    <div class="page">
      <h1>{{ 'settings.title' | translate }}</h1>
      <div class="settings-grid">
        <a routerLink="/settings/subscription" class="card settings-card">
          <span class="material-icons-outlined">credit_card</span>
          <h3>{{ 'settings.subscription' | translate }}</h3>
          <p>{{ 'settings.manage_subscription' | translate }}</p>
        </a>
      </div>
    </div>
  `,
  styles: [`
    h1 { font-size: 1.5rem; margin-bottom: 1.5rem; }
    .settings-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; }
    .settings-card { text-decoration: none; color: inherit; text-align: center; padding: 2rem; cursor: pointer; }
    .settings-card:hover { box-shadow: var(--shadow-md); }
    .settings-card .material-icons-outlined { font-size: 2.5rem; color: var(--primary); margin-bottom: 0.75rem; }
    .settings-card h3 { font-size: 1rem; margin-bottom: 0.25rem; }
    .settings-card p { font-size: 0.875rem; color: var(--text-secondary); }
  `]
})
export class SettingsComponent {}
