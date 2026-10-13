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
  demoExpiresAt: string;
}

type StoredDemoCredentials = DemoCredentials;

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
  protected readonly whatsappUrl = 'https://wa.me/22893741753?text=Bonjour%2C%20je%20souhaite%20continuer%20apr%C3%A8s%20ma%20d%C3%A9mo%20Daily%20Reporting.';

  constructor(title: Title, meta: Meta) {
    const storedCredentials = this.readStoredDemoCredentials();
    if (storedCredentials) {
      this.request.update(current => ({ ...current, email: storedCredentials.email }));
    }

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

  protected canResumeDemo(): boolean {
    const email = this.request().email.trim().toLowerCase();
    if (!email) return false;

    const savedCredentials = this.readStoredDemoCredentials();
    if (savedCredentials && savedCredentials.email.toLowerCase() === email) return true;
    if (!localStorage.getItem('itc_token')) return false;

    try {
      const profile = JSON.parse(localStorage.getItem('itc_profile') ?? '') as AuthResponse;
      return profile.role === 'MANAGER'
        && profile.email.toLowerCase() === email
        && !!profile.demoExpiresAt
        && Date.parse(profile.demoExpiresAt) > Date.now();
    } catch {
      return false;
    }
  }

  protected resumeDemo(): void {
    if (!this.canResumeDemo()) return;

    const email = this.request().email.trim().toLowerCase();
    const storedCredentials = this.readStoredDemoCredentials();
    if (storedCredentials?.email.toLowerCase() === email) {
      this.loading.set(true);
      this.http.post<AuthResponse>('/api/auth/login', {
        email: storedCredentials.email,
        password: storedCredentials.password,
      }).subscribe({
        next: profile => {
          this.saveProfile(profile);
          this.router.navigateByUrl('/connexion');
        },
        error: (response: HttpErrorResponse) => {
          this.error.set(apiErrorMessage(response, 'Impossible de reprendre la démo. Réessayez ou contactez le super-admin.'));
          this.loading.set(false);
        },
      });
      return;
    }

    this.router.navigateByUrl('/connexion');
  }

  protected startDemo(): void {
    if (this.canResumeDemo()) {
      this.resumeDemo();
      return;
    }

    this.error.set('');
    this.loading.set(true);
    const request = this.request();
    this.http.post<DemoCredentials>('/api/demo/requests', {
      ...request,
      fullName: request.fullName.trim(),
      email: request.email.trim(),
      phone: request.phone.trim(),
    }).subscribe({
      next: credentials => {
        this.storeDemoCredentials(credentials);
        this.http.post<AuthResponse>('/api/auth/login', credentials).subscribe({
        next: profile => {
          this.saveProfile(profile);
          this.router.navigateByUrl('/connexion');
        },
        error: (response: HttpErrorResponse) => {
          this.error.set(apiErrorMessage(response, 'La démo a été créée. Vos identifiants ont été conservés dans ce navigateur ; réessayez avec le même e-mail pour reprendre l’accès.'));
          this.loading.set(false);
        },
      });
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(response, 'La demande de démo n’a pas pu être envoyée.'));
        this.loading.set(false);
      },
    });
  }

  private saveProfile(profile: AuthResponse): void {
    localStorage.setItem('itc_token', profile.token);
    localStorage.setItem('itc_profile', JSON.stringify(profile));
  }

  private storeDemoCredentials(credentials: DemoCredentials): void {
    localStorage.setItem('itc_demo_resume', JSON.stringify(credentials));
  }

  private readStoredDemoCredentials(): StoredDemoCredentials | null {
    const stored = localStorage.getItem('itc_demo_resume');
    if (!stored) return null;

    try {
      const credentials = JSON.parse(stored) as StoredDemoCredentials;
      if (!credentials.email || !credentials.password || !credentials.demoExpiresAt
          || Date.parse(credentials.demoExpiresAt) <= Date.now()) {
        localStorage.removeItem('itc_demo_resume');
        return null;
      }
      return credentials;
    } catch {
      localStorage.removeItem('itc_demo_resume');
      return null;
    }
  }
}
