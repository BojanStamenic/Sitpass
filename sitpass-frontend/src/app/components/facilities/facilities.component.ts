import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';

@Component({
  selector: 'app-facilities',
  templateUrl: './facilities.component.html',
  imports: [CommonModule, FormsModule, RouterModule],
  styleUrls: ['./facilities.component.css'],
  standalone: true,
})
export class FacilitiesComponent implements OnInit {
  facilities: Facility[] = [];
  filteredFacilities: Facility[] = [];
  searchQuery: string = '';
  isLoading = false;
  errorMessage = '';
  filterCriteria: {
    city: string;
    discipline: string;
    minRating: number | null;
    maxRating: number | null;
    workDay: string;
  } = {
    city: '',
    discipline: '',
    minRating: null,
    maxRating: null,
    workDay: '',
  };

  isSidebarOpen: boolean = false;

  constructor(private facilityService: FacilityService) {}

  ngOnInit(): void {
    this.loadFacilities();
  }

  loadFacilities(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.facilityService.getFacilities().subscribe(
      (data) => {
        this.facilities = data;
        this.filteredFacilities = data;
        this.isLoading = false;
      },
      (error) => {
        console.error('Error loading facilities', error);
        this.isLoading = false;
        if (error?.status === 401 || error?.status === 403) {
          this.errorMessage =
            'Sesija je istekla. Uloguj se ponovo da vidis objekte.';
          return;
        }
        this.errorMessage =
          'Ne mogu da ucitam objekte. Proveri da li backend radi na portu 8080.';
      }
    );
  }

  onSearch(): void {
    const trimmedQuery = this.searchQuery.trim();
    if (!trimmedQuery) {
      this.filterFacilities();
      return;
    }

    this.errorMessage = '';
    this.facilityService.searchFacilities(trimmedQuery).subscribe(
      (data) => {
        this.filteredFacilities = this.applyAdditionalFilters(data);
      },
      (error) => {
        console.error('Error searching facilities', error);
        this.errorMessage = 'Pretraga trenutno nije dostupna.';
      }
    );
  }

  filterFacilities(): void {
    this.filteredFacilities = this.applyAdditionalFilters(this.facilities);
  }

  private applyAdditionalFilters(baseFacilities: Facility[]): Facility[] {
    let filtered = baseFacilities;

    if (this.filterCriteria.city) {
      filtered = filtered.filter((facility) =>
        facility.city
          .toLowerCase()
          .includes(this.filterCriteria.city.toLowerCase())
      );
    }

    if (this.filterCriteria.discipline) {
      filtered = filtered.filter((facility) =>
        facility.disciplines.some((d) =>
          d.name
            .toLowerCase()
            .includes(this.filterCriteria.discipline.toLowerCase())
        )
      );
    }

    if (this.filterCriteria.minRating !== null) {
      filtered = filtered.filter(
        (facility) => facility.totalRating >= this.filterCriteria.minRating!
      );
    }

    if (this.filterCriteria.maxRating !== null) {
      filtered = filtered.filter(
        (facility) => facility.totalRating <= this.filterCriteria.maxRating!
      );
    }

    if (this.filterCriteria.workDay) {
      filtered = filtered.filter((facility) =>
        facility.workDays.some(
          (wd) =>
            wd.day.toLowerCase() === this.filterCriteria.workDay.toLowerCase()
        )
      );
    }

    return filtered;
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.filterCriteria = {
      city: '',
      discipline: '',
      minRating: null,
      maxRating: null,
      workDay: '',
    };
    this.filteredFacilities = [...this.facilities];
  }

  downloadPdf(facility: Facility): void {
    if (!facility.id) {
      return;
    }

    this.facilityService.downloadFacilityPdf(facility.id).subscribe(
      (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = facility.pdfFileName || `facility-${facility.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      (error) => {
        console.error('Error downloading PDF', error);
      }
    );
  }

  getFacilityImageUrl(facility: Facility): string {
    const images: any[] = (facility as any).images || [];
    if (images.length > 0) {
      const firstImage = images[0];
      if (typeof firstImage === 'string' && firstImage.trim().length > 0) {
        return firstImage;
      }
      if (firstImage?.id) {
        return `http://localhost:8080/SitPass/api/facilities/images/${firstImage.id}`;
      }
    }
    const seed = facility.id || 1;
    return `https://picsum.photos/seed/sitpass-${seed}/800/450`;
  }
}
