import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditFacilityComponent } from './edit-facility.component';

describe('EditFacilityComponent', () => {
  let component: EditFacilityComponent;
  let fixture: ComponentFixture<EditFacilityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditFacilityComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditFacilityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
