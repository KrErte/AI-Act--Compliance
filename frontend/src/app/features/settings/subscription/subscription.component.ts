import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

interface SubscriptionInfo {
  plan: string;
  status: string;
}

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [RouterLink, TranslateModule, LoadingSpinnerComponent],
  template: `
    <div class="page">
      <a routerLink="/settings" class="back-link">&larr; {{ 'settings.title' | translate }}</a>
      <h1>{{ 'settings.subscription' | translate }}</h1>

      @if (loading) {
        <app-loading-spinner />
      } @else {
        <div class="current-plan card">
          <h3>{{ 'settings.current_plan' | translate }}</h3>
          <div class="plan-badge">{{ subscription?.plan }}</div>
          <span class="plan-status" [class]="subscription?.status?.toLowerCase()">{{ subscription?.status }}</span>
        </div>

        <h2>{{ 'settings.plan_comparison' | translate }}</h2>
        <div class="plans-grid">
          @for (plan of plans; track plan.name) {
            <div class="card plan-card" [class.current]="plan.name === subscription?.plan">
              <h3>{{ plan.label }}</h3>
              <div class="plan-price">{{ plan.price }}</div>
              <ul>
                <li>{{ plan.systems }} {{ 'settings.ai_systems_limit' | translate }}</li>
                @for (feature of plan.features; track feature) {
                  <li>{{ feature }}</li>
                }
              </ul>
              @if (plan.name !== subscription?.plan) {
                <button class="btn btn-primary">{{ 'settings.upgrade' | translate }}</button>
              } @else {
                <button class="btn btn-secondary" disabled>Current Plan</button>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .back-link { font-size: 0.875rem; color: var(--text-secondary); display: inline-block; margin-bottom: 0.5rem; }
    h1 { font-size: 1.5rem; margin-bottom: 1.5rem; }
    h2 { font-size: 1.25rem; margin: 2rem 0 1rem; }
    .current-plan { display: flex; align-items: center; gap: 1rem; }
    .plan-badge { background: var(--primary); color: white; padding: 0.25rem 0.75rem; border-radius: var(--radius-sm); font-weight: 600; font-size: 0.875rem; }
    .plan-status { font-size: 0.875rem; text-transform: capitalize; }
    .plan-status.active { color: var(--success); }
    .plan-status.cancelled { color: var(--danger); }
    .plan-status.past_due { color: var(--warning); }
    .plans-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
    .plan-card { text-align: center; padding: 2rem 1.5rem; }
    .plan-card.current { border-color: var(--primary); }
    .plan-card h3 { font-size: 1.125rem; margin-bottom: 0.5rem; }
    .plan-price { font-size: 2rem; font-weight: 700; margin-bottom: 1.5rem; }
    .plan-card ul { list-style: none; margin-bottom: 1.5rem; }
    .plan-card li { padding: 0.375rem 0; font-size: 0.875rem; color: var(--text-secondary); }
    .plan-card .btn { width: 100%; justify-content: center; }
    @media (max-width: 768px) { .plans-grid { grid-template-columns: 1fr; } }
  `]
})
export class SubscriptionComponent implements OnInit {
  private api = inject(ApiService);
  subscription: SubscriptionInfo | null = null;
  loading = true;

  plans = [
    { name: 'STARTER', label: 'Starter', price: 'Free', systems: '5', features: ['Risk Classification', 'Compliance Checklist', 'Email Support'] },
    { name: 'PROFESSIONAL', label: 'Professional', price: '\u20AC49/mo', systems: '20', features: ['Everything in Starter', 'Document Generation', 'Priority Support'] },
    { name: 'ENTERPRISE', label: 'Enterprise', price: 'Custom', systems: 'Unlimited', features: ['Everything in Pro', 'SSO / SAML', 'Dedicated Account Manager'] }
  ];

  ngOnInit(): void {
    this.api.get<SubscriptionInfo>('/subscription').subscribe({
      next: res => { this.subscription = res.data ?? null; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
