import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <span class="status-badge" [class]="cssClass">
      {{ ('enums.' + type + '.' + value) | translate }}
    </span>
  `
})
export class StatusBadgeComponent {
  @Input({ required: true }) value!: string;
  @Input() type: 'risk' | 'compliance' | 'obligation' = 'risk';

  get cssClass(): string {
    switch (this.type) {
      case 'risk':
        return 'risk-badge ' + this.value.toLowerCase();
      case 'compliance':
      case 'obligation':
        return 'status-badge ' + this.value.toLowerCase().replace(/_/g, '-');
      default:
        return '';
    }
  }
}
