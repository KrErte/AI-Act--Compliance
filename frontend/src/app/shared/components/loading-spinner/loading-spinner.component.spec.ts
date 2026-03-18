import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingSpinnerComponent } from './loading-spinner.component';

describe('LoadingSpinnerComponent', () => {
  let component: LoadingSpinnerComponent;
  let fixture: ComponentFixture<LoadingSpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingSpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create without overlay by default', () => {
    expect(component).toBeTruthy();
    expect(component.overlay).toBeFalse();

    const container = fixture.nativeElement.querySelector('.spinner-container');
    expect(container.classList.contains('overlay')).toBeFalse();
  });

  it('should add overlay class when overlay input is true', () => {
    component.overlay = true;
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector('.spinner-container');
    expect(container.classList.contains('overlay')).toBeTrue();
  });
});
