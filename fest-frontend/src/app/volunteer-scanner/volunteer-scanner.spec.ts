import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VolunteerScanner } from './volunteer-scanner';

describe('VolunteerScanner', () => {
  let component: VolunteerScanner;
  let fixture: ComponentFixture<VolunteerScanner>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VolunteerScanner],
    }).compileComponents();

    fixture = TestBed.createComponent(VolunteerScanner);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
