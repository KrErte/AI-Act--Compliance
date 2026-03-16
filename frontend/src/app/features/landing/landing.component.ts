import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  template: `
    <div class="landing">
      <!-- Header -->
      <header class="landing-header">
        <div class="container header-inner">
          <a routerLink="/" class="logo">AIAudit</a>
          <nav class="header-nav">
            <a routerLink="/classify" class="nav-link">{{ 'landing.try_classifier' | translate }}</a>
            <a routerLink="/auth/login" class="btn btn-secondary btn-sm">{{ 'common.login' | translate }}</a>
            <a routerLink="/auth/register" class="btn btn-primary btn-sm">{{ 'landing.get_started' | translate }}</a>
          </nav>
        </div>
      </header>

      <!-- Hero -->
      <section class="hero">
        <div class="container hero-inner">
          <div class="hero-badge">EU AI Act (Regulation 2024/1689)</div>
          <h1>{{ 'landing.hero_title' | translate }}</h1>
          <p class="hero-subtitle">{{ 'landing.hero_subtitle' | translate }}</p>
          <div class="hero-actions">
            <a routerLink="/auth/register" class="btn btn-primary btn-lg">
              {{ 'landing.get_started' | translate }}
              <span class="material-icons-outlined">arrow_forward</span>
            </a>
            <a routerLink="/classify" class="btn btn-secondary btn-lg">
              <span class="material-icons-outlined">category</span>
              {{ 'landing.try_classifier' | translate }}
            </a>
          </div>
          <p class="hero-note">{{ 'landing.cta_subtitle' | translate }}</p>
        </div>
      </section>

      <!-- Deadline Banner -->
      <section class="deadline-banner">
        <div class="container">
          <div class="countdown-inner">
            <span class="material-icons-outlined">schedule</span>
            <div>
              <strong>August 2, 2026</strong> — High-risk AI system obligations become enforceable
            </div>
          </div>
        </div>
      </section>

      <!-- Features -->
      <section class="features" id="features">
        <div class="container">
          <h2>{{ 'landing.features_title' | translate }}</h2>
          <div class="features-grid">
            @for (feature of features; track feature.icon) {
              <div class="feature-card">
                <div class="feature-icon">
                  <span class="material-icons-outlined">{{ feature.icon }}</span>
                </div>
                <h3>{{ feature.titleKey | translate }}</h3>
                <p>{{ feature.descKey | translate }}</p>
              </div>
            }
          </div>
        </div>
      </section>

      <!-- How it Works -->
      <section class="how-it-works">
        <div class="container">
          <h2>How It Works</h2>
          <div class="steps-grid">
            <div class="step">
              <div class="step-number">1</div>
              <h3>Register Your AI Systems</h3>
              <p>Add your AI systems with a simple wizard. Describe purpose, deployment context, and your organization's role.</p>
            </div>
            <div class="step">
              <div class="step-number">2</div>
              <h3>Classify Risk Level</h3>
              <p>Our classification engine analyzes your system against all EU AI Act criteria — Articles 5, 6, and Annex III.</p>
            </div>
            <div class="step">
              <div class="step-number">3</div>
              <h3>Track Obligations</h3>
              <p>Obligations are auto-generated based on risk classification. Assign tasks, set deadlines, monitor progress.</p>
            </div>
            <div class="step">
              <div class="step-number">4</div>
              <h3>Generate Documentation</h3>
              <p>AI-powered document generation creates FRIA, technical docs, risk management plans, and more.</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Pricing -->
      <section class="pricing" id="pricing">
        <div class="container">
          <h2>{{ 'landing.pricing_title' | translate }}</h2>
          <div class="pricing-grid">
            <div class="pricing-card">
              <div class="plan-name">{{ 'landing.pricing_starter' | translate }}</div>
              <div class="plan-price">
                <span class="price">{{ 'landing.pricing_free' | translate }}</span>
              </div>
              <ul class="plan-features">
                <li>Up to 5 AI Systems</li>
                <li>Risk Classification</li>
                <li>Compliance Tracking</li>
                <li>3 Documents / Month</li>
                <li>1 Team Member</li>
                <li>Email Support</li>
              </ul>
              <a routerLink="/auth/register" class="btn btn-secondary btn-block">{{ 'landing.pricing_cta' | translate }}</a>
            </div>
            <div class="pricing-card featured">
              <div class="popular-badge">Most Popular</div>
              <div class="plan-name">{{ 'landing.pricing_professional' | translate }}</div>
              <div class="plan-price">
                <span class="price">&euro;49</span>
                <span class="period">{{ 'landing.pricing_month' | translate }}</span>
              </div>
              <ul class="plan-features">
                <li>Up to 20 AI Systems</li>
                <li>Risk Classification</li>
                <li>Compliance Tracking</li>
                <li>Unlimited Documents</li>
                <li>10 Team Members</li>
                <li>GPAI Model Tracking</li>
                <li>Analytics & Reporting</li>
                <li>Priority Support</li>
              </ul>
              <a routerLink="/auth/register" class="btn btn-primary btn-block">{{ 'landing.pricing_cta' | translate }}</a>
            </div>
            <div class="pricing-card">
              <div class="plan-name">{{ 'landing.pricing_enterprise' | translate }}</div>
              <div class="plan-price">
                <span class="price">{{ 'landing.pricing_contact' | translate }}</span>
              </div>
              <ul class="plan-features">
                <li>Unlimited AI Systems</li>
                <li>Risk Classification</li>
                <li>Compliance Tracking</li>
                <li>Unlimited Documents</li>
                <li>Unlimited Team Members</li>
                <li>GPAI Model Tracking</li>
                <li>Analytics & Reporting</li>
                <li>White-Label Portal</li>
                <li>API Access</li>
                <li>Dedicated Support</li>
              </ul>
              <a href="mailto:sales@aiaudit.eu" class="btn btn-secondary btn-block">{{ 'landing.pricing_contact' | translate }}</a>
            </div>
          </div>
        </div>
      </section>

      <!-- CTA -->
      <section class="cta">
        <div class="container cta-inner">
          <h2>{{ 'landing.cta_title' | translate }}</h2>
          <p>{{ 'landing.cta_subtitle' | translate }}</p>
          <a routerLink="/auth/register" class="btn btn-primary btn-lg">
            {{ 'landing.get_started' | translate }}
            <span class="material-icons-outlined">arrow_forward</span>
          </a>
        </div>
      </section>

      <!-- Footer -->
      <footer class="landing-footer">
        <div class="container footer-inner">
          <div class="footer-brand">
            <h3>AIAudit</h3>
            <p>{{ 'auth.tagline' | translate }}</p>
          </div>
          <div class="footer-links">
            <div class="footer-col">
              <h4>Product</h4>
              <a routerLink="/classify">Risk Classifier</a>
              <a href="#features">Features</a>
              <a href="#pricing">Pricing</a>
            </div>
            <div class="footer-col">
              <h4>Compliance</h4>
              <a href="https://eur-lex.europa.eu/eli/reg/2024/1689" target="_blank" rel="noopener">EU AI Act Full Text</a>
              <a href="https://digital-strategy.ec.europa.eu/en/policies/regulatory-framework-ai" target="_blank" rel="noopener">EC AI Policy</a>
            </div>
            <div class="footer-col">
              <h4>Company</h4>
              <a href="mailto:info@aiaudit.eu">Contact</a>
              <a href="/privacy">Privacy Policy</a>
              <a href="/terms">Terms of Service</a>
            </div>
          </div>
          <div class="footer-bottom">
            <p>&copy; {{ currentYear }} AIAudit. {{ 'common.all_rights_reserved' | translate }}</p>
          </div>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .landing { background: white; }

    /* Header */
    .landing-header {
      position: fixed; top: 0; left: 0; right: 0; z-index: 100;
      background: rgba(255,255,255,0.95); backdrop-filter: blur(8px);
      border-bottom: 1px solid var(--border);
    }
    .header-inner {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.75rem 1.5rem; max-width: 1200px; margin: 0 auto;
    }
    .logo { font-size: 1.375rem; font-weight: 700; color: var(--text-primary); text-decoration: none; }
    .header-nav { display: flex; align-items: center; gap: 1rem; }
    .nav-link { font-size: 0.875rem; color: var(--text-secondary); text-decoration: none; font-weight: 500; }
    .nav-link:hover { color: var(--primary); }
    .btn-sm { padding: 0.375rem 0.875rem; font-size: 0.8125rem; }
    .btn-lg { padding: 0.875rem 1.75rem; font-size: 1rem; }
    .btn-block { display: block; width: 100%; text-align: center; }

    /* Hero */
    .hero {
      padding: 8rem 1.5rem 4rem; text-align: center;
      background: linear-gradient(180deg, #f8fafc 0%, white 100%);
    }
    .hero-inner { max-width: 800px; margin: 0 auto; }
    .hero-badge {
      display: inline-block; padding: 0.375rem 1rem;
      background: var(--primary-light); color: var(--primary);
      font-size: 0.8125rem; font-weight: 600; border-radius: 9999px;
      margin-bottom: 1.5rem;
    }
    .hero h1 {
      font-size: 3rem; font-weight: 800; line-height: 1.15;
      color: var(--text-primary); margin-bottom: 1.25rem;
      letter-spacing: -0.02em;
    }
    .hero-subtitle {
      font-size: 1.125rem; color: var(--text-secondary); line-height: 1.6;
      margin-bottom: 2rem; max-width: 640px; margin-left: auto; margin-right: auto;
    }
    .hero-actions { display: flex; gap: 1rem; justify-content: center; margin-bottom: 1rem; }
    .hero-note { font-size: 0.8125rem; color: var(--text-muted); }

    /* Deadline */
    .deadline-banner {
      background: #0f172a; color: white; padding: 1rem 1.5rem;
    }
    .countdown-inner {
      max-width: 1200px; margin: 0 auto;
      display: flex; align-items: center; justify-content: center; gap: 0.75rem;
      font-size: 0.9375rem;
      .material-icons-outlined { color: #f97316; }
      strong { color: #f97316; }
    }

    /* Features */
    .features {
      padding: 5rem 1.5rem; background: white;
      h2 { text-align: center; font-size: 2rem; font-weight: 700; margin-bottom: 3rem; }
    }
    .features-grid {
      max-width: 1200px; margin: 0 auto;
      display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 2rem;
    }
    .feature-card {
      padding: 2rem; border: 1px solid var(--border); border-radius: var(--radius-lg);
      transition: all 0.2s ease;
      &:hover { box-shadow: var(--shadow-md); border-color: var(--primary-light); }
      h3 { font-size: 1.125rem; font-weight: 600; margin: 1rem 0 0.5rem; }
      p { font-size: 0.875rem; color: var(--text-secondary); line-height: 1.6; }
    }
    .feature-icon {
      width: 48px; height: 48px; border-radius: var(--radius-md);
      background: var(--primary-light); display: flex; align-items: center; justify-content: center;
      .material-icons-outlined { color: var(--primary); font-size: 1.5rem; }
    }

    /* How it Works */
    .how-it-works {
      padding: 5rem 1.5rem; background: var(--bg-secondary);
      h2 { text-align: center; font-size: 2rem; font-weight: 700; margin-bottom: 3rem; }
    }
    .steps-grid {
      max-width: 1000px; margin: 0 auto;
      display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 2rem;
    }
    .step { text-align: center; }
    .step-number {
      width: 48px; height: 48px; border-radius: 50%; margin: 0 auto 1rem;
      background: var(--primary); color: white;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.25rem; font-weight: 700;
    }
    .step h3 { font-size: 1rem; font-weight: 600; margin-bottom: 0.5rem; }
    .step p { font-size: 0.8125rem; color: var(--text-secondary); line-height: 1.5; }

    /* Pricing */
    .pricing {
      padding: 5rem 1.5rem; background: white;
      h2 { text-align: center; font-size: 2rem; font-weight: 700; margin-bottom: 3rem; }
    }
    .pricing-grid {
      max-width: 1000px; margin: 0 auto;
      display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem;
      align-items: start;
    }
    .pricing-card {
      position: relative; padding: 2rem; border: 1px solid var(--border);
      border-radius: var(--radius-lg); background: white;
      &.featured {
        border-color: var(--primary); box-shadow: 0 0 0 1px var(--primary);
        transform: scale(1.02);
      }
    }
    .popular-badge {
      position: absolute; top: -12px; left: 50%; transform: translateX(-50%);
      background: var(--primary); color: white; padding: 0.25rem 1rem;
      font-size: 0.75rem; font-weight: 600; border-radius: 9999px;
    }
    .plan-name { font-size: 1.125rem; font-weight: 600; margin-bottom: 0.5rem; }
    .plan-price { margin-bottom: 1.5rem; }
    .price { font-size: 2.5rem; font-weight: 800; }
    .period { font-size: 0.875rem; color: var(--text-muted); }
    .plan-features {
      list-style: none; margin-bottom: 2rem;
      li {
        padding: 0.5rem 0; border-bottom: 1px solid #f1f5f9;
        font-size: 0.875rem; color: var(--text-secondary);
        &::before { content: '\\2713'; color: var(--success); margin-right: 0.5rem; font-weight: 700; }
      }
    }

    /* CTA */
    .cta {
      padding: 5rem 1.5rem; background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
      color: white; text-align: center;
    }
    .cta-inner { max-width: 600px; margin: 0 auto; }
    .cta h2 { font-size: 2rem; font-weight: 700; margin-bottom: 0.75rem; }
    .cta p { font-size: 1.125rem; opacity: 0.9; margin-bottom: 2rem; }
    .cta .btn-primary { background: white; color: var(--primary); }
    .cta .btn-primary:hover { background: #f1f5f9; }

    /* Footer */
    .landing-footer {
      background: #0f172a; color: #94a3b8; padding: 3rem 1.5rem 1.5rem;
    }
    .footer-inner { max-width: 1200px; margin: 0 auto; }
    .footer-brand {
      margin-bottom: 2rem;
      h3 { color: white; font-size: 1.25rem; margin-bottom: 0.25rem; }
      p { font-size: 0.875rem; }
    }
    .footer-links { display: flex; gap: 4rem; margin-bottom: 2rem; flex-wrap: wrap; }
    .footer-col {
      h4 { color: white; font-size: 0.875rem; font-weight: 600; margin-bottom: 0.75rem; }
      a {
        display: block; color: #94a3b8; font-size: 0.8125rem; padding: 0.25rem 0;
        text-decoration: none;
        &:hover { color: white; }
      }
    }
    .footer-bottom {
      padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,0.1);
      p { font-size: 0.75rem; }
    }

    @media (max-width: 768px) {
      .hero h1 { font-size: 2rem; }
      .hero-actions { flex-direction: column; align-items: center; }
      .header-nav { gap: 0.5rem; }
      .nav-link { display: none; }
      .footer-links { flex-direction: column; gap: 1.5rem; }
    }
  `]
})
export class LandingComponent {
  currentYear = new Date().getFullYear();

  features = [
    { icon: 'category', titleKey: 'landing.feature_classify', descKey: 'landing.feature_classify_desc' },
    { icon: 'fact_check', titleKey: 'landing.feature_compliance', descKey: 'landing.feature_compliance_desc' },
    { icon: 'description', titleKey: 'landing.feature_documents', descKey: 'landing.feature_documents_desc' },
    { icon: 'group', titleKey: 'landing.feature_team', descKey: 'landing.feature_team_desc' },
    { icon: 'notifications_active', titleKey: 'landing.feature_alerts', descKey: 'landing.feature_alerts_desc' },
    { icon: 'insights', titleKey: 'landing.feature_analytics', descKey: 'landing.feature_analytics_desc' }
  ];
}
