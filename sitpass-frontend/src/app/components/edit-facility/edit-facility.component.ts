import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';
import { Discipline } from '../../model/discipline.model';
import { WorkDay } from '../../model/workday.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-facility',
  templateUrl: './edit-facility.component.html',
  styleUrls: ['./edit-facility.component.css'],
  imports: [FormsModule, CommonModule],
  standalone: true,
})
export class EditFacilityComponent implements OnInit {
  facilityId: number = 0;
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
    createdAt: new Date().toISOString().split('T')[0],
  };

  disciplines: Discipline[] = [];
  workDays: WorkDay[] = [];

  constructor(
    private facilityService: FacilityService,
    private router: Router,
    private route: ActivatedRoute // Dodato za preuzimanje parametra rute
  ) {}

  ngOnInit(): void {
    // Preuzimanje ID objekta iz URL-a
    this.facilityId = this.route.snapshot.params['id'];

    // Učitavanje objekta sa servera
    this.facilityService.getFacilityById(this.facilityId).subscribe(
      (data) => {
        this.facility = data;
        this.disciplines = data.disciplines;
        this.workDays = data.workDays;
      },
      (error) => {
        console.error('Error loading facility', error);
      }
    );
  }

  addDiscipline(): void {
    this.disciplines.push({ id: 0, name: '', facility: this.facility });
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
      facility: this.facility,
    });
  }

  removeWorkDay(index: number): void {
    this.workDays.splice(index, 1);
  }

  onSubmit(): void {
    this.facility.disciplines = this.disciplines;
    this.facility.workDays = this.workDays;

    this.facilityService.editFacility(this.facilityId, this.facility).subscribe(
      () => {
        alert('Facility updated successfully');
        this.router.navigate(['/facilities']);
      },
      (error) => {
        console.error('Error updating facility', error);
      }
    );
  }
}
