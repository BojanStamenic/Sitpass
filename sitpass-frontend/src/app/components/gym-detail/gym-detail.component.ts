import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FacilityService } from '../../services/facility-service.service';
import { Facility } from '../../model/facility.model';
import { CommonModule } from '@angular/common';
import { Rate } from '../../model/rate.model';
import { Review } from '../../model/review.model';
import { FormsModule } from '@angular/forms';
import { User } from '../../model/user.model';
import { UserService } from '../../services/user.service';
import { ReviewService } from '../../services/review-service.service';

@Component({
  selector: 'app-gym-detail',
  templateUrl: './gym-detail.component.html',
  styleUrls: ['./gym-detail.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
})
export class GymDetailComponent implements OnInit {
  facility: Facility | undefined;
  activeSection: string | null = 'disciplines';
  showModal: boolean = false;
  newReview: Review = {
    id: 0,
    createdAt: new Date(),
    exerciseCount: 0,
    hidden: false,
    user: null,
    facility: null,
    rates: null,
    comments: [],
  };
  newRate: Rate = {
    id: 0,
    equipment: 0,
    staff: 0,
    hygiene: 0,
    space: 0,
    review: null,
  };
  commentText: string = '';
  reviews: Review[] = [];
  showReservationModal: boolean = false; // New property for reservation modal
  reservationStart: string = ''; // Property for reservation start date and time
  reservationEnd: string = '';
  isAuthorized: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private facilityService: FacilityService,
    private router: Router,
    private userService: UserService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      alert('Invalid facility ID');
      this.router.navigate(['/facilities']);
      return;
    }

    this.facilityService.getFacilityById(id).subscribe(
      (data) => {
        this.facility = data;
        this.loadReviews(id); // Load reviews for the facility
      },
      (error) => {
        console.error('Error fetching facility details', error);
        alert('Failed to fetch facility details. Please try again later.');
        this.router.navigate(['/facilities']);
      }
    );
  }

  isAdmin(): boolean {
    if (localStorage.getItem('role') === 'ROLE_ADMIN') {
      return true;
    } else {
      return false;
    }
  }

  toggleSection(section: string): void {
    this.activeSection = this.activeSection === section ? null : section;
  }

  editFacility(): void {
    if (this.facility) {
      this.router.navigate(['/facilities/edit', this.facility.id]);
    }
  }

  openReservationModal(): void {
    this.showReservationModal = true;
  }

  closeReservationModal(): void {
    this.showReservationModal = false;
  }

  submitReservation(): void {
    // Check for facilityId
    const facilityId = this.facility?.id;
    if (!facilityId) {
      console.error('Facility ID is not available');
      alert('Facility ID is not available');
      return;
    }

    // Fetch user ID using the email from local storage
    const email = localStorage.getItem('userEmail');
    if (!email) {
      console.error('User email is not available');
      alert('User email is not available');
      return;
    }

    // Fetch user details to get user ID
    this.userService.getUserByEmail(email).subscribe(
      (user: User) => {
        const userId = user.id;
        if (!userId) {
          console.error('User ID is not available');
          alert('User ID is not available');
          return;
        }

        // Construct reservation data
        const reservationData = {
          startTime: this.reservationStart,
          endTime: this.reservationEnd,
          facilityId: facilityId,
          userId: userId,
        };

        console.log('Reservation Data:', reservationData);

        // Call your reservation service to submit the reservation
        this.facilityService.submitReservation(reservationData).subscribe(
          () => {
            alert('Reservation added successfully');
            this.closeReservationModal(); // Close the modal after submission
          },
          (error) => {
            console.error('Error adding reservation', error);
            alert('Failed to add reservation. Please try again later.');
          }
        );
      },
      (error) => {
        console.error('Error fetching user details', error);
        alert('Failed to fetch user details. Please try again later.');
      }
    );
  }

  deleteFacility(): void {
    if (
      this.facility &&
      confirm('Are you sure you want to delete this facility?')
    ) {
      this.facilityService.deleteFacility(this.facility.id).subscribe(
        () => {
          alert('Facility deleted successfully.');
          this.router.navigate(['/facilities']);
        },
        (error) => {
          console.error('Error deleting facility', error);
          alert('Failed to delete facility. Please try again later.');
        }
      );
    }
  }

  openModal(): void {
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  submitReview(): void {
    const facilityId = this.route.snapshot.paramMap.get('id');
    if (facilityId) {
      this.newReview.facility = { id: +facilityId } as Facility;
    } else {
      console.error('Facility ID is not available');
      alert('Facility ID is not available');
      return;
    }

    const email = localStorage.getItem('userEmail');
    if (email) {
      this.userService.getUserByEmail(email).subscribe(
        (user: User) => {
          this.newReview.user = { id: user.id } as User;

          const ratePayload: Rate = {
            ...this.newRate,
            review: { id: 0 } as Review,
          };

          const reviewPayload: Review = {
            ...this.newReview,
            rates: ratePayload,
          };

          this.facilityService.addReview(reviewPayload).subscribe(
            () => {
              alert('Review added successfully');
              this.closeModal();
              this.loadReviews(+facilityId); // Reload reviews after adding a new one
            },
            (error) => {
              console.error('Error adding review', error);
              alert('Failed to add review. Please try again later.');
            }
          );
        },
        (error) => {
          console.error('Error fetching user details', error);
        }
      );
    } else {
      console.error('User email is not available');
      alert('User email is not available');
    }
  }

  loadReviews(facilityId: number): void {
    this.reviewService.getReviewsByFacilityId(facilityId).subscribe(
      (data) => {
        this.reviews = data;
      },
      (error) => {
        console.error('Error fetching reviews', error);
      }
    );
  }
}
