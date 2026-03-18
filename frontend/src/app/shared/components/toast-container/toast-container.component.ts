import { Component, inject } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast toast-{{ toast.type }}" (click)="toastService.remove(toast.id)">
          <span class="toast-icon material-icons-outlined">
            @switch (toast.type) {
              @case ('success') { check_circle }
              @case ('error') { error }
              @case ('warning') { warning }
              @case ('info') { info }
            }
          </span>
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" (click)="toastService.remove(toast.id); $event.stopPropagation()">
            <span class="material-icons-outlined">close</span>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 10000;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 420px;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.875rem 1rem;
      border-radius: var(--radius-md, 8px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      font-size: 0.875rem;
      cursor: pointer;
      animation: slideIn 0.3s ease-out;
      min-width: 300px;
    }

    .toast-success {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      color: #166534;
    }

    .toast-error {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #991b1b;
    }

    .toast-warning {
      background: #fffbeb;
      border: 1px solid #fde68a;
      color: #92400e;
    }

    .toast-info {
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      color: #1e40af;
    }

    .toast-icon {
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .toast-success .toast-icon { color: #16a34a; }
    .toast-error .toast-icon { color: #dc2626; }
    .toast-warning .toast-icon { color: #d97706; }
    .toast-info .toast-icon { color: #2563eb; }

    .toast-message {
      flex: 1;
      line-height: 1.4;
    }

    .toast-close {
      background: none;
      border: none;
      cursor: pointer;
      opacity: 0.5;
      padding: 0;
      display: flex;
      flex-shrink: 0;
    }

    .toast-close:hover {
      opacity: 1;
    }

    .toast-close .material-icons-outlined {
      font-size: 1rem;
    }

    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}
