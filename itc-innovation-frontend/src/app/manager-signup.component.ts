import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { apiErrorMessage } from './api-error';

@Component({
  selector: 'app-manager-signup',
  imports: [FormsModule],
  templateUrl: './manager-signup.component.html',
  styleUrl: './manager-signup.component.css',
})
export class ManagerSignupComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);

  protected readonly invitationToken = signal('');
  protected readonly validatingInvitation = signal(true);
  protected readonly invitationValid = signal(false);
  protected readonly firstName = signal('');
  protected readonly lastName = signal('');
  protected readonly email = signal('');
  protected readonly phone = signal('');
  protected readonly password = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.invitationToken.set(token);
    if (!token) {
      this.error.set('Le lien d’invitation est incomplet.');
      this.validatingInvitation.set(false);
      return;
    }

    this.http.get<{ valid: boolean }>(`/api/setup/invitations/${encodeURIComponent(token)}`).subscribe({
      next: response => {
        this.invitationValid.set(response.valid);
        if (!response.valid) this.error.set('Cette invitation n’est pas valide.');
        this.validatingInvitation.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'Cette invitation est invalide ou expirée.'));
        this.validatingInvitation.set(false);
      },
    });
  }

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
      invitationToken: this.invitationToken(),
    }).subscribe({
      next: () => {
        this.success.set('Votre compte Manager a été créé. Connectez-vous depuis la page principale.');
        this.invitationValid.set(false);
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