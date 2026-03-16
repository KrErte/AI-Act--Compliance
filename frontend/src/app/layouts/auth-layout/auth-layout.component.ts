import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet, TranslateModule],
  template: `
    <div class="auth-layout">
      <div class="auth-left">
        <div class="auth-brand">
          <h1>AIAudit</h1>
          <p>{{ 'auth.tagline' | translate }}</p>
        </div>
        <div class="auth-form-container">
          <router-outlet />
        </div>
      </div>
      <div class="auth-right">
        <div class="auth-promo">
          <h2>{{ 'auth.promo_title' | translate }}</h2>
          <p>{{ 'auth.promo_description' | translate }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-layout {
      display: flex;
      min-height: 100vh;
    }
    .auth-left {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 2rem 3rem;
      max-width: 560px;
    }
    .auth-brand {
      margin-bottom: 2rem;
      h1 {
        font-size: 1.75rem;
        font-weight: 700;
        color: var(--primary);
      }
      p {
        color: var(--text-secondary);
        margin-top: 0.25rem;
      }
    }
    .auth-right {
      flex: 1;
      background: linear-gradient(135deg, var(--primary) 0%, #1e40af 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 3rem;
    }
    .auth-promo {
      color: white;
      max-width: 480px;
      h2 { font-size: 2rem; margin-bottom: 1rem; }
      p { opacity: 0.9; font-size: 1.1rem; line-height: 1.7; }
    }
    @media (max-width: 768px) {
      .auth-right { display: none; }
      .auth-left { max-width: 100%; padding: 1.5rem; }
    }
  `]
})
export class AuthLayoutComponent {}
