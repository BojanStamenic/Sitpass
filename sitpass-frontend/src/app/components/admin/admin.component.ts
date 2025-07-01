import { Component, OnInit } from '@angular/core';
import { AccountRequestService } from '../../services/account-request.service'; // Pretpostavljamo da postoji servis za account requests
import { HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms'; // Importujte FormsModule
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // Dodajte FormsModule ovde
})
export class AdminComponent implements OnInit {
  accountRequests: any[] = [];
  showRejectModal = false;
  rejectionReason = '';
  currentRequestId: number | null = null;

  constructor(private accountRequestService: AccountRequestService) {}

  ngOnInit() {
    this.loadAccountRequests();
  }

  loadAccountRequests() {
    this.accountRequestService.getAllAccountRequests().subscribe(
      (data) => {
        this.accountRequests = data;
        console.log('Account requests:', this.accountRequests);
      },
      (error) => {
        console.error('Error fetching account requests:', error);
      }
    );
  }

  acceptRequest(id: number) {
    const headers = this.getAuthHeaders();
    this.accountRequestService.acceptRequest(id, headers).subscribe(
      (success) => {
        console.log('Request accepted');
        this.loadAccountRequests(); // Refresh the list
        window.location.reload(); // Refresh the page
      },
      (error) => {
        console.error('Error accepting request:', error);
        window.location.reload(); // Refresh the page
      }
    );
  }

  openRejectModal(id: number) {
    this.currentRequestId = id;
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectionReason = '';
    this.currentRequestId = null;
  }

  rejectRequest() {
    if (this.currentRequestId !== null && this.rejectionReason.trim()) {
      const headers = this.getAuthHeaders();
      this.accountRequestService
        .rejectRequest(this.currentRequestId, this.rejectionReason, headers)
        .subscribe(
          () => {
            console.log('Request rejected');
            this.closeRejectModal();
            this.loadAccountRequests(); // Refresh the list
            window.location.reload(); // Refresh the page
          },
          (error) => {
            console.error('Error rejecting request:', error);
            window.location.reload(); // Refresh the page
          }
        );
    }
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }
}
