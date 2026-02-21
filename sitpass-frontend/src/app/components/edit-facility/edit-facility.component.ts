import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';
import { Discipline } from '../../model/discipline.model';
import { WorkDay } from '../../model/workday.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-edit-facility',
  templateUrl: './edit-facility.component.html',
  styleUrls: ['./edit-facility.component.css'],
  imports: [FormsModule, CommonModule, RouterModule],
  standalone: true,
})
export class EditFacilityComponent implements OnInit {
  facilityId: number = 0;
  selectedPdfFile: File | null = null;
  isSubmitting = false;
  dayOptions = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
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

  onPdfSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedPdfFile = input.files && input.files.length ? input.files[0] : null;
  }

  onSubmit(): void {
    if (this.isSubmitting) {
      return;
    }

    this.facility.disciplines = this.disciplines;
    this.facility.workDays = this.workDays;
    this.isSubmitting = true;

    this.facilityService.editFacility(this.facilityId, this.facility).subscribe(
      () => {
        if (!this.selectedPdfFile) {
          this.isSubmitting = false;
          alert('Facility updated successfully');
          this.router.navigate(['/facilities']);
          return;
        }

        this.facilityService.uploadFacilityPdf(this.facilityId, this.selectedPdfFile).subscribe(
          () => {
            this.isSubmitting = false;
            alert('Facility and PDF updated successfully');
            this.router.navigate(['/facilities']);
          },
          (pdfError) => {
            this.isSubmitting = false;
            console.error('Facility updated, but PDF upload failed', pdfError);
            alert('Facility is updated, but PDF upload failed.');
            this.router.navigate(['/facilities']);
          }
        );
      },
      (error) => {
        this.isSubmitting = false;
        console.error('Error updating facility', error);
      }
    );
  }
}
