import { Routes } from '@angular/router';

export const REGULATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./regulations.component').then(m => m.RegulationsComponent)
  },
  {
    path: 'assess/:id',
    loadComponent: () => import('./regulation-assessment/regulation-assessment.component').then(m => m.RegulationAssessmentComponent)
  },
  {
    path: 'result/:regulationId',
    loadComponent: () => import('./regulation-result/regulation-result.component').then(m => m.RegulationResultComponent)
  }
];
