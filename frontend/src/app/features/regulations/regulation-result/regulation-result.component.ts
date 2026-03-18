import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { RegulationService, AssessmentResult } from '../../../core/services/regulation.service';

@Component({
  selector: 'app-regulation-result',
  standalone: true,
  imports: [TranslateModule, RouterLink, DatePipe],
  template: `
    <div class="result-page">
      @if (result()) {
        <div class="page-header">
          <h1>{{ result()!.regulationName }} — {{ 'regulations.assessment_result' | translate }}</h1>
          @if (result()!.completedAt) {
            <p class="subtitle">{{ 'regulations.completed_on' | translate }} {{ result()!.completedAt | date:'medium' }}</p>
          }
        </div>

        <!-- Overall Score -->
        <div class="card score-card">
          <div class="overall-score">
            <div class="score-ring" [class.low]="result()!.overallScore < 40"
                 [class.medium]="result()!.overallScore >= 40 && result()!.overallScore < 70"
                 [class.high]="result()!.overallScore >= 70">
              <span class="score-number">{{ result()!.overallScore }}%</span>
            </div>
            <div class="score-info">
              <h2>{{ 'regulations.overall_score' | translate }}</h2>
              <p class="score-level" [class.low]="result()!.overallScore < 40"
                 [class.medium]="result()!.overallScore >= 40 && result()!.overallScore < 70"
                 [class.high]="result()!.overallScore >= 70">
                @if (result()!.overallScore < 40) { {{ 'regulations.level_low' | translate }} }
                @else if (result()!.overallScore < 70) { {{ 'regulations.level_medium' | translate }} }
                @else { {{ 'regulations.level_high' | translate }} }
              </p>
            </div>
          </div>
        </div>

        <!-- Domain Scores -->
        <h2 class="section-title">{{ 'regulations.domain_scores' | translate }}</h2>
        <div class="domains-grid">
          @for (domain of result()!.domainScores; track domain.domainCode) {
            <div class="card domain-score-card">
              <div class="domain-header">
                <span class="domain-code">{{ domain.domainCode }}</span>
                <span class="domain-questions">{{ domain.answeredQuestions }}/{{ domain.totalQuestions }}</span>
              </div>
              <h3>{{ domain.domainName }}</h3>
              <div class="domain-score-bar">
                <div class="bar-bg">
                  <div class="bar-fill"
                       [style.width.%]="domain.score"
                       [class.low]="domain.score < 40"
                       [class.medium]="domain.score >= 40 && domain.score < 70"
                       [class.high]="domain.score >= 70">
                  </div>
                </div>
                <span class="domain-score-value">{{ domain.score }}%</span>
              </div>
              <div class="domain-weight">{{ 'regulations.weight' | translate }}: {{ domain.weight }}x</div>
            </div>
          }
        </div>

        <div class="actions">
          <a routerLink="/regulations" class="btn-secondary">{{ 'common.back' | translate }}</a>
          <a [routerLink]="['/regulations/assess', result()!.regulationId]" class="btn-primary">
            {{ 'regulations.retake' | translate }}
          </a>
        </div>
      } @else if (noResult()) {
        <div class="card empty-state">
          <span class="material-icons-outlined">assessment</span>
          <h3>{{ 'regulations.no_assessment' | translate }}</h3>
          <p>{{ 'regulations.no_assessment_desc' | translate }}</p>
          <a routerLink="/regulations" class="btn-primary">{{ 'common.back' | translate }}</a>
        </div>
      } @else {
        <div class="loading">{{ 'common.loading' | translate }}</div>
      }
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.5rem; h1 { font-size: 1.5rem; margin-bottom: 0.25rem; } .subtitle { color: var(--text-secondary); font-size: 0.875rem; } }
    .card { background: white; border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 1.25rem; }
    .score-card { margin-bottom: 1.5rem; }
    .overall-score { display: flex; align-items: center; gap: 2rem; }
    .score-ring {
      width: 120px; height: 120px; border-radius: 50%; display: flex; align-items: center;
      justify-content: center; border: 8px solid;
      &.low { border-color: #ef4444; }
      &.medium { border-color: #f59e0b; }
      &.high { border-color: #10b981; }
    }
    .score-number { font-size: 2rem; font-weight: 700; }
    .score-info h2 { font-size: 1.125rem; font-weight: 600; margin-bottom: 0.25rem; }
    .score-level { font-size: 1rem; font-weight: 600; &.low { color: #ef4444; } &.medium { color: #f59e0b; } &.high { color: #10b981; } }
    .section-title { font-size: 1.125rem; font-weight: 600; margin: 1.5rem 0 1rem; }
    .domains-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .domain-score-card {
      .domain-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
      h3 { font-size: 0.9375rem; font-weight: 600; margin-bottom: 0.75rem; }
    }
    .domain-code { font-size: 0.75rem; font-weight: 600; color: var(--primary); background: var(--primary-light); padding: 0.125rem 0.5rem; border-radius: var(--radius-sm); }
    .domain-questions { font-size: 0.75rem; color: var(--text-muted); }
    .domain-score-bar { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem; }
    .bar-bg { flex: 1; height: 10px; background: #f1f5f9; border-radius: 5px; overflow: hidden; }
    .bar-fill { height: 100%; border-radius: 5px; transition: width 0.5s; }
    .bar-fill.low { background: #ef4444; }
    .bar-fill.medium { background: #f59e0b; }
    .bar-fill.high { background: #10b981; }
    .domain-score-value { font-size: 0.875rem; font-weight: 600; min-width: 3rem; }
    .domain-weight { font-size: 0.75rem; color: var(--text-muted); }
    .actions { display: flex; gap: 0.75rem; }
    .btn-primary { display: inline-block; padding: 0.625rem 1.5rem; background: var(--primary); color: white; border-radius: var(--radius-md); font-size: 0.875rem; font-weight: 500; text-decoration: none; &:hover { opacity: 0.9; } }
    .btn-secondary { display: inline-block; padding: 0.625rem 1.5rem; border: 1px solid var(--border); border-radius: var(--radius-md); font-size: 0.875rem; text-decoration: none; color: var(--text-primary); &:hover { border-color: var(--primary); } }
    .empty-state { text-align: center; padding: 3rem; .material-icons-outlined { font-size: 3rem; color: var(--text-muted); margin-bottom: 1rem; } h3 { margin-bottom: 0.5rem; } p { color: var(--text-secondary); margin-bottom: 1.5rem; } }
    .loading { text-align: center; padding: 3rem; color: var(--text-muted); }
  `]
})
export class RegulationResultComponent implements OnInit {
  result = signal<AssessmentResult | null>(null);
  noResult = signal(false);

  constructor(
    private route: ActivatedRoute,
    private regulationService: RegulationService
  ) {}

  ngOnInit() {
    const regulationId = this.route.snapshot.paramMap.get('regulationId')!;
    this.regulationService.getLatestAssessment(regulationId).subscribe({
      next: res => {
        if (res.success && res.data) this.result.set(res.data);
        else this.noResult.set(true);
      },
      error: () => this.noResult.set(true)
    });
  }
}
