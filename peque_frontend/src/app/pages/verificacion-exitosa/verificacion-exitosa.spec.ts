import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificacionExitosa } from './verificacion-exitosa';

describe('VerificacionExitosa', () => {
  let component: VerificacionExitosa;
  let fixture: ComponentFixture<VerificacionExitosa>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerificacionExitosa],
    }).compileComponents();

    fixture = TestBed.createComponent(VerificacionExitosa);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
