import { Component, Input, OnInit, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [TranslateModule, DatePipe, LoadingSpinnerComponent],
  template: `
    <div class="doc-header">
      <h3>{{ 'documents.title' | translate }}</h3>
      <div class="doc-actions">
        <select class="form-control" #docType>
          <option value="FRIA">{{ 'documents.types.FRIA' | translate }}</option>
          <option value="TECHNICAL_DOC">{{ 'documents.types.TECHNICAL_DOC' | translate }}</option>
          <option value="RISK_MANAGEMENT">{{ 'documents.types.RISK_MANAGEMENT' | translate }}</option>
          <option value="HUMAN_OVERSIGHT">{{ 'documents.types.HUMAN_OVERSIGHT' | translate }}</option>
          <option value="DATA_GOVERNANCE">{{ 'documents.types.DATA_GOVERNANCE' | translate }}</option>
          <option value="CONFORMITY_DECLARATION">{{ 'documents.types.CONFORMITY_DECLARATION' | translate }}</option>
          <option value="POST_MARKET_MONITORING">{{ 'documents.types.POST_MARKET_MONITORING' | translate }}</option>
          <option value="TRANSPARENCY_NOTICE">{{ 'documents.types.TRANSPARENCY_NOTICE' | translate }}</option>
        </select>
        <button class="btn btn-primary" (click)="generateDocument(docType.value)" [disabled]="generating">
          {{ generating ? ('documents.generating' | translate) : ('documents.generate' | translate) }}
        </button>
      </div>
    </div>

    @if (loading) {
      <app-loading-spinner />
    } @else if (documents.length === 0) {
      <p class="text-muted">{{ 'documents.no_documents' | translate }}</p>
    } @else {
      <div class="doc-list">
        @for (doc of documents; track doc.id) {
          <div class="doc-card" [class.generating]="doc.status === 'GENERATING'">
            <div class="doc-info">
              <h4>{{ doc.title }}</h4>
              <div class="doc-meta">
                <span class="doc-type">{{ 'documents.types.' + doc.documentType | translate }}</span>
                <span class="doc-status" [class]="'status-' + doc.status.toLowerCase()">{{ doc.status }}</span>
                <span>v{{ doc.version }}</span>
                <span>{{ doc.createdAt | date:'medium' }}</span>
              </div>
            </div>
            <div class="doc-actions-row">
              @if (doc.status === 'COMPLETED') {
                <button class="btn btn-secondary btn-sm" (click)="viewDocument(doc)">{{ 'common.view' | translate }}</button>
                <button class="btn btn-secondary btn-sm" (click)="exportPdf(doc)">{{ 'documents.export_pdf' | translate }}</button>
                <button class="btn btn-secondary btn-sm" (click)="exportDocx(doc)">{{ 'documents.export_docx' | translate }}</button>
                <button class="btn btn-secondary btn-sm" (click)="regenerate(doc)">{{ 'documents.regenerate' | translate }}</button>
              }
              @if (doc.status === 'GENERATING') {
                <span class="generating-text">{{ 'documents.generating' | translate }}</span>
              }
              <button class="btn btn-danger btn-sm" (click)="deleteDocument(doc)">{{ 'common.delete' | translate }}</button>
            </div>
          </div>
        }
      </div>
    }

    @if (viewingDoc) {
      <div class="modal-overlay" (click)="viewingDoc = null">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>{{ viewingDoc.title }}</h3>
            <button class="btn btn-secondary btn-sm" (click)="viewingDoc = null">{{ 'common.close' | translate }}</button>
          </div>
          <div class="modal-body markdown-content" [innerHTML]="viewingDoc.content"></div>
        </div>
      </div>
    }
  `,
  styles: [`
    .doc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .doc-header h3 { font-size: 1rem; font-weight: 600; margin: 0; }
    .doc-actions { display: flex; gap: 0.5rem; align-items: center; }
    .form-control { padding: 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.8125rem; }
    .text-muted { color: var(--text-muted); font-size: 0.875rem; }
    .doc-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .doc-card { padding: 1rem; border: 1px solid var(--border); border-radius: var(--radius-md); }
    .doc-card.generating { opacity: 0.7; border-style: dashed; }
    .doc-info h4 { font-size: 0.9375rem; font-weight: 600; margin-bottom: 0.25rem; }
    .doc-meta { display: flex; gap: 0.75rem; font-size: 0.75rem; color: var(--text-secondary); align-items: center; }
    .doc-type { background: #eff6ff; color: #1d4ed8; padding: 0.125rem 0.5rem; border-radius: 9999px; font-weight: 500; }
    .doc-status { padding: 0.125rem 0.5rem; border-radius: 9999px; font-weight: 500; }
    .status-completed { background: #f0fdf4; color: #16a34a; }
    .status-generating { background: #fefce8; color: #ca8a04; }
    .status-failed { background: #fef2f2; color: #dc2626; }
    .doc-actions-row { display: flex; gap: 0.5rem; margin-top: 0.75rem; }
    .btn-sm { padding: 0.25rem 0.5rem; font-size: 0.75rem; }
    .btn-danger { background: #ef4444; color: white; border: none; border-radius: var(--radius-sm); cursor: pointer; }
    .generating-text { color: #ca8a04; font-size: 0.8125rem; }
    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; }
    .modal-content { background: white; border-radius: var(--radius-lg, 8px); max-width: 900px; width: 90vw; max-height: 90vh; overflow-y: auto; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; border-bottom: 1px solid var(--border); }
    .modal-header h3 { font-size: 1rem; margin: 0; }
    .modal-body { padding: 1.5rem; white-space: pre-wrap; font-size: 0.875rem; line-height: 1.6; }
  `]
})
export class DocumentListComponent implements OnInit {
  @Input() aiSystemId!: string;

  private api = inject(ApiService);

  documents: any[] = [];
  loading = true;
  generating = false;
  viewingDoc: any = null;

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.api.get<any[]>(`/ai-systems/${this.aiSystemId}/documents`).subscribe({
      next: res => { this.documents = res.data || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  generateDocument(type: string): void {
    this.generating = true;
    this.api.post<any>(`/ai-systems/${this.aiSystemId}/documents/generate`, { documentType: type }).subscribe({
      next: () => {
        this.generating = false;
        this.loadDocuments();
        // Poll for completion
        setTimeout(() => this.loadDocuments(), 5000);
        setTimeout(() => this.loadDocuments(), 15000);
        setTimeout(() => this.loadDocuments(), 30000);
      },
      error: () => { this.generating = false; }
    });
  }

  viewDocument(doc: any): void {
    this.viewingDoc = doc;
  }

  exportPdf(doc: any): void {
    window.open(`/api/v1/ai-systems/${this.aiSystemId}/documents/${doc.id}/export?format=pdf`, '_blank');
  }

  exportDocx(doc: any): void {
    window.open(`/api/v1/ai-systems/${this.aiSystemId}/documents/${doc.id}/export?format=docx`, '_blank');
  }

  regenerate(doc: any): void {
    this.api.post<any>(`/ai-systems/${this.aiSystemId}/documents/${doc.id}/regenerate`).subscribe({
      next: () => {
        this.loadDocuments();
        setTimeout(() => this.loadDocuments(), 10000);
      }
    });
  }

  deleteDocument(doc: any): void {
    if (confirm('Delete this document?')) {
      this.api.delete(`/ai-systems/${this.aiSystemId}/documents/${doc.id}`).subscribe({
        next: () => this.loadDocuments()
      });
    }
  }
}
