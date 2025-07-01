import { Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register.component';
import { LoginComponent } from './components/login/login.component';
import { AdminComponent } from './components/admin/admin.component';
import { ProfileComponent } from './components/profile/profile.component';
import { FacilitiesComponent } from './components/facilities/facilities.component';
import { GymDetailComponent } from './components/gym-detail/gym-detail.component';
import { AddFacilityComponent } from './components/add-facility/add-facility.component';
import { EditFacilityComponent } from './components/edit-facility/edit-facility.component';
import { AuthGuard } from './auth.guard';
import { UserGuard } from './user.guard';

export const routes: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [UserGuard] },
  {
    path: 'facilities',
    component: FacilitiesComponent,
    canActivate: [UserGuard],
  },
  { path: 'gym/:id', component: GymDetailComponent, canActivate: [UserGuard] },
  {
    path: 'addFacility',
    component: AddFacilityComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'editFacility/:id',
    component: EditFacilityComponent,
    canActivate: [AuthGuard],
  },

  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login', pathMatch: 'full' },
];
