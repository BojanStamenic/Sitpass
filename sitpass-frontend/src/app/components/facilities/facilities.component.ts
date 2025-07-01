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
    this.facilityService.getFacilities().subscribe(
      (data) => {
        this.facilities = data;
        this.filteredFacilities = data; // Initialize with all facilities
      },
      (error) => {
        console.error('Error loading facilities', error);
      }
    );
  }

  onSearch(): void {
    this.filterFacilities();
  }

  filterFacilities(): void {
    let filtered = this.facilities;

    if (this.searchQuery) {
      filtered = filtered.filter((facility) =>
        facility.name.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }

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

    this.filteredFacilities = filtered;
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }
}
