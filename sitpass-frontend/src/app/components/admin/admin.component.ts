import { Component, OnInit } from '@angular/core';
import { AccountRequestService } from '../../services/account-request.service';
import { HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
})
export class AdminComponent implements OnInit {
  accountRequests: any[] = [];
  showRejectModal = false;
  rejectionReason = '';
  currentRequestId: number | null = null;
  isLoading = false;
  actionRequestId: number | null = null;
  successMessage = '';
  errorMessage = '';

  constructor(private accountRequestService: AccountRequestService) {}

  ngOnInit() {
    this.loadAccountRequests();
  }

  loadAccountRequests() {
    this.isLoading = true;
    this.accountRequestService.getAllAccountRequests().subscribe(
      (data) => {
        this.accountRequests = data;
        this.isLoading = false;
      },
      () => {
        this.errorMessage = 'Ne mogu da ucitam zahteve.';
        this.isLoading = false;
      }
    );
  }

  acceptRequest(id: number) {
    this.actionRequestId = id;
    this.successMessage = '';
    this.errorMessage = '';
    const headers = this.getAuthHeaders();

    this.accountRequestService.acceptRequest(id, headers).subscribe(
      () => {
        this.successMessage = `Zahtev #${id} je prihvacen.`;
        this.actionRequestId = null;
        this.loadAccountRequests();
      },
      () => {
        this.errorMessage = `Greska pri prihvatanju zahteva #${id}.`;
        this.actionRequestId = null;
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
    if (this.currentRequestId === null || !this.rejectionReason.trim()) {
      return;
    }

    const id = this.currentRequestId;
    this.actionRequestId = id;
    this.successMessage = '';
    this.errorMessage = '';
    const headers = this.getAuthHeaders();

    this.accountRequestService
      .rejectRequest(id, this.rejectionReason, headers)
      .subscribe(
        () => {
          this.successMessage = `Zahtev #${id} je odbijen.`;
          this.actionRequestId = null;
          this.closeRejectModal();
          this.loadAccountRequests();
        },
        () => {
          this.errorMessage = `Greska pri odbijanju zahteva #${id}.`;
          this.actionRequestId = null;
        }
      );
  }

  formatDate(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return date.toLocaleString('sr-RS');
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-emerald-100 text-emerald-700';
      case 'REJECTED':
        return 'bg-rose-100 text-rose-700';
      default:
        return 'bg-amber-100 text-amber-700';
    }
  }

  getAvatarUrl(email: string): string {
    const safe = encodeURIComponent(email || 'Admin User');
    return `https://ui-avatars.com/api/?name=${safe}&background=0f172a&color=67e8f9&size=128`;
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }
}
