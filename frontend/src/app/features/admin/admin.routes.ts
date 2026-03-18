import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'users',
    loadComponent: () => import('./admin-users/admin-users.component').then(m => m.AdminUsersComponent)
  },
  {
    path: 'audit-log',
    loadComponent: () => import('./admin-audit-log/admin-audit-log.component').then(m => m.AdminAuditLogComponent)
  }
];
