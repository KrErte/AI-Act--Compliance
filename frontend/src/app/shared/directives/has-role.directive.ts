import { Directive, Input, TemplateRef, ViewContainerRef, inject, effect } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective {
  private templateRef = inject(TemplateRef);
  private viewContainer = inject(ViewContainerRef);
  private authService = inject(AuthService);
  private rendered = false;

  @Input() set appHasRole(roles: string | string[]) {
    const roleArray = Array.isArray(roles) ? roles : [roles];
    effect(() => {
      const hasRole = this.authService.hasAnyRole(...roleArray);
      if (hasRole && !this.rendered) {
        this.viewContainer.createEmbeddedView(this.templateRef);
        this.rendered = true;
      } else if (!hasRole && this.rendered) {
        this.viewContainer.clear();
        this.rendered = false;
      }
    });
  }
}
