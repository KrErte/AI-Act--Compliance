import { Routes } from '@angular/router';

export const REGULATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./regulations.component').then(m => m.RegulationsComponent)
  }
];
