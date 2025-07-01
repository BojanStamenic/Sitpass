import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { User } from '../../model/user.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ExerciseService } from '../../services/exercise-service.service';
import { Exercise } from '../../model/exercise.model';

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
  isUpdating: boolean = false;
  oldPassword: string = '';
  newPassword: string = '';
  confirmNewPassword: string = '';
  originalUserId: number | undefined;
  exercises: Exercise[] = []; // Lista vežbi

  constructor(
    private userService: UserService,
    private exerciseService: ExerciseService,
    private router: Router
  ) {}

  ngOnInit() {
    if (typeof window !== 'undefined' && window.localStorage) {
      const email = localStorage.getItem('userEmail');
      if (email) {
        this.userService.getUserByEmail(email).subscribe(
          (data) => {
            this.user = data;
            this.originalUser = { ...data };
            this.originalUserId = this.user?.id;
            this.loadUserExercises(this.user?.id);
            console.log('User data:', this.user);
            console.log('Original user data:', this.originalUser);
            console.log('User ID:', this.originalUserId);
          },
          (error) => {
            console.error('Error fetching user data', error);
          }
        );
      }
    } else {
      console.warn('LocalStorage is not available.');
    }
  }

  loadUserExercises(userId: number | undefined) {
    // Proveravamo da li je userId definisan
    if (userId) {
      this.exerciseService.getExercisesByUserId(userId).subscribe(
        (exercises) => {
          this.exercises = exercises;
        },
        (error) => {
          console.error('Error fetching exercises', error);
        }
      );
    } else {
      console.error('User ID is undefined');
    }
  }

  onUpdateProfile() {
    this.isUpdating = true;

    // Validate old and new passwords
    if (this.oldPassword && this.newPassword) {
      if (this.newPassword !== this.confirmNewPassword) {
        console.error('New passwords do not match');
        this.isUpdating = false;
        return;
      }

      if (this.user) {
        this.userService
          .changePassword(this.user.id, this.oldPassword, this.newPassword)
          .subscribe(
            () => {
              console.log('Password changed successfully');
              this.router.navigate(['/']);
            },
            (error) => {
              console.error('Error changing password', error);
            }
          );
      }
    }

    if (this.user && this.originalUser) {
      const changes = this.getChangedFields();
      if (Object.keys(changes).length > 0) {
        this.userService.updateUserProfile(changes).subscribe(
          () => {
            console.log('Profile updated successfully');
            this.router.navigate(['/']);
          },
          (error) => {
            console.error('Error updating profile', error);
          }
        );
      } else {
        console.log('No changes detected');
        this.isUpdating = false;
      }
    }
  }
  getChangedFields() {
    if (!this.user || !this.originalUserId) {
      return {}; // Nema promena ako nije dostupan korisnik ili ID
    }
    console.log('LALALALALALALLALA' + this.user.birthday);
    // Uporedite polja i vratite samo promene
    const changes: any = {};
    if (this.user.id !== null) changes.id = this.user.id;
    if (this.user.name !== this.originalUser?.name)
      changes.name = this.user.name;
    else changes.name = this.originalUser.name;
    if (this.user.surname !== this.originalUser?.surname)
      changes.surname = this.user.surname;
    else changes.surname = this.originalUser.surname;
    if (this.user.email !== this.originalUser?.email)
      changes.email = this.user.email;
    else changes.email = this.originalUser.email;
    if (this.user.address !== this.originalUser?.address)
      changes.address = this.user.address;
    else changes.address = this.originalUser.address;
    if (this.user.birthday !== this.originalUser?.birthday)
      changes.birthday = this.user.birthday;
    else changes.birthday = this.originalUser?.birthday;
    if (this.user.phoneNumber !== this.originalUser?.phoneNumber)
      changes.phoneNumber = this.user.phoneNumber;
    else changes.phoneNumber = this.originalUser?.phoneNumber;
    if (this.user.zipCode !== this.originalUser?.zipCode)
      changes.zipCode = this.user.zipCode;
    else changes.zipCode = this.originalUser?.zipCode;
    if (this.user.city !== this.originalUser?.city)
      changes.city = this.user.city;
    else changes.city = this.originalUser?.city;

    console.log('IZMENE' + changes.birthday);

    return changes;
  }
}
