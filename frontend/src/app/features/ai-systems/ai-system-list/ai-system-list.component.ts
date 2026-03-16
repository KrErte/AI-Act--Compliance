import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService } from '../../../core/services/api.service';
import { PagedResponse } from '../../../core/models/api-response.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

interface AiSystemSummary {
  id: string;
  name: string;
  vendor: string;
  status: string;
  riskLevel: string | null;
  complianceScore: number;
  complianceStatus: string;
  deploymentContext: string | null;
  createdAt: string;
}

@Component({
  selector: 'app-ai-system-list',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LoadingSpinnerComponent, StatusBadgeComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <h1>{{ 'ai_systems.title' | translate }}</h1>
        <a routerLink="/ai-systems/new" class="btn btn-primary">
          <span class="material-icons-outlined">add</span>
          {{ 'ai_systems.new_system' | translate }}
        </a>
      </div>

      <div class="filters card">
        <input type="text" [(ngModel)]="search" (input)="loadSystems()"
               [placeholder]="'ai_systems.search_placeholder' | translate" class="search-input">
        <select [(ngModel)]="riskFilter" (change)="loadSystems()">
          <option value="">{{ 'ai_systems.filter_risk' | translate }}</option>
          <option value="MINIMAL">Minimal</option>
          <option value="LIMITED">Limited</option>
          <option value="HIGH">High</option>
          <option value="UNACCEPTABLE">Unacceptable</option>
        </select>
        <select [(ngModel)]="statusFilter" (change)="loadSystems()">
          <option value="">{{ 'ai_systems.filter_status' | translate }}</option>
          <option value="DRAFT">Draft</option>
          <option value="ACTIVE">Active</option>
          <option value="RETIRED">Retired</option>
        </select>
      </div>

      @if (loading) {
        <app-loading-spinner />
      } @else if (systems.length === 0) {
        <div class="card empty-state">
          <span class="material-icons-outlined empty-icon">smart_toy</span>
          <h3>{{ 'ai_systems.no_systems' | translate }}</h3>
          <p>{{ 'ai_systems.no_systems_desc' | translate }}</p>
          <a routerLink="/ai-systems/new" class="btn btn-primary">{{ 'ai_systems.new_system' | translate }}</a>
        </div>
      } @else {
        <div class="systems-grid">
          @for (system of systems; track system.id) {
            <a [routerLink]="['/ai-systems', system.id]" class="card system-card">
              <div class="system-header">
                <h3>{{ system.name }}</h3>
                @if (system.riskLevel) {
                  <app-status-badge [value]="system.riskLevel" type="risk" />
                }
              </div>
              <div class="system-meta">
                @if (system.vendor) { <span>{{ system.vendor }}</span> }
                <span class="compliance-score">{{ system.complianceScore }}%</span>
              </div>
            </a>
          }
        </div>

        @if (totalPages > 1) {
          <div class="pagination">
            <button class="btn btn-secondary" [disabled]="currentPage === 0" (click)="changePage(currentPage - 1)">
              {{ 'common.previous' | translate }}
            </button>
            <span>{{ currentPage + 1 }} / {{ totalPages }}</span>
            <button class="btn btn-secondary" [disabled]="currentPage >= totalPages - 1" (click)="changePage(currentPage + 1)">
              {{ 'common.next' | translate }}
            </button>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h1 { font-size: 1.5rem; }
    .filters { display: flex; gap: 1rem; margin-bottom: 1.5rem; padding: 1rem; }
    .search-input { flex: 1; }
    .filters select { min-width: 150px; padding: 0.625rem; border: 1px solid var(--border); border-radius: var(--radius-md); }
    .systems-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; }
    .system-card { text-decoration: none; color: inherit; transition: box-shadow 0.15s; cursor: pointer; }
    .system-card:hover { box-shadow: var(--shadow-md); }
    .system-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; }
    .system-header h3 { font-size: 1rem; font-weight: 600; }
    .system-meta { display: flex; justify-content: space-between; margin-top: 0.75rem; font-size: 0.875rem; color: var(--text-secondary); }
    .compliance-score { font-weight: 600; }
    .empty-state { text-align: center; padding: 3rem; }
    .empty-icon { font-size: 3rem; color: var(--text-muted); margin-bottom: 1rem; }
    .empty-state h3 { margin-bottom: 0.5rem; }
    .empty-state p { color: var(--text-secondary); margin-bottom: 1.5rem; }
    .pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
  `]
})
export class AiSystemListComponent implements OnInit {
  private api = inject(ApiService);

  systems: AiSystemSummary[] = [];
  loading = true;
  search = '';
  riskFilter = '';
  statusFilter = '';
  currentPage = 0;
  totalPages = 0;

  ngOnInit(): void {
    this.loadSystems();
  }

  loadSystems(): void {
    this.loading = true;
    const params: Record<string, string | number> = { page: this.currentPage, size: 20 };
    if (this.search) params['search'] = this.search;
    if (this.riskFilter) params['riskLevel'] = this.riskFilter;
    if (this.statusFilter) params['status'] = this.statusFilter;

    this.api.getPaged<AiSystemSummary>('/ai-systems', params).subscribe({
      next: res => {
        if (res.data) {
          this.systems = res.data.content;
          this.totalPages = res.data.totalPages;
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadSystems();
  }
}
