import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./settings.component').then(m => m.SettingsComponent)
  },
  {
    path: 'subscription',
    loadComponent: () => import('./subscription/subscription.component').then(m => m.SubscriptionComponent)
  }
];
