import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { DashboardComponent, DashboardSummary } from './dashboard.component';
import { ApiService } from '../../core/services/api.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;

  const mockSummary: DashboardSummary = {
    totalAiSystems: 5,
    riskDistribution: { HIGH: 2, LIMITED: 1, MINIMAL: 2 },
    overallComplianceScore: 72,
    obligationCounts: { total: 20, completed: 14, inProgress: 4, notStarted: 2 },
    upcomingDeadlines: [
      { title: 'FRIA Report', date: '2026-08-01', aiSystemName: 'HR Screening AI' }
    ]
  };

  beforeEach(async () => {
    apiServiceSpy = jasmine.createSpyObj('ApiService', ['get']);
    apiServiceSpy.get.and.returnValue(of({
      success: true,
      data: mockSummary,
      timestamp: new Date().toISOString()
    }));

    await TestBed.configureTestingModule({
      imports: [
        DashboardComponent,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = true', () => {
    expect(component.loading).toBeTrue();
  });

  it('should load dashboard summary on init', () => {
    fixture.detectChanges(); // triggers ngOnInit

    expect(apiServiceSpy.get).toHaveBeenCalledWith('/dashboard/summary');
    expect(component.summary).toEqual(mockSummary);
    expect(component.loading).toBeFalse();
  });

  it('should populate riskEntries from the response', () => {
    fixture.detectChanges();

    expect(component.riskEntries.length).toBe(3);
    expect(component.riskEntries).toContain(['HIGH', 2]);
    expect(component.riskEntries).toContain(['LIMITED', 1]);
    expect(component.riskEntries).toContain(['MINIMAL', 2]);
  });

  it('should set loading to false on error', () => {
    apiServiceSpy.get.and.returnValue(throwError(() => new Error('Network error')));

    fixture.detectChanges();

    expect(component.loading).toBeFalse();
    expect(component.summary).toBeNull();
  });
});
