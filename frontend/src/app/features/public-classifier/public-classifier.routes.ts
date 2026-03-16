import { Routes } from '@angular/router';

export const PUBLIC_CLASSIFIER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./public-classifier.component').then(m => m.PublicClassifierComponent)
  }
];
