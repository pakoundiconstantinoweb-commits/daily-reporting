import { Routes } from '@angular/router';

import { App } from './app';
import { ManagerSignupComponent } from './manager-signup.component';
import { LandingComponent } from './landing.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'connexion', component: App },
  { path: 'manager-invite/:token', component: ManagerSignupComponent },
  { path: '**', redirectTo: '' },
];
