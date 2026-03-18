import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse } from '../models/api-response.model';

export interface RegulationSummary {
  id: string;
  code: string;
  name: string;
  description: string;
  effectiveDate: string;
  domainCount: number;
  questionCount: number;
}

export interface RegulationDetail {
  id: string;
  code: string;
  name: string;
  description: string;
  effectiveDate: string;
  domains: RegulationDomain[];
}

export interface RegulationDomain {
  id: string;
  code: string;
  name: string;
  description: string;
  weight: number;
  questions: RegulationQuestion[];
}

export interface RegulationQuestion {
  id: string;
  questionEn: string;
  questionEt: string;
  articleRef: string;
  explanationEn: string;
  recommendationEn: string;
}

export interface AssessmentResult {
  id: string;
  regulationId: string;
  regulationName: string;
  overallScore: number;
  domainScores: DomainScore[];
  completedAt: string;
}

export interface DomainScore {
  domainName: string;
  domainCode: string;
  score: number;
  weight: number;
  answeredQuestions: number;
  totalQuestions: number;
}

export interface AnswerInput {
  questionId: string;
  answer: number;
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class RegulationService {
  constructor(private api: ApiService) {}

  getAll(): Observable<ApiResponse<RegulationSummary[]>> {
    return this.api.get<RegulationSummary[]>('/regulations');
  }

  getDetail(id: string): Observable<ApiResponse<RegulationDetail>> {
    return this.api.get<RegulationDetail>(`/regulations/${id}`);
  }

  getByCode(code: string): Observable<ApiResponse<RegulationDetail>> {
    return this.api.get<RegulationDetail>(`/regulations/code/${code}`);
  }

  submitAssessment(regulationId: string, answers: AnswerInput[]): Observable<ApiResponse<AssessmentResult>> {
    return this.api.post<AssessmentResult>('/regulations/assessments', { regulationId, answers });
  }

  getLatestAssessment(regulationId: string): Observable<ApiResponse<AssessmentResult>> {
    return this.api.get<AssessmentResult>(`/regulations/assessments/${regulationId}/latest`);
  }

  getAssessmentHistory(): Observable<ApiResponse<AssessmentResult[]>> {
    return this.api.get<AssessmentResult[]>('/regulations/assessments/history');
  }
}
