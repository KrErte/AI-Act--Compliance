import { Routes } from '@angular/router';

export const GPAI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./gpai-list/gpai-list.component').then(m => m.GpaiListComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./gpai-detail/gpai-detail.component').then(m => m.GpaiDetailComponent)
  }
];
