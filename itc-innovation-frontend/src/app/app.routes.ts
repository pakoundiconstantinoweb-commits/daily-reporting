import { Routes } from '@angular/router';

import { App } from './app';
import { ManagerSignupComponent } from './manager-signup.component';

export const routes: Routes = [
  { path: '', component: App },
  { path: 'manager-invite/:token', component: ManagerSignupComponent },
  { path: '**', redirectTo: '' },
];
