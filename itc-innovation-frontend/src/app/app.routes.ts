import { Routes } from '@angular/router';

import { App } from './app';
import { ManagerSignupComponent } from './manager-signup.component';

export const routes: Routes = [
  { path: '', component: App },
  { path: 'manager-access-c6ea546063f7ae11456a402557415f6b', component: ManagerSignupComponent },
  { path: '**', redirectTo: '' },
];
