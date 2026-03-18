import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { RegulationService, RegulationSummary, AssessmentResult } from '../../core/services/regulation.service';

interface Milestone {
  date: string;
  title: string;
  description: string;
  status: 'active' | 'upcoming' | 'future';
}

@Component({
  selector: 'app-regulations',
  standalone: true,
  imports: [TranslateModule, RouterLink, DatePipe],
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

      <!-- Regulation Frameworks from API -->
      @if (regulations().length > 0) {
        <h2 class="section-title">{{ 'regulations.frameworks' | translate }}</h2>
        <div class="frameworks-grid">
          @for (reg of regulations(); track reg.id) {
            <div class="card framework-card">
              <div class="framework-header">
                <span class="framework-code">{{ reg.code }}</span>
                <span class="framework-badge">{{ reg.domainCount }} {{ 'regulations.domains' | translate }} · {{ reg.questionCount }} {{ 'regulations.questions' | translate }}</span>
              </div>
              <h3>{{ reg.name }}</h3>
              <p>{{ reg.description }}</p>
              @if (reg.effectiveDate) {
                <div class="effective-date">
                  <span class="material-icons-outlined">event</span>
                  {{ reg.effectiveDate }}
                </div>
              }
              <div class="framework-actions">
                <a [routerLink]="['/regulations/assess', reg.id]" class="btn-primary">
                  {{ 'regulations.start_assessment' | translate }}
                </a>
                <a [routerLink]="['/regulations/result', reg.id]" class="btn-secondary">
                  {{ 'regulations.view_results' | translate }}
                </a>
              </div>
            </div>
          }
        </div>
      }

      <!-- Assessment History -->
      @if (assessmentHistory().length > 0) {
        <h2 class="section-title">{{ 'regulations.assessment_history' | translate }}</h2>
        <div class="card">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ 'regulations.regulation' | translate }}</th>
                <th>{{ 'regulations.score' | translate }}</th>
                <th>{{ 'common.date' | translate }}</th>
              </tr>
            </thead>
            <tbody>
              @for (a of assessmentHistory(); track a.id) {
                <tr>
                  <td>{{ a.regulationName }}</td>
                  <td>
                    <div class="score-cell">
                      <div class="score-bar">
                        <div class="score-fill" [style.width.%]="a.overallScore"
                             [class.low]="a.overallScore < 40"
                             [class.medium]="a.overallScore >= 40 && a.overallScore < 70"
                             [class.high]="a.overallScore >= 70"></div>
                      </div>
                      <span class="score-value">{{ a.overallScore }}%</span>
                    </div>
                  </td>
                  <td class="date-cell">{{ a.completedAt | date:'medium' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
      .subtitle { color: var(--text-secondary); font-size: 0.875rem; }
    }
    .section-title { font-size: 1.125rem; font-weight: 600; margin: 1.5rem 0 1rem; }
    .timeline-card { margin-bottom: 1.5rem; }
    .timeline-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1.5rem; }
    .card {
      background: white; border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 1.25rem;
    }
    .timeline {
      position: relative; padding-left: 2rem;
      &::before { content: ''; position: absolute; left: 8px; top: 0; bottom: 0; width: 2px; background: var(--border); }
    }
    .timeline-item { position: relative; padding-bottom: 1.5rem; padding-left: 1rem; &:last-child { padding-bottom: 0; } }
    .timeline-marker {
      position: absolute; left: -1.55rem; top: 4px; width: 14px; height: 14px;
      border-radius: 50%; border: 2px solid var(--border); background: white;
    }
    .timeline-item.active .timeline-marker { border-color: var(--success); background: var(--success); }
    .timeline-item.upcoming .timeline-marker { border-color: var(--warning); background: var(--warning); }
    .timeline-item.future .timeline-marker { border-color: var(--text-muted); }
    .timeline-date { font-size: 0.75rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
    .timeline-title { font-weight: 600; margin: 0.25rem 0; }
    .timeline-desc { font-size: 0.875rem; color: var(--text-secondary); }
    .frameworks-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .framework-card {
      h3 { font-size: 1.125rem; font-weight: 600; margin: 0.75rem 0 0.5rem; }
      p { font-size: 0.875rem; color: var(--text-secondary); line-height: 1.5; margin-bottom: 0.75rem; }
    }
    .framework-header { display: flex; justify-content: space-between; align-items: center; }
    .framework-code {
      font-size: 0.75rem; font-weight: 600; color: var(--primary); background: var(--primary-light);
      padding: 0.125rem 0.5rem; border-radius: var(--radius-sm);
    }
    .framework-badge { font-size: 0.75rem; color: var(--text-muted); }
    .effective-date {
      display: inline-flex; align-items: center; gap: 0.25rem; font-size: 0.8125rem;
      color: var(--text-secondary); margin-bottom: 1rem;
      .material-icons-outlined { font-size: 1rem; }
    }
    .framework-actions { display: flex; gap: 0.5rem; }
    .btn-primary {
      display: inline-block; padding: 0.5rem 1rem; background: var(--primary); color: white;
      border-radius: var(--radius-md); font-size: 0.8125rem; font-weight: 500; text-decoration: none;
      transition: opacity 0.15s; &:hover { opacity: 0.9; }
    }
    .btn-secondary {
      display: inline-block; padding: 0.5rem 1rem; border: 1px solid var(--border);
      border-radius: var(--radius-md); font-size: 0.8125rem; font-weight: 500; text-decoration: none;
      color: var(--text-primary); &:hover { border-color: var(--primary); color: var(--primary); }
    }
    .data-table {
      width: 100%; border-collapse: collapse;
      th { text-align: left; padding: 0.75rem 1rem; font-size: 0.75rem; font-weight: 600;
           text-transform: uppercase; color: var(--text-muted); border-bottom: 1px solid var(--border); }
      td { padding: 0.75rem 1rem; font-size: 0.875rem; border-bottom: 1px solid var(--border); }
    }
    .score-cell { display: flex; align-items: center; gap: 0.75rem; }
    .score-bar { flex: 1; height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden; }
    .score-fill { height: 100%; border-radius: 4px; transition: width 0.3s; }
    .score-fill.low { background: #ef4444; }
    .score-fill.medium { background: #f59e0b; }
    .score-fill.high { background: #10b981; }
    .score-value { font-size: 0.875rem; font-weight: 600; min-width: 3rem; }
    .date-cell { color: var(--text-secondary); font-size: 0.8125rem; }
  `]
})
export class RegulationsComponent implements OnInit {
  regulations = signal<RegulationSummary[]>([]);
  assessmentHistory = signal<AssessmentResult[]>([]);

  milestones: Milestone[] = [
    { date: '1 August 2024', title: 'AI Act Enters into Force', description: 'Regulation (EU) 2024/1689 published and entered into force.', status: 'active' },
    { date: '17 January 2025', title: 'DORA Applies', description: 'Digital Operational Resilience Act (EU) 2022/2554 becomes applicable to financial entities.', status: 'active' },
    { date: '2 February 2025', title: 'Prohibited AI Practices Ban', description: 'Article 5 prohibitions become enforceable.', status: 'active' },
    { date: '2 August 2025', title: 'GPAI Model Obligations', description: 'General-purpose AI model providers must comply with Articles 51-56.', status: 'upcoming' },
    { date: '2 August 2026', title: 'High-Risk AI System Obligations', description: 'Full obligations for high-risk AI systems under Article 6 and Annex III.', status: 'future' },
  ];

  constructor(private regulationService: RegulationService) {}

  ngOnInit() {
    this.regulationService.getAll().subscribe(res => {
      if (res.success && res.data) this.regulations.set(res.data);
    });
    this.regulationService.getAssessmentHistory().subscribe(res => {
      if (res.success && res.data) this.assessmentHistory.set(res.data);
    });
  }
}
