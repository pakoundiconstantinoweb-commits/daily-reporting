import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthResponse } from './auth.types';
import { apiErrorMessage } from './api-error';

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
  protected readonly passwordVisible = signal(false);

  protected login(): void {
    this.error.set('');
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

  protected togglePassword(): void {
    this.passwordVisible.update(visible => !visible);
  }
}
