import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { User } from '../../model/user.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ExerciseService } from '../../services/exercise-service.service';
import { Exercise } from '../../model/exercise.model';
import { forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
})
export class ProfileComponent implements OnInit {
  originalUser: User | undefined;
  user: User | undefined;
  isUpdating = false;
  isLoading = true;
  oldPassword = '';
  newPassword = '';
  confirmNewPassword = '';
  exercises: Exercise[] = [];
  successMessage = '';
  errorMessage = '';

  constructor(
    private userService: UserService,
    private exerciseService: ExerciseService
  ) {}

  ngOnInit() {
    const email = localStorage.getItem('userEmail');
    if (!email) {
      this.isLoading = false;
      this.errorMessage = 'Nema aktivne sesije.';
      return;
    }

    this.userService.getUserByEmail(email).subscribe(
      (data) => {
        this.user = data;
        this.originalUser = { ...data };
        this.loadUserExercises(this.user?.id);
        this.isLoading = false;
      },
      () => {
        this.errorMessage = 'Ne mogu da ucitam profil.';
        this.isLoading = false;
      }
    );
  }

  loadUserExercises(userId: number | undefined) {
    if (!userId) {
      this.exercises = [];
      return;
    }

    this.exerciseService.getExercisesByUserId(userId).subscribe(
      (exercises) => {
        this.exercises = exercises;
      },
      () => {
        this.exercises = [];
      }
    );
  }

  onUpdateProfile() {
    if (!this.user || this.isUpdating) {
      return;
    }

    this.isUpdating = true;
    this.successMessage = '';
    this.errorMessage = '';

    const wantsPasswordChange = !!this.oldPassword || !!this.newPassword || !!this.confirmNewPassword;

    if (wantsPasswordChange) {
      if (!this.oldPassword || !this.newPassword || !this.confirmNewPassword) {
        this.errorMessage = 'Za promenu lozinke popuni sva 3 polja.';
        this.isUpdating = false;
        return;
      }

      if (this.newPassword !== this.confirmNewPassword) {
        this.errorMessage = 'Nova lozinka i potvrda se ne poklapaju.';
        this.isUpdating = false;
        return;
      }
    }

    const profileChanges = this.getChangedFields();
    const profileRequest = this.userService.updateUserProfile(profileChanges);
    const passwordRequest = wantsPasswordChange
      ? this.userService.changePassword(this.user.id, this.oldPassword, this.newPassword)
      : of(null);

    forkJoin([profileRequest, passwordRequest]).subscribe(
      () => {
        this.successMessage = 'Profil je uspesno azuriran.';
        this.originalUser = this.user ? { ...this.user } : undefined;
        this.oldPassword = '';
        this.newPassword = '';
        this.confirmNewPassword = '';
        this.isUpdating = false;
      },
      () => {
        this.errorMessage = 'Azuriranje nije uspelo. Proveri podatke i pokusaj ponovo.';
        this.isUpdating = false;
      }
    );
  }

  getChangedFields() {
    if (!this.user) {
      return {};
    }

    return {
      id: this.user.id,
      name: this.user.name,
      surname: this.user.surname,
      email: this.user.email,
      address: this.user.address,
      birthday: this.user.birthday,
      phoneNumber: this.user.phoneNumber,
      zipCode: this.user.zipCode,
      city: this.user.city,
    };
  }

  getProfileImageUrl(): string {
    const name = this.user?.name || 'SitPass';
    const surname = this.user?.surname || 'User';
    const initials = encodeURIComponent(`${name} ${surname}`);
    return `https://ui-avatars.com/api/?name=${initials}&background=0f172a&color=67e8f9&size=256`;
  }
}
