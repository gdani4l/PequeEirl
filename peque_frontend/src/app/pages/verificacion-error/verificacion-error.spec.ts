import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificacionError } from './verificacion-error';

describe('VerificacionError', () => {
  let component: VerificacionError;
  let fixture: ComponentFixture<VerificacionError>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerificacionError],
    }).compileComponents();

    fixture = TestBed.createComponent(VerificacionError);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
