import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

interface Regulation {
  article: string;
  title: string;
  description: string;
  deadline: string;
  status: 'active' | 'upcoming' | 'future';
  category: string;
}

@Component({
  selector: 'app-regulations',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="regulations-page">
      <div class="page-header">
        <h1>{{ 'regulations.title' | translate }}</h1>
        <p class="subtitle">{{ 'regulations.subtitle' | translate }}</p>
      </div>

      <!-- Timeline -->
      <div class="card timeline-card">
        <h3>{{ 'regulations.key_deadlines' | translate }}</h3>
        <div class="timeline">
          @for (milestone of milestones; track milestone.date) {
            <div class="timeline-item" [class]="milestone.status">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <div class="timeline-date">{{ milestone.date }}</div>
                <div class="timeline-title">{{ milestone.title }}</div>
                <div class="timeline-desc">{{ milestone.description }}</div>
              </div>
            </div>
          }
        </div>
      </div>

      <!-- Category Filter -->
      <div class="filter-row">
        <button class="filter-btn" [class.active]="activeCategory === ''"
                (click)="activeCategory = ''">
          {{ 'common.all' | translate }}
        </button>
        @for (cat of categories; track cat) {
          <button class="filter-btn" [class.active]="activeCategory === cat"
                  (click)="activeCategory = cat">
            {{ cat }}
          </button>
        }
      </div>

      <!-- Regulations List -->
      <div class="regulations-list">
        @for (reg of filteredRegulations; track reg.article) {
          <div class="card regulation-card">
            <div class="reg-header">
              <span class="reg-article">{{ reg.article }}</span>
              <span class="reg-status" [class]="reg.status">
                @switch (reg.status) {
                  @case ('active') { Active }
                  @case ('upcoming') { Upcoming }
                  @case ('future') { Future }
                }
              </span>
            </div>
            <h3>{{ reg.title }}</h3>
            <p>{{ reg.description }}</p>
            <div class="reg-footer">
              <span class="reg-category">{{ reg.category }}</span>
              <span class="reg-deadline">
                <span class="material-icons-outlined">event</span>
                {{ reg.deadline }}
              </span>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
      .subtitle { color: var(--text-secondary); font-size: 0.875rem; }
    }
    .timeline-card { margin-bottom: 1.5rem; }
    .timeline-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1.5rem; }
    .timeline {
      position: relative; padding-left: 2rem;
      &::before {
        content: ''; position: absolute; left: 8px; top: 0; bottom: 0;
        width: 2px; background: var(--border);
      }
    }
    .timeline-item {
      position: relative; padding-bottom: 1.5rem; padding-left: 1rem;
      &:last-child { padding-bottom: 0; }
    }
    .timeline-marker {
      position: absolute; left: -1.55rem; top: 4px;
      width: 14px; height: 14px; border-radius: 50%;
      border: 2px solid var(--border); background: white;
    }
    .timeline-item.active .timeline-marker { border-color: var(--success); background: var(--success); }
    .timeline-item.upcoming .timeline-marker { border-color: var(--warning); background: var(--warning); }
    .timeline-item.future .timeline-marker { border-color: var(--text-muted); }
    .timeline-date { font-size: 0.75rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
    .timeline-title { font-weight: 600; margin: 0.25rem 0; }
    .timeline-desc { font-size: 0.875rem; color: var(--text-secondary); }
    .filter-row {
      display: flex; gap: 0.5rem; margin-bottom: 1.5rem; flex-wrap: wrap;
    }
    .filter-btn {
      padding: 0.375rem 0.875rem; border: 1px solid var(--border);
      border-radius: 9999px; background: white; cursor: pointer;
      font-size: 0.8125rem; font-weight: 500; transition: all 0.15s ease;
      &:hover { border-color: var(--primary); color: var(--primary); }
      &.active { background: var(--primary); color: white; border-color: var(--primary); }
    }
    .regulations-list { display: flex; flex-direction: column; gap: 1rem; }
    .regulation-card {
      h3 { font-size: 1rem; font-weight: 600; margin: 0.5rem 0; }
      p { font-size: 0.875rem; color: var(--text-secondary); line-height: 1.5; margin-bottom: 0.75rem; }
    }
    .reg-header { display: flex; justify-content: space-between; align-items: center; }
    .reg-article {
      font-size: 0.75rem; font-weight: 600; color: var(--primary);
      background: var(--primary-light); padding: 0.125rem 0.5rem;
      border-radius: var(--radius-sm);
    }
    .reg-status {
      font-size: 0.75rem; font-weight: 500; padding: 0.125rem 0.5rem;
      border-radius: var(--radius-sm);
      &.active { background: #dcfce7; color: #166534; }
      &.upcoming { background: #fef3c7; color: #92400e; }
      &.future { background: #f1f5f9; color: #475569; }
    }
    .reg-footer {
      display: flex; justify-content: space-between; align-items: center;
      font-size: 0.8125rem;
    }
    .reg-category { color: var(--text-muted); }
    .reg-deadline {
      display: inline-flex; align-items: center; gap: 0.25rem;
      color: var(--text-secondary);
      .material-icons-outlined { font-size: 1rem; }
    }
  `]
})
export class RegulationsComponent {
  activeCategory = '';

  categories = ['Prohibited AI', 'High-Risk', 'GPAI', 'Transparency', 'Governance'];

  milestones = [
    {
      date: '1 August 2024',
      title: 'AI Act Enters into Force',
      description: 'Regulation (EU) 2024/1689 published in the Official Journal and entered into force.',
      status: 'active'
    },
    {
      date: '2 February 2025',
      title: 'Prohibited AI Practices Ban',
      description: 'Article 5 prohibitions become enforceable. Banned: social scoring, subliminal manipulation, exploitation of vulnerabilities, predictive policing, untargeted facial scraping, emotion recognition in workplace/education, biometric categorization for sensitive attributes.',
      status: 'active'
    },
    {
      date: '2 August 2025',
      title: 'GPAI Model Obligations & Governance',
      description: 'General-purpose AI model providers must comply with Articles 51-56. AI Office and national competent authorities established. Codes of practice finalized.',
      status: 'upcoming'
    },
    {
      date: '2 August 2026',
      title: 'High-Risk AI System Obligations',
      description: 'Full obligations for high-risk AI systems under Article 6 and Annex III become enforceable. Conformity assessments, technical documentation, risk management, and human oversight required.',
      status: 'future'
    },
    {
      date: '2 August 2027',
      title: 'Annex I High-Risk Systems (Safety)',
      description: 'Obligations for high-risk AI systems embedded in products covered by existing EU safety legislation (Annex I) become enforceable.',
      status: 'future'
    }
  ];

  regulations: Regulation[] = [
    {
      article: 'Article 5',
      title: 'Prohibited Artificial Intelligence Practices',
      description: 'Bans AI systems that use subliminal, manipulative, or deceptive techniques, exploit vulnerabilities, perform social scoring, predictive policing, untargeted facial recognition scraping, emotion recognition in workplaces/education, and biometric categorization for sensitive attributes.',
      deadline: '2 Feb 2025',
      status: 'active',
      category: 'Prohibited AI'
    },
    {
      article: 'Article 6',
      title: 'Classification Rules for High-Risk AI Systems',
      description: 'Establishes two pathways for high-risk classification: (1) AI as safety component of product under EU harmonization legislation (Annex I), and (2) standalone high-risk AI in Annex III areas like biometrics, critical infrastructure, education, employment, law enforcement, etc.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 9',
      title: 'Risk Management System',
      description: 'High-risk AI providers must implement a continuous risk management system covering identification, estimation, evaluation, and treatment of risks. Must include testing, monitoring, and documentation throughout the system lifecycle.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 10',
      title: 'Data and Data Governance',
      description: 'Training, validation, and testing datasets must meet quality criteria. Requires data governance practices including design choices, data collection, preparation, labeling, bias examination, and gap identification.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 11',
      title: 'Technical Documentation',
      description: 'Providers must draw up technical documentation before placing on market. Must include system description, development process, monitoring methods, risk management, data governance, testing, and standards applied (Annex IV).',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 13',
      title: 'Transparency and Provision of Information',
      description: 'High-risk AI systems must be designed to be sufficiently transparent for deployers to interpret and use output appropriately. Instructions for use must accompany the system.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'Transparency'
    },
    {
      article: 'Article 14',
      title: 'Human Oversight',
      description: 'High-risk AI must be designed for effective human oversight during use. Must include appropriate human-machine interface tools, ability to understand capabilities/limitations, and ability to override or intervene.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 15',
      title: 'Accuracy, Robustness, and Cybersecurity',
      description: 'High-risk AI systems must achieve appropriate levels of accuracy, robustness, and cybersecurity. Must be resilient to errors, faults, adversarial attacks, and unauthorized access.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 27',
      title: 'Fundamental Rights Impact Assessment',
      description: 'Deployers of high-risk AI (public bodies and certain private entities) must conduct FRIA before putting the system into use. Must assess impact on fundamental rights of affected persons.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Article 50',
      title: 'Transparency Obligations for Certain AI Systems',
      description: 'Providers of AI systems interacting with persons must inform them they are interacting with AI. Providers of synthetic content must mark output as artificially generated. Deployers of emotion recognition/biometric categorization must inform persons.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'Transparency'
    },
    {
      article: 'Articles 51-56',
      title: 'General-Purpose AI Model Obligations',
      description: 'GPAI model providers must provide technical documentation, comply with copyright, publish training content summary. Systemic risk models (>10^25 FLOPS) face additional obligations: adversarial testing, incident monitoring, cybersecurity.',
      deadline: '2 Aug 2025',
      status: 'upcoming',
      category: 'GPAI'
    },
    {
      article: 'Article 64',
      title: 'AI Regulatory Sandboxes',
      description: 'Member States shall establish at least one AI regulatory sandbox. Provides controlled environment for developing, testing, and validating innovative AI systems for limited time before placement on market.',
      deadline: '2 Aug 2026',
      status: 'future',
      category: 'Governance'
    },
    {
      article: 'Article 72',
      title: 'Post-Market Monitoring',
      description: 'Providers of high-risk AI must establish post-market monitoring system proportionate to nature and risks. Must actively collect and review experience data from deployers.',
      deadline: '2 Aug 2026',
      status: 'upcoming',
      category: 'High-Risk'
    },
    {
      article: 'Articles 64-69',
      title: 'EU AI Office & National Authorities',
      description: 'Establishment of the EU AI Office within the Commission. Member States must designate national competent authorities and notify the Commission. AI Board established as advisory body.',
      deadline: '2 Aug 2025',
      status: 'upcoming',
      category: 'Governance'
    }
  ];

  get filteredRegulations(): Regulation[] {
    if (!this.activeCategory) return this.regulations;
    return this.regulations.filter(r => r.category === this.activeCategory);
  }
}
