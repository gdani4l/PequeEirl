import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCuentas } from './admin-cuentas';

describe('AdminCuentas', () => {
  let component: AdminCuentas;
  let fixture: ComponentFixture<AdminCuentas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCuentas],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCuentas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
