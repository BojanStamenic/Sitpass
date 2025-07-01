import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';
import { Discipline } from '../../model/discipline.model';
import { WorkDay } from '../../model/workday.model';
import { FormsModule } from '@angular/forms'; // Dodajte FormsModule
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-facility',
  templateUrl: './add-facility.component.html',
  styleUrls: ['./add-facility.component.css'],
  imports: [FormsModule, CommonModule], // Dodajte FormsModule ovde
  standalone: true,
})
export class AddFacilityComponent {
  facility: Facility = {
    id: 0,
    name: '',
    description: '',
    address: '',
    city: '',
    totalRating: 0,
    active: true,
    images: [],
    disciplines: [],
    workDays: [],
    createdAt: new Date().toISOString().split('T')[0], // Dodajemo createdAt sa trenutnim datumom
  };

  disciplines: Discipline[] = [{ id: 0, name: '', facility: {} as Facility }];
  workDays: WorkDay[] = [
    {
      id: 0,
      day: '',
      startTime: '',
      endTime: '',
      validFrom: '',
      facility: {} as Facility,
    },
  ];

  constructor(
    private facilityService: FacilityService,
    private router: Router
  ) {}

  addDiscipline(): void {
    this.disciplines.push({ id: 0, name: '', facility: {} as Facility }); // Dodajemo prazni Facility
  }

  removeDiscipline(index: number): void {
    this.disciplines.splice(index, 1);
  }

  addWorkDay(): void {
    this.workDays.push({
      id: 0,
      day: '',
      startTime: '',
      endTime: '',
      validFrom: '',
      facility: {} as Facility,
    });
  }

  removeWorkDay(index: number): void {
    this.workDays.splice(index, 1);
  }

  onSubmit(): void {
    this.facility.disciplines = this.disciplines;
    this.facility.workDays = this.workDays;

    this.facilityService.addFacility(this.facility).subscribe(
      () => {
        alert('Facility added successfully');
        this.router.navigate(['/facilities']);
      },
      (error) => {
        console.error('Error adding facility', error);
      }
    );
  }
}
