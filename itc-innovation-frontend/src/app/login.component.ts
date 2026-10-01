import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthResponse } from './auth.types';
import { apiErrorMessage } from './api-error';
import { COUNTRY_CODES } from './country-codes';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly http = inject(HttpClient);
  readonly authenticated = output<AuthResponse>();
  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly setupMode = signal(false);
  protected readonly firstName = signal('');
  protected readonly lastName = signal('');
  protected readonly phone = signal('');
  protected readonly phoneCountryCode = signal('+225');
  protected readonly countryCodes = COUNTRY_CODES;
  protected readonly setupSuccess = signal('');
  protected readonly passwordVisible = signal(false);

  protected login(): void {
    this.error.set('');
    this.setupSuccess.set('');
    this.loading.set(true);
    this.http.post<AuthResponse>('/api/auth/login', {
      email: this.email(),
      password: this.password(),
    }).subscribe({
      next: (response) => {
        localStorage.setItem('itc_token', response.token);
        localStorage.setItem('itc_profile', JSON.stringify(response));
        this.authenticated.emit(response);
        this.loading.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'La connexion a échoué.'));
        this.loading.set(false);
      },
    });
  }

  protected createInitialManager(): void {
    this.error.set('');
    this.setupSuccess.set('');
    this.loading.set(true);
    this.http.post('/api/setup/manager', {
      firstName: this.firstName(), lastName: this.lastName(), email: this.email(),
      password: this.password(), phone: `${this.phoneCountryCode()} ${this.phone()}`
    }).subscribe({
      next: () => {
        this.setupSuccess.set('Compte Directeur / Manager créé. Vous pouvez maintenant vous connecter.');
        this.setupMode.set(false);
        this.password.set('');
        this.loading.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'Le compte Directeur / Manager n’a pas pu être créé.'));
        this.loading.set(false);
      },
    });
  }

  protected showSetup(): void {
    this.error.set('');
    this.setupSuccess.set('');
    this.email.set('');
    this.password.set('');
    this.passwordVisible.set(false);
    this.setupMode.set(true);
  }

  protected showLogin(): void {
    this.error.set('');
    this.setupMode.set(false);
  }

  protected togglePassword(): void {
    this.passwordVisible.update(visible => !visible);
  }
}
