import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  let component: StatusBadgeComponent;
  let fixture: ComponentFixture<StatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadgeComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(StatusBadgeComponent);
    component = fixture.componentInstance;
  });

  it('should create with risk type by default', () => {
    component.value = 'HIGH';
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(component.type).toBe('risk');
  });

  it('should return correct cssClass for risk type', () => {
    component.value = 'HIGH';
    component.type = 'risk';
    fixture.detectChanges();

    expect(component.cssClass).toBe('risk-badge high');
  });

  it('should return correct cssClass for obligation type with underscore value', () => {
    component.value = 'IN_PROGRESS';
    component.type = 'obligation';
    fixture.detectChanges();

    expect(component.cssClass).toBe('status-badge in-progress');
  });
});
