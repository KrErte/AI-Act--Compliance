import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RegulationService, RegulationDetail, RegulationDomain, AnswerInput } from '../../../core/services/regulation.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-regulation-assessment',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="assessment-page">
      @if (regulation()) {
        <div class="page-header">
          <h1>{{ regulation()!.name }}</h1>
          <p class="subtitle">{{ regulation()!.description }}</p>
        </div>

        <!-- Progress Bar -->
        <div class="progress-section">
          <div class="progress-info">
            <span>{{ 'regulations.domain' | translate }} {{ currentDomainIndex() + 1 }} / {{ regulation()!.domains.length }}</span>
            <span>{{ answeredCount() }} / {{ totalQuestions() }} {{ 'regulations.questions_answered' | translate }}</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" [style.width.%]="progressPercent()"></div>
          </div>
        </div>

        <!-- Domain Header -->
        @if (currentDomain()) {
          <div class="card domain-card">
            <div class="domain-header">
              <span class="domain-code">{{ currentDomain()!.code }}</span>
              <span class="domain-weight">{{ 'regulations.weight' | translate }}: {{ currentDomain()!.weight }}x</span>
            </div>
            <h2>{{ currentDomain()!.name }}</h2>
            <p class="domain-desc">{{ currentDomain()!.description }}</p>
          </div>

          <!-- Questions -->
          <div class="questions-list">
            @for (question of currentDomain()!.questions; track question.id; let i = $index) {
              <div class="card question-card">
                <div class="question-num">Q{{ i + 1 }}</div>
                <p class="question-text">{{ question.questionEn }}</p>
                @if (question.articleRef) {
                  <span class="article-ref">{{ question.articleRef }}</span>
                }
                @if (question.explanationEn) {
                  <p class="explanation">{{ question.explanationEn }}</p>
                }
                <div class="answer-options">
                  @for (option of answerOptions; track option.value) {
                    <button class="answer-btn"
                            [class.selected]="getAnswer(question.id) === option.value"
                            (click)="setAnswer(question.id, option.value)">
                      <span class="answer-value">{{ option.value }}</span>
                      <span class="answer-label">{{ option.label }}</span>
                    </button>
                  }
                </div>
                @if (question.recommendationEn && getAnswer(question.id) !== null && getAnswer(question.id)! < 3) {
                  <div class="recommendation">
                    <span class="material-icons-outlined">lightbulb</span>
                    {{ question.recommendationEn }}
                  </div>
                }
              </div>
            }
          </div>

          <!-- Navigation -->
          <div class="nav-buttons">
            <button class="btn-secondary" [disabled]="currentDomainIndex() === 0" (click)="prevDomain()">
              {{ 'common.previous' | translate }}
            </button>
            @if (currentDomainIndex() < regulation()!.domains.length - 1) {
              <button class="btn-primary" (click)="nextDomain()">
                {{ 'common.next' | translate }}
              </button>
            } @else {
              <button class="btn-primary submit" (click)="submit()" [disabled]="submitting()">
                {{ submitting() ? ('common.saving' | translate) : ('regulations.submit_assessment' | translate) }}
              </button>
            }
          </div>
        }
      } @else {
        <div class="loading">{{ 'common.loading' | translate }}</div>
      }
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
      h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
      .subtitle { color: var(--text-secondary); font-size: 0.875rem; }
    }
    .progress-section { margin-bottom: 1.5rem; }
    .progress-info { display: flex; justify-content: space-between; font-size: 0.8125rem; color: var(--text-secondary); margin-bottom: 0.5rem; }
    .progress-bar { height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--primary); border-radius: 4px; transition: width 0.3s; }
    .card {
      background: white; border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 1.25rem;
    }
    .domain-card { margin-bottom: 1rem; }
    .domain-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .domain-code {
      font-size: 0.75rem; font-weight: 600; color: var(--primary); background: var(--primary-light);
      padding: 0.125rem 0.5rem; border-radius: var(--radius-sm);
    }
    .domain-weight { font-size: 0.75rem; color: var(--text-muted); }
    .domain-card h2 { font-size: 1.125rem; font-weight: 600; margin-bottom: 0.25rem; }
    .domain-desc { font-size: 0.875rem; color: var(--text-secondary); }
    .questions-list { display: flex; flex-direction: column; gap: 1rem; margin-bottom: 1.5rem; }
    .question-card { position: relative; padding-left: 3rem; }
    .question-num {
      position: absolute; left: 1.25rem; top: 1.25rem;
      font-size: 0.75rem; font-weight: 600; color: var(--primary);
    }
    .question-text { font-size: 0.9375rem; font-weight: 500; margin-bottom: 0.5rem; line-height: 1.5; }
    .article-ref {
      display: inline-block; font-size: 0.75rem; color: var(--text-muted); background: #f1f5f9;
      padding: 0.125rem 0.375rem; border-radius: var(--radius-sm); margin-bottom: 0.5rem;
    }
    .explanation { font-size: 0.8125rem; color: var(--text-secondary); line-height: 1.5; margin-bottom: 0.75rem; }
    .answer-options { display: flex; gap: 0.5rem; margin-top: 0.5rem; flex-wrap: wrap; }
    .answer-btn {
      display: flex; flex-direction: column; align-items: center; gap: 0.125rem;
      padding: 0.5rem 0.75rem; border: 2px solid var(--border); border-radius: var(--radius-md);
      background: white; cursor: pointer; transition: all 0.15s; min-width: 5rem;
      &:hover { border-color: var(--primary); }
      &.selected { border-color: var(--primary); background: var(--primary-light); }
    }
    .answer-value { font-size: 1rem; font-weight: 700; color: var(--text-primary); }
    .answer-label { font-size: 0.6875rem; color: var(--text-muted); }
    .recommendation {
      display: flex; align-items: flex-start; gap: 0.5rem; margin-top: 0.75rem; padding: 0.75rem;
      background: #fef3c7; border-radius: var(--radius-sm); font-size: 0.8125rem; color: #92400e;
      .material-icons-outlined { font-size: 1.125rem; flex-shrink: 0; margin-top: 1px; }
    }
    .nav-buttons { display: flex; justify-content: space-between; }
    .btn-primary {
      padding: 0.625rem 1.5rem; background: var(--primary); color: white; border: none;
      border-radius: var(--radius-md); font-size: 0.875rem; font-weight: 500; cursor: pointer;
      &:hover { opacity: 0.9; }
      &:disabled { opacity: 0.5; cursor: not-allowed; }
      &.submit { background: #10b981; }
    }
    .btn-secondary {
      padding: 0.625rem 1.5rem; border: 1px solid var(--border); background: white;
      border-radius: var(--radius-md); font-size: 0.875rem; cursor: pointer;
      &:disabled { opacity: 0.5; cursor: not-allowed; }
      &:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
    }
    .loading { text-align: center; padding: 3rem; color: var(--text-muted); }
  `]
})
export class RegulationAssessmentComponent implements OnInit {
  regulation = signal<RegulationDetail | null>(null);
  currentDomainIndex = signal(0);
  answers = signal<Map<string, number>>(new Map());
  submitting = signal(false);

  answerOptions = [
    { value: 0, label: 'Not started' },
    { value: 1, label: 'Initial' },
    { value: 2, label: 'Developing' },
    { value: 3, label: 'Defined' },
    { value: 4, label: 'Optimized' }
  ];

  currentDomain = signal<RegulationDomain | null>(null);
  totalQuestions = signal(0);
  answeredCount = signal(0);
  progressPercent = signal(0);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private regulationService: RegulationService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.regulationService.getDetail(id).subscribe(res => {
      if (res.success && res.data) {
        this.regulation.set(res.data);
        const total = res.data.domains.reduce((sum, d) => sum + d.questions.length, 0);
        this.totalQuestions.set(total);
        this.updateCurrentDomain();
      }
    });
  }

  getAnswer(questionId: string): number | null {
    return this.answers().get(questionId) ?? null;
  }

  setAnswer(questionId: string, value: number) {
    const map = new Map(this.answers());
    map.set(questionId, value);
    this.answers.set(map);
    this.answeredCount.set(map.size);
    this.progressPercent.set(Math.round((map.size / this.totalQuestions()) * 100));
  }

  nextDomain() {
    if (this.currentDomainIndex() < this.regulation()!.domains.length - 1) {
      this.currentDomainIndex.update(i => i + 1);
      this.updateCurrentDomain();
      window.scrollTo(0, 0);
    }
  }

  prevDomain() {
    if (this.currentDomainIndex() > 0) {
      this.currentDomainIndex.update(i => i - 1);
      this.updateCurrentDomain();
      window.scrollTo(0, 0);
    }
  }

  submit() {
    const reg = this.regulation();
    if (!reg) return;

    this.submitting.set(true);
    const answerInputs: AnswerInput[] = [];
    this.answers().forEach((answer, questionId) => {
      answerInputs.push({ questionId, answer });
    });

    this.regulationService.submitAssessment(reg.id, answerInputs).subscribe({
      next: res => {
        if (res.success) {
          this.toast.success('Assessment submitted successfully');
          this.router.navigate(['/regulations/result', reg.id]);
        }
        this.submitting.set(false);
      },
      error: () => this.submitting.set(false)
    });
  }

  private updateCurrentDomain() {
    const reg = this.regulation();
    if (reg && reg.domains.length > 0) {
      this.currentDomain.set(reg.domains[this.currentDomainIndex()]);
    }
  }
}
