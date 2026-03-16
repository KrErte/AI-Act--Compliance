import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

interface Question {
  id: string;
  questionKey: string;
  questionText: string;
  questionType: string;
  helpText: string;
  category: string;
  dependsOn: string | null;
  dependsOnAnswer: string | null;
}

interface ClassificationResult {
  riskLevel: string;
  rationale: string;
  applicableArticles: string[];
  recommendedActions: string[];
  deadline: string;
}

@Component({
  selector: 'app-risk-classifier',
  standalone: true,
  imports: [RouterLink, TranslateModule, LoadingSpinnerComponent, StatusBadgeComponent],
  template: `
    @if (loadingQuestions) {
      <app-loading-spinner />
    } @else if (result) {
      <div class="result">
        <h2>{{ 'classification.result_title' | translate }}</h2>
        <div class="card result-card">
          <div class="result-risk">
            <span class="label">{{ 'classification.risk_level' | translate }}</span>
            <app-status-badge [value]="result.riskLevel" type="risk" />
          </div>
          <div class="result-section">
            <h4>{{ 'classification.rationale' | translate }}</h4>
            <p>{{ result.rationale }}</p>
          </div>
          <div class="result-section">
            <h4>{{ 'classification.applicable_articles' | translate }}</h4>
            <ul>
              @for (article of result.applicableArticles; track article) {
                <li>{{ article }}</li>
              }
            </ul>
          </div>
          <div class="result-section">
            <h4>{{ 'classification.recommended_actions' | translate }}</h4>
            <ul>
              @for (action of result.recommendedActions; track action) {
                <li>{{ action }}</li>
              }
            </ul>
          </div>
          <div class="result-section">
            <h4>{{ 'classification.deadline' | translate }}</h4>
            <p>{{ result.deadline }}</p>
          </div>
        </div>
        <div class="result-actions">
          <a [routerLink]="['/ai-systems', aiSystemId]" class="btn btn-primary">{{ 'common.back' | translate }}</a>
        </div>
      </div>
    } @else {
      <div class="questionnaire">
        <a [routerLink]="['/ai-systems', aiSystemId]" class="back-link">&larr; {{ 'common.back' | translate }}</a>
        <h2>{{ 'classification.title' | translate }}</h2>
        <div class="progress-bar">
          <div class="progress-fill" [style.width.%]="progressPercent"></div>
        </div>
        <p class="progress-text">{{ 'classification.question' | translate }} {{ currentIndex + 1 }} {{ 'classification.of' | translate }} {{ visibleQuestions.length }}</p>

        @if (currentQuestion) {
          <div class="card question-card">
            <p class="question-text">{{ currentQuestion.questionText }}</p>
            @if (currentQuestion.helpText) {
              <p class="help-text">{{ currentQuestion.helpText }}</p>
            }
            <div class="answer-buttons">
              <button class="btn btn-answer" [class.selected]="answers[currentQuestion.questionKey] === 'YES'"
                      (click)="answer('YES')">{{ 'common.yes' | translate }}</button>
              <button class="btn btn-answer" [class.selected]="answers[currentQuestion.questionKey] === 'NO'"
                      (click)="answer('NO')">{{ 'common.no' | translate }}</button>
            </div>
          </div>
        }

        <div class="nav-buttons">
          <button class="btn btn-secondary" [disabled]="currentIndex === 0" (click)="prev()">{{ 'common.previous' | translate }}</button>
          @if (currentIndex < visibleQuestions.length - 1) {
            <button class="btn btn-primary" [disabled]="!answers[currentQuestion?.questionKey ?? '']" (click)="next()">
              {{ 'common.next' | translate }}
            </button>
          } @else {
            <button class="btn btn-primary" [disabled]="submitting" (click)="submit()">
              {{ submitting ? ('classification.running' | translate) : ('common.submit' | translate) }}
            </button>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .back-link { font-size: 0.875rem; color: var(--text-secondary); display: inline-block; margin-bottom: 0.5rem; }
    h2 { font-size: 1.5rem; margin-bottom: 1rem; }
    .progress-bar { height: 4px; background: var(--bg-tertiary); border-radius: 2px; margin-bottom: 0.5rem; }
    .progress-fill { height: 100%; background: var(--primary); border-radius: 2px; transition: width 0.3s; }
    .progress-text { font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 1.5rem; }
    .question-card { max-width: 700px; }
    .question-text { font-size: 1.05rem; font-weight: 500; margin-bottom: 0.75rem; }
    .help-text { font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 1rem; padding: 0.75rem; background: var(--bg-secondary); border-radius: var(--radius-md); }
    .answer-buttons { display: flex; gap: 1rem; }
    .btn-answer { min-width: 100px; padding: 0.75rem 1.5rem; border: 2px solid var(--border); background: white; border-radius: var(--radius-md); cursor: pointer; font-weight: 500; }
    .btn-answer.selected { border-color: var(--primary); background: var(--primary-light); color: var(--primary); }
    .nav-buttons { display: flex; justify-content: space-between; margin-top: 1.5rem; max-width: 700px; }
    .result-card { max-width: 700px; }
    .result-risk { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
    .result-section { margin-bottom: 1.25rem; }
    .result-section h4 { font-size: 0.875rem; font-weight: 600; margin-bottom: 0.5rem; color: var(--text-secondary); }
    .result-section ul { padding-left: 1.25rem; }
    .result-section li { margin-bottom: 0.25rem; font-size: 0.9rem; }
    .result-actions { margin-top: 1.5rem; }
    .label { font-size: 0.875rem; color: var(--text-secondary); }
  `]
})
export class RiskClassifierComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);

  aiSystemId = '';
  allQuestions: Question[] = [];
  visibleQuestions: Question[] = [];
  currentIndex = 0;
  answers: Record<string, string> = {};
  result: ClassificationResult | null = null;
  loadingQuestions = true;
  submitting = false;

  get currentQuestion(): Question | null {
    return this.visibleQuestions[this.currentIndex] ?? null;
  }

  get progressPercent(): number {
    return this.visibleQuestions.length ? ((this.currentIndex + 1) / this.visibleQuestions.length) * 100 : 0;
  }

  ngOnInit(): void {
    this.aiSystemId = this.route.snapshot.paramMap.get('id') ?? '';
    this.api.get<Question[]>(`/ai-systems/${this.aiSystemId}/classification/questions`).subscribe({
      next: res => {
        this.allQuestions = res.data ?? [];
        this.updateVisibleQuestions();
        this.loadingQuestions = false;
      },
      error: () => { this.loadingQuestions = false; }
    });
  }

  answer(value: string): void {
    if (this.currentQuestion) {
      this.answers[this.currentQuestion.questionKey] = value;
      this.updateVisibleQuestions();
    }
  }

  next(): void {
    if (this.currentIndex < this.visibleQuestions.length - 1) this.currentIndex++;
  }

  prev(): void {
    if (this.currentIndex > 0) this.currentIndex--;
  }

  submit(): void {
    this.submitting = true;
    this.api.post<ClassificationResult>(`/ai-systems/${this.aiSystemId}/classification/run`, { answers: this.answers })
      .subscribe({
        next: res => { this.result = res.data ?? null; this.submitting = false; },
        error: () => { this.submitting = false; }
      });
  }

  private updateVisibleQuestions(): void {
    this.visibleQuestions = this.allQuestions.filter(q => {
      if (!q.dependsOn) return true;
      return this.answers[q.dependsOn] === q.dependsOnAnswer;
    });
  }
}
