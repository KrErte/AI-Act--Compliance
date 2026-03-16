import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <div class="spinner-container" [class.overlay]="overlay">
      <div class="spinner"></div>
    </div>
  `,
  styles: [`
    .spinner-container {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }
    .spinner-container.overlay {
      position: absolute;
      inset: 0;
      background: rgba(255, 255, 255, 0.8);
      z-index: 10;
    }
    .spinner {
      width: 2rem;
      height: 2rem;
      border: 3px solid var(--border);
      border-top-color: var(--primary);
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class LoadingSpinnerComponent {
  @Input() overlay = false;
}
