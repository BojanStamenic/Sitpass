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
  private readonly fallbackGymImages: string[] = [
    'https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?auto=compress&cs=tinysrgb&w=1200&h=700&dpr=1',
    'https://images.pexels.com/photos/2261485/pexels-photo-2261485.jpeg?auto=compress&cs=tinysrgb&w=1200&h=700&dpr=1',
    'https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=1200&h=700&dpr=1',
    'https://images.pexels.com/photos/3757376/pexels-photo-3757376.jpeg?auto=compress&cs=tinysrgb&w=1200&h=700&dpr=1',
    'https://images.pexels.com/photos/414029/pexels-photo-414029.jpeg?auto=compress&cs=tinysrgb&w=1200&h=700&dpr=1',
  ];

  facilities: Facility[] = [];
  filteredFacilities: Facility[] = [];
  searchQuery: string = '';
  isLoading = false;
  errorMessage = '';
  filterCriteria: {
    operator: 'AND' | 'OR';
    sortByName: '' | 'asc' | 'desc';
    city: string;
    discipline: string;
    minRating: number | null;
    maxRating: number | null;
    minReviews: number | null;
    maxReviews: number | null;
    ratingCategory: '' | 'EQUIPMENT' | 'STAFF' | 'HYGIENE' | 'SPACE';
    minCategoryRating: number | null;
    maxCategoryRating: number | null;
    workDay: string;
  } = {
    operator: 'AND',
    sortByName: '',
    city: '',
    discipline: '',
    minRating: null,
    maxRating: null,
    minReviews: null,
    maxReviews: null,
    ratingCategory: '',
    minCategoryRating: null,
    maxCategoryRating: null,
    workDay: '',
  };

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
    const hasReviewRange =
      this.filterCriteria.minReviews !== null ||
      this.filterCriteria.maxReviews !== null;
    const hasCategoryRange =
      this.filterCriteria.minCategoryRating !== null ||
      this.filterCriteria.maxCategoryRating !== null;

    if (!trimmedQuery && !hasReviewRange && !hasCategoryRange) {
      this.filterFacilities();
      return;
    }

    this.errorMessage = '';
    this.facilityService
      .advancedSearchFacilities({
        q: trimmedQuery,
        minReviews: this.filterCriteria.minReviews,
        maxReviews: this.filterCriteria.maxReviews,
        ratingCategory: this.filterCriteria.ratingCategory,
        minCategoryRating: this.filterCriteria.minCategoryRating,
        maxCategoryRating: this.filterCriteria.maxCategoryRating,
        operator: this.filterCriteria.operator,
        sortByName: this.filterCriteria.sortByName,
      })
      .subscribe(
      (data) => {
        const localCriteriaActive = this.hasLocalOnlyFilters();
        if (this.filterCriteria.operator === 'OR' && localCriteriaActive) {
          const localMatches = this.applyAdditionalFilters(this.facilities);
          this.filteredFacilities = this.mergeById(data, localMatches);
          return;
        }
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

  private hasLocalOnlyFilters(): boolean {
    return Boolean(
      this.filterCriteria.city.trim() ||
      this.filterCriteria.discipline.trim() ||
      this.filterCriteria.workDay ||
      this.filterCriteria.minRating !== null ||
      this.filterCriteria.maxRating !== null
    );
  }

  private mergeById(primary: Facility[], secondary: Facility[]): Facility[] {
    const merged: Facility[] = [];
    const seen = new Set<number>();

    for (const facility of primary) {
      if (facility?.id && !seen.has(facility.id)) {
        seen.add(facility.id);
        merged.push(facility);
      }
    }

    for (const facility of secondary) {
      if (facility?.id && !seen.has(facility.id)) {
        seen.add(facility.id);
        merged.push(facility);
      }
    }

    return merged;
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.filterCriteria = {
      operator: 'AND',
      sortByName: '',
      city: '',
      discipline: '',
      minRating: null,
      maxRating: null,
      minReviews: null,
      maxReviews: null,
      ratingCategory: '',
      minCategoryRating: null,
      maxCategoryRating: null,
      workDay: '',
    };
    this.filteredFacilities = [...this.facilities];
  }

  loadMoreLikeThis(facility: Facility): void {
    if (!facility?.id) {
      return;
    }

    this.errorMessage = '';
    this.facilityService.moreLikeThis(facility.id, this.filterCriteria.sortByName).subscribe(
      (data) => {
        this.filteredFacilities = this.applyAdditionalFilters(data);
      },
      (error) => {
        console.error('Error loading more-like-this', error);
        this.errorMessage = 'More like this pretraga trenutno nije dostupna.';
      }
    );
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
    const seed = Number(facility.id || 1);
    const index = Math.abs(seed) % this.fallbackGymImages.length;
    return this.fallbackGymImages[index];
  }
}
