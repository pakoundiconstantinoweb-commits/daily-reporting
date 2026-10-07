import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { RootApp } from './app/root-app.component';

bootstrapApplication(RootApp, appConfig)
  .catch((err) => console.error(err));
