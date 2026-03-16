import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layouts/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
      },
      {
        path: 'ai-systems',
        loadChildren: () => import('./features/ai-systems/ai-systems.routes').then(m => m.AI_SYSTEMS_ROUTES)
      },
      {
        path: 'compliance',
        loadChildren: () => import('./features/compliance/compliance.routes').then(m => m.COMPLIANCE_ROUTES)
      },
      {
        path: 'organization',
        loadChildren: () => import('./features/organization/organization.routes').then(m => m.ORGANIZATION_ROUTES)
      },
      {
        path: 'team',
        loadChildren: () => import('./features/team/team.routes').then(m => m.TEAM_ROUTES)
      },
      {
        path: 'gpai-models',
        loadChildren: () => import('./features/gpai/gpai.routes').then(m => m.GPAI_ROUTES)
      },
      {
        path: 'my-tasks',
        loadChildren: () => import('./features/tasks/tasks.routes').then(m => m.TASKS_ROUTES)
      },
      {
        path: 'analytics',
        loadChildren: () => import('./features/analytics/analytics.routes').then(m => m.ANALYTICS_ROUTES)
      },
      {
        path: 'regulations',
        loadChildren: () => import('./features/regulations/regulations.routes').then(m => m.REGULATIONS_ROUTES)
      },
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/profile.routes').then(m => m.PROFILE_ROUTES)
      },
      {
        path: 'settings',
        loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
      }
    ]
  },
  {
    path: 'auth',
    loadComponent: () => import('./layouts/auth-layout/auth-layout.component').then(m => m.AuthLayoutComponent),
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'classify',
    loadComponent: () => import('./layouts/public-layout/public-layout.component').then(m => m.PublicLayoutComponent),
    loadChildren: () => import('./features/public-classifier/public-classifier.routes').then(m => m.PUBLIC_CLASSIFIER_ROUTES)
  },
  { path: '**', redirectTo: '' }
];
