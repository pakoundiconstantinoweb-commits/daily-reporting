import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Meta, Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';

import { AuthResponse } from './auth.types';
import { apiErrorMessage } from './api-error';

interface DemoRequest {
  fullName: string;
  email: string;
  phone: string;
  consentGiven: boolean;
}

interface DemoCredentials {
  email: string;
  password: string;
}

@Component({
  selector: 'app-landing',
  imports: [FormsModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css',
})
export class LandingComponent {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  protected readonly request = signal<DemoRequest>({
    fullName: '',
    email: '',
    phone: '',
    consentGiven: false,
  });

  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly whatsappUrl = 'https://wa.me/22870383470?text=Bonjour%2C%20je%20souhaite%20continuer%20apr%C3%A8s%20ma%20d%C3%A9mo%20Daily%20Reporting.';

  constructor(title: Title, meta: Meta) {
    title.setTitle('Daily Reporting — Logiciel de reporting quotidien d’équipe');
    meta.updateTag({
      name: 'description',
      content: 'Suivez les activités quotidiennes de votre équipe, recevez les reportings et validez-les simplement. Essayez Daily Reporting pendant 24 heures.',
    });
    meta.updateTag({
      name: 'keywords',
      content: 'logiciel de reporting quotidien, suivi des activités d’équipe, gestion des reportings, outil manager, Togo',
    });
  }

  protected update<K extends keyof DemoRequest>(field: K, value: DemoRequest[K]): void {
    this.request.update(current => ({ ...current, [field]: value }));
    this.error.set('');
  }

  protected startDemo(): void {
    this.error.set('');
    this.loading.set(true);
    const request = this.request();
    this.http.post<DemoCredentials>('/api/demo/requests', {
      ...request,
      fullName: request.fullName.trim(),
      email: request.email.trim(),
      phone: request.phone.trim(),
    }).subscribe({
      next: credentials => this.http.post<AuthResponse>('/api/auth/login', credentials).subscribe({
        next: profile => {
          localStorage.setItem('itc_token', profile.token);
          localStorage.setItem('itc_profile', JSON.stringify(profile));
          this.router.navigateByUrl('/connexion');
        },
        error: (response: HttpErrorResponse) => {
          this.error.set(apiErrorMessage(response, 'La démo a été créée, mais la connexion automatique a échoué. Contactez-nous sur WhatsApp.'));
          this.loading.set(false);
        },
      }),
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'La demande de démo n’a pas pu être envoyée.'));
        this.loading.set(false);
      },
    });
  }
}
