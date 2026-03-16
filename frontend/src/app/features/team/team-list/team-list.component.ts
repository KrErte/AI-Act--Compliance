import { Component, OnInit, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-team-list',
  standalone: true,
  imports: [TranslateModule, DatePipe, FormsModule, LoadingSpinnerComponent],
  template: `
    <div class="page-header">
      <h1>{{ 'team.title' | translate }}</h1>
      @if (authService.hasAnyRole('OWNER', 'ADMIN')) {
        <button class="btn btn-primary" (click)="showInviteForm = !showInviteForm">
          {{ 'team.invite_member' | translate }}
        </button>
      }
    </div>

    @if (showInviteForm) {
      <div class="card invite-form">
        <h3>{{ 'team.invite_member' | translate }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>{{ 'team.email' | translate }}</label>
            <input type="email" [(ngModel)]="inviteEmail" class="form-control" placeholder="colleague@company.com">
          </div>
          <div class="form-group">
            <label>{{ 'team.role' | translate }}</label>
            <select [(ngModel)]="inviteRole" class="form-control">
              <option value="ADMIN">{{ 'enums.role.ADMIN' | translate }}</option>
              <option value="COMPLIANCE_MANAGER">{{ 'enums.role.COMPLIANCE_MANAGER' | translate }}</option>
              <option value="VIEWER">{{ 'enums.role.VIEWER' | translate }}</option>
            </select>
          </div>
          <button class="btn btn-primary" (click)="sendInvite()" [disabled]="!inviteEmail">
            {{ 'common.invite' | translate }}
          </button>
        </div>
        @if (inviteMessage) {
          <p class="success-msg">{{ inviteMessage }}</p>
        }
      </div>
    }

    @if (loading) {
      <app-loading-spinner />
    } @else {
      <div class="card">
        <table class="team-table">
          <thead>
            <tr>
              <th>{{ 'common.name' | translate }}</th>
              <th>{{ 'team.email' | translate }}</th>
              <th>{{ 'team.role' | translate }}</th>
              <th>{{ 'team.joined' | translate }}</th>
              @if (authService.hasAnyRole('OWNER', 'ADMIN')) {
                <th>{{ 'common.actions' | translate }}</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (member of members; track member.id) {
              <tr>
                <td>{{ member.firstName }} {{ member.lastName }}</td>
                <td>{{ member.email }}</td>
                <td><span class="role-badge">{{ 'enums.role.' + member.role | translate }}</span></td>
                <td>{{ member.joinedAt | date:'mediumDate' }}</td>
                @if (authService.hasAnyRole('OWNER', 'ADMIN')) {
                  <td>
                    @if (member.role !== 'OWNER' && member.id !== authService.currentUser()?.id) {
                      <button class="btn btn-danger btn-sm" (click)="removeMember(member)">
                        {{ 'common.remove' | translate }}
                      </button>
                    }
                  </td>
                }
              </tr>
            }
          </tbody>
        </table>
      </div>

      @if (pendingInvitations.length > 0) {
        <div class="card" style="margin-top: 1.5rem;">
          <h3>{{ 'team.pending' | translate }}</h3>
          <table class="team-table">
            <thead>
              <tr>
                <th>{{ 'team.email' | translate }}</th>
                <th>{{ 'team.role' | translate }}</th>
                <th>{{ 'common.date' | translate }}</th>
                <th>{{ 'common.actions' | translate }}</th>
              </tr>
            </thead>
            <tbody>
              @for (inv of pendingInvitations; track inv.id) {
                <tr>
                  <td>{{ inv.email }}</td>
                  <td><span class="role-badge">{{ 'enums.role.' + inv.role | translate }}</span></td>
                  <td>{{ inv.createdAt | date:'mediumDate' }}</td>
                  <td>
                    <button class="btn btn-danger btn-sm" (click)="cancelInvitation(inv)">
                      {{ 'common.cancel' | translate }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    }
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.5rem; font-weight: 700; }
    .invite-form { margin-bottom: 1.5rem; }
    .invite-form h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; }
    .form-row { display: flex; gap: 1rem; align-items: flex-end; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-size: 0.8125rem; font-weight: 500; color: var(--text-secondary); }
    .form-control { padding: 0.5rem 0.75rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.875rem; }
    .team-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .team-table th { text-align: left; padding: 0.5rem 0.75rem; color: var(--text-secondary); font-weight: 500; border-bottom: 1px solid var(--border); }
    .team-table td { padding: 0.5rem 0.75rem; border-bottom: 1px solid var(--border-light, #f1f5f9); }
    .role-badge { padding: 0.125rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; background: #f0f9ff; color: #0369a1; }
    .btn-sm { padding: 0.25rem 0.5rem; font-size: 0.75rem; }
    .btn-danger { background: #ef4444; color: white; border: none; border-radius: var(--radius-sm); cursor: pointer; }
    .btn-danger:hover { background: #dc2626; }
    .success-msg { color: #16a34a; font-size: 0.875rem; margin-top: 0.5rem; }
  `]
})
export class TeamListComponent implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  members: any[] = [];
  pendingInvitations: any[] = [];
  loading = true;
  showInviteForm = false;
  inviteEmail = '';
  inviteRole = 'VIEWER';
  inviteMessage = '';

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.api.get<any[]>('/team/members').subscribe({
      next: res => { this.members = res.data || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
    if (this.authService.hasAnyRole('OWNER', 'ADMIN')) {
      this.api.get<any[]>('/team/invitations').subscribe({
        next: res => { this.pendingInvitations = res.data || []; }
      });
    }
  }

  sendInvite(): void {
    this.api.post<any>('/team/invitations', { email: this.inviteEmail, role: this.inviteRole }).subscribe({
      next: () => {
        this.inviteMessage = 'Invitation sent!';
        this.inviteEmail = '';
        this.loadData();
        setTimeout(() => this.inviteMessage = '', 3000);
      }
    });
  }

  removeMember(member: any): void {
    if (confirm('Remove this team member?')) {
      this.api.delete(`/team/members/${member.id}`).subscribe({ next: () => this.loadData() });
    }
  }

  cancelInvitation(inv: any): void {
    this.api.delete(`/team/invitations/${inv.id}`).subscribe({ next: () => this.loadData() });
  }
}
