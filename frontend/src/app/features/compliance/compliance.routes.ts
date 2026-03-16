import { Routes } from '@angular/router';

export const COMPLIANCE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./compliance-list/compliance-list.component').then(m => m.ComplianceListComponent)
  },
  {
    path: ':aiSystemId',
    loadComponent: () => import('./compliance-checklist/compliance-checklist.component').then(m => m.ComplianceChecklistComponent)
  }
];
