import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, PagedResponse } from '../models/api-response.model';

export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalAiSystems: number;
  overallComplianceScore: number;
  totalObligations: number;
  completedObligations: number;
  riskDistribution: Record<string, number>;
  usersByRole: Record<string, number>;
}

export interface AuditLogEntry {
  id: string;
  entityType: string;
  entityId: string;
  action: string;
  oldValue: string | null;
  newValue: string | null;
  userName: string | null;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private api: ApiService) {}

  getUsers(page = 0, size = 20): Observable<ApiResponse<PagedResponse<AdminUser>>> {
    return this.api.getPaged<AdminUser>('/admin/users', { page, size });
  }

  updateUser(id: string, data: { role?: string; enabled?: boolean }): Observable<ApiResponse<AdminUser>> {
    return this.api.put<AdminUser>(`/admin/users/${id}`, data);
  }

  deactivateUser(id: string): Observable<ApiResponse<void>> {
    return this.api.delete<void>(`/admin/users/${id}`);
  }

  getStats(): Observable<ApiResponse<AdminStats>> {
    return this.api.get<AdminStats>('/admin/stats');
  }

  getAuditLog(page = 0, size = 20): Observable<ApiResponse<PagedResponse<AuditLogEntry>>> {
    return this.api.getPaged<AuditLogEntry>('/admin/audit-log', { page, size });
  }
}
