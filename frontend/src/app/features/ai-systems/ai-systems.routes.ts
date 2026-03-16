import { Routes } from '@angular/router';

export const AI_SYSTEMS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./ai-system-list/ai-system-list.component').then(m => m.AiSystemListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./ai-system-wizard/ai-system-wizard.component').then(m => m.AiSystemWizardComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./ai-system-detail/ai-system-detail.component').then(m => m.AiSystemDetailComponent)
  },
  {
    path: ':id/classify',
    loadComponent: () => import('../risk-classifier/risk-classifier.component').then(m => m.RiskClassifierComponent)
  }
];
