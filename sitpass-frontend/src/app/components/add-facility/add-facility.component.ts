import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';
import { Discipline } from '../../model/discipline.model';
import { WorkDay } from '../../model/workday.model';
import { FormsModule } from '@angular/forms'; // Dodajte FormsModule
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-add-facility',
  templateUrl: './add-facility.component.html',
  styleUrls: ['./add-facility.component.css'],
  imports: [FormsModule, CommonModule, RouterModule], // Dodajte FormsModule ovde
  standalone: true,
})
export class AddFacilityComponent {
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

    this.facilityService.addFacility(this.facility).subscribe(
      (savedFacility) => {
        if (!this.selectedPdfFile) {
          this.isSubmitting = false;
          alert('Facility added successfully');
          this.router.navigate(['/facilities']);
          return;
        }

        this.facilityService.uploadFacilityPdf(savedFacility.id, this.selectedPdfFile).subscribe(
          () => {
            this.isSubmitting = false;
            alert('Facility and PDF added successfully');
            this.router.navigate(['/facilities']);
          },
          (pdfError) => {
            this.isSubmitting = false;
            console.error('Facility saved, but PDF upload failed', pdfError);
            alert('Facility is saved, but PDF upload failed.');
            this.router.navigate(['/facilities']);
          }
        );
      },
      (error) => {
        this.isSubmitting = false;
        console.error('Error adding facility', error);
      }
    );
  }
}
