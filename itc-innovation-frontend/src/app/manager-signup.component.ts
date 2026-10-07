import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { apiErrorMessage } from './api-error';

@Component({
  selector: 'app-manager-signup',
  imports: [FormsModule],
  templateUrl: './manager-signup.component.html',
  styleUrl: './manager-signup.component.css',
})
export class ManagerSignupComponent {
  private readonly http = inject(HttpClient);

  protected readonly firstName = signal('');
  protected readonly lastName = signal('');
  protected readonly email = signal('');
  protected readonly phone = signal('');
  protected readonly password = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');

  protected createManager(): void {
    this.error.set('');
    this.success.set('');
    if (this.password() !== this.confirmPassword()) {
      this.error.set('Les mots de passe ne correspondent pas.');
      return;
    }

    this.loading.set(true);
    this.http.post<void>('/api/setup/manager', {
      firstName: this.firstName().trim(),
      lastName: this.lastName().trim(),
      email: this.email().trim(),
      phone: this.phone().trim() || null,
      password: this.password(),
    }).subscribe({
      next: () => {
        this.success.set('Votre compte Manager a été créé. Connectez-vous depuis la page principale.');
        this.password.set('');
        this.confirmPassword.set('');
        this.loading.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'Le compte Directeur / Manager n’a pas pu être créé.'));
        this.loading.set(false);
      },
    });
  }
}