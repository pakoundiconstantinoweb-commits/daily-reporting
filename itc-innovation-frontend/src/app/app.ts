import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthResponse } from './auth.types';
import { apiErrorMessage } from './api-error';
import { LoginComponent } from './login.component';
import { COUNTRY_CODES } from './country-codes';

registerLocaleData(localeFr);

interface Reporting {
  id: number;
  reportDate: string;
  title: string;
  description: string;
  status: 'DRAFT' | 'SUBMITTED';
  reaction: 'LIKE' | 'DISLIKE' | null;
  employeeId: number;
  employeeFirstName: string;
  employeeLastName: string;
}

interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  department: string | null;
  status: 'ACTIVE' | 'INACTIVE';
}

interface ProfileInformation {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  department: string | null;
  role: 'SUPER_ADMIN' | 'MANAGER' | 'EMPLOYEE';
  status: 'ACTIVE' | 'INACTIVE';
}

interface CreatedManagerInvitation {
  id: number;
  expiresAt: string;
  url: string;
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule, LoginComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly http = inject(HttpClient);
  protected readonly error = signal('');
  protected readonly user = signal<AuthResponse | null>(null);
  protected readonly reportings = signal<Reporting[]>([]);
  protected readonly selectedReport = signal<Reporting | null>(null);
  protected readonly reportTitle = signal('');
  protected readonly reportDescription = signal('');
  protected readonly selectedDate = signal('');
  protected readonly employeeSelectedDate = signal('');
  protected readonly selectedEmployeeId = signal<number | null>(null);
  protected readonly notice = signal('');
  protected readonly editingReportId = signal<number | null>(null);
  protected readonly today = new Date();
  protected readonly employees = signal<Employee[]>([]);
  protected readonly employeeFirstName = signal('');
  protected readonly employeeLastName = signal('');
  protected readonly employeeEmail = signal('');
  protected readonly employeePassword = signal('');
  protected readonly employeePasswordVisible = signal(false);
  protected readonly employeeDepartment = signal('');
  protected readonly employeePhone = signal('');
  protected readonly employeePhoneCountryCode = signal('+228');
  protected readonly countryCodes = COUNTRY_CODES;
  protected readonly managerSection = signal<'reports' | 'employees' | 'invitations' | 'profile'>('reports');
  protected readonly employeeSection = signal<'report' | 'history' | 'profile'>('report');
  protected readonly profileInformation = signal<ProfileInformation | null>(null);
  protected readonly profileLoadError = signal('');
  protected readonly profileFirstName = signal('');
  protected readonly profileLastName = signal('');
  protected readonly profileEmail = signal('');
  protected readonly profilePhone = signal('');
  protected readonly profileDepartment = signal('');
  protected readonly profileSaving = signal(false);
  protected readonly profileSaveNotice = signal('');
  protected readonly profileSaveError = signal('');
  protected readonly currentPassword = signal('');
  protected readonly newPassword = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly currentPasswordVisible = signal(false);
  protected readonly newPasswordVisible = signal(false);
  protected readonly confirmPasswordVisible = signal(false);
  protected readonly passwordNotice = signal('');
  protected readonly passwordError = signal('');
  protected readonly submitConfirmationOpen = signal(false);
  protected readonly submissionInProgress = signal(false);
  protected readonly managerInvitation = signal<CreatedManagerInvitation | null>(null);
  protected readonly invitationLoading = signal(false);
  protected readonly invitationError = signal('');
  protected readonly invitationNotice = signal('');

  constructor() {
    const storedProfile = localStorage.getItem('itc_profile');
    if (!storedProfile) return;
    try {
      const profile = JSON.parse(storedProfile) as AuthResponse;
      this.user.set(profile);
      this.loadWorkspace(profile);
    } catch {
      localStorage.removeItem('itc_profile');
      localStorage.removeItem('itc_token');
    }
  }

  protected handleLogin(response: AuthResponse): void {
    this.user.set(response);
    this.loadWorkspace(response);
  }

  protected logout(): void {
    localStorage.removeItem('itc_token');
    localStorage.removeItem('itc_profile');
    this.user.set(null);
  }

  protected openProfile(): void {
    if (this.user()?.role === 'EMPLOYEE') {
      this.employeeSection.set('profile');
    } else {
      this.managerSection.set('profile');
    }
    this.profileInformation.set(null);
    this.profileLoadError.set('');
    this.profileSaveNotice.set('');
    this.profileSaveError.set('');
    this.passwordNotice.set('');
    this.passwordError.set('');
    this.http.get<ProfileInformation>('/api/account/me').subscribe({
      next: profile => {
        this.profileInformation.set(profile);
        this.profileFirstName.set(profile.firstName);
        this.profileLastName.set(profile.lastName);
        this.profileEmail.set(profile.email);
        this.profilePhone.set(profile.phone ?? '');
        this.profileDepartment.set(profile.department ?? '');
      },
      error: (response: HttpErrorResponse) => this.profileLoadError.set(
        apiErrorMessage(response, 'Les informations du profil n’ont pas pu être chargées.')),
    });
  }

  protected saveProfile(): void {
    this.profileSaveNotice.set('');
    this.profileSaveError.set('');
    this.profileSaving.set(true);
    this.http.put<ProfileInformation>('/api/account/me', {
      firstName: this.profileFirstName().trim(),
      lastName: this.profileLastName().trim(),
      email: this.profileEmail().trim(),
      phone: this.profilePhone().trim(),
      department: this.profileDepartment().trim(),
    }).subscribe({
      next: updated => {
        this.profileInformation.set(updated);
        this.profileFirstName.set(updated.firstName);
        this.profileLastName.set(updated.lastName);
        this.profileEmail.set(updated.email);
        this.profilePhone.set(updated.phone ?? '');
        this.profileDepartment.set(updated.department ?? '');
        this.user.update(current => {
          if (!current) return current;
          const refreshed = {
            ...current,
            firstName: updated.firstName,
            lastName: updated.lastName,
            email: updated.email,
          };
          localStorage.setItem('itc_profile', JSON.stringify(refreshed));
          return refreshed;
        });
        this.profileSaveNotice.set('Tes informations ont été mises à jour.');
        this.profileSaving.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.profileSaveError.set(
          apiErrorMessage(response, 'Les informations du profil n’ont pas pu être enregistrées.'));
        this.profileSaving.set(false);
      },
    });
  }

  protected createManagerInvitation(): void {
    this.invitationError.set('');
    this.invitationNotice.set('');
    this.managerInvitation.set(null);
    this.invitationLoading.set(true);
    this.http.post<{ id: number; token: string; expiresAt: string }>(
      '/api/super-admin/invitations', {}
    ).subscribe({
      next: invitation => {
        this.managerInvitation.set({
          id: invitation.id,
          expiresAt: invitation.expiresAt,
          url: `${globalThis.location.origin}/manager-invite/${encodeURIComponent(invitation.token)}`,
        });
        this.invitationNotice.set('Invitation créée. Elle expirera dans 24 heures ou après son utilisation.');
        this.invitationLoading.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.invitationError.set(apiErrorMessage(response, 'L’invitation n’a pas pu être créée.'));
        this.invitationLoading.set(false);
      },
    });
  }

  protected copyManagerInvitation(): void {
    const invitation = this.managerInvitation();
    if (!invitation) return;
    navigator.clipboard.writeText(invitation.url).then(
      () => this.invitationNotice.set('Lien copié dans le presse-papiers.'),
      () => this.invitationError.set('Copie impossible. Sélectionnez et copiez le lien affiché.')
    );
  }

  protected revokeManagerInvitation(): void {
    const invitation = this.managerInvitation();
    if (!invitation) return;
    this.invitationError.set('');
    this.invitationNotice.set('');
    this.invitationLoading.set(true);
    this.http.patch<{ revoked: boolean }>(
      `/api/super-admin/invitations/${invitation.id}/revoke`, {}
    ).subscribe({
      next: () => {
        this.managerInvitation.set(null);
        this.invitationNotice.set('Invitation révoquée.');
        this.invitationLoading.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.invitationError.set(apiErrorMessage(response, 'L’invitation n’a pas pu être révoquée.'));
        this.invitationLoading.set(false);
      },
    });
  }

  protected togglePasswordVisibility(field: 'current' | 'new' | 'confirm' | 'employee'): void {
    if (field === 'current') this.currentPasswordVisible.update(visible => !visible);
    if (field === 'new') this.newPasswordVisible.update(visible => !visible);
    if (field === 'confirm') this.confirmPasswordVisible.update(visible => !visible);
    if (field === 'employee') this.employeePasswordVisible.update(visible => !visible);
  }

  protected changePassword(): void {
    this.passwordNotice.set('');
    this.passwordError.set('');
    if (this.newPassword() !== this.confirmPassword()) {
      this.passwordError.set('Les nouveaux mots de passe ne correspondent pas.');
      return;
    }
    if (this.newPassword().length < 8) {
      this.passwordError.set('Le nouveau mot de passe doit contenir au moins 8 caractères.');
      return;
    }

    this.http.patch('/api/account/password', {
      currentPassword: this.currentPassword(),
      newPassword: this.newPassword(),
    }).subscribe({
      next: () => {
        this.currentPassword.set('');
        this.newPassword.set('');
        this.confirmPassword.set('');
        this.passwordNotice.set('Mot de passe modifié avec succès.');
      },
      error: (response: HttpErrorResponse) => this.passwordError.set(
        apiErrorMessage(response, 'Le mot de passe n’a pas pu être modifié.')),
    });
  }

  protected saveDraft(): void {
    this.notice.set('');
    if (!this.validateReportFields()) return;
    this.error.set('');
    const payload = {
      title: this.reportTitle().trim(), description: this.reportDescription().trim()
    };
    const request = this.editingReportId()
      ? this.http.put<Reporting>(`/api/reports/${this.editingReportId()}`, payload)
      : this.http.post<Reporting>('/api/reports', payload);
    request.subscribe({
      next: (savedDraft) => {
        this.notice.set('Brouillon enregistré.');
        this.editingReportId.set(savedDraft.id);
        this.loadEmployeeReports();
      },
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Le brouillon n’a pas pu être enregistré.')),
    });
  }

  protected editDraft(report: Reporting): void {
    this.editingReportId.set(report.id);
    this.reportTitle.set(report.title);
    this.reportDescription.set(report.description);
    this.notice.set('Brouillon chargé pour modification.');
    this.employeeSection.set('report');
  }

  protected submitReport(): void {
    this.notice.set('');
    this.error.set('');
    if (!this.validateReportFields()) return;
    this.submitConfirmationOpen.set(true);
  }

  protected cancelSubmitReport(): void {
    this.submitConfirmationOpen.set(false);
  }

  protected confirmSubmitReport(): void {
    if (this.submissionInProgress()) return;
    this.submitConfirmationOpen.set(false);
    this.error.set('');
    if (!this.validateReportFields()) return;
    this.submissionInProgress.set(true);
    const payload = {
      title: this.reportTitle().trim(), description: this.reportDescription().trim()
    };
    const draftRequest = this.editingReportId()
      ? this.http.put<Reporting>(`/api/reports/${this.editingReportId()}`, payload)
      : this.http.post<Reporting>('/api/reports', payload);
    draftRequest.subscribe({
      next: (draft) => {
        this.editingReportId.set(draft.id);
        this.http.post<Reporting>(`/api/reports/${draft.id}/submit`, {}).subscribe({
          next: () => {
            this.submissionInProgress.set(false);
            this.notice.set('Reporting envoyé définitivement.');
            this.editingReportId.set(null);
            this.reportTitle.set('');
            this.reportDescription.set('');
            this.loadEmployeeReports();
          },
          error: (response: HttpErrorResponse) => {
            this.submissionInProgress.set(false);
            this.error.set(apiErrorMessage(response, 'Le reporting n’a pas pu être envoyé.'));
          },
        });
      },
      error: (response: HttpErrorResponse) => {
        this.submissionInProgress.set(false);
        this.error.set(apiErrorMessage(response, 'Le reporting n’a pas pu être enregistré.'));
      },
    });
  }

  private validateReportFields(): boolean {
    const missingTitle = !this.reportTitle().trim();
    const missingDescription = !this.reportDescription().trim();
    if (!missingTitle && !missingDescription) return true;

    let validationMessage = 'La description est obligatoire.';
    if (missingTitle && missingDescription) {
      validationMessage = 'Le titre et la description sont obligatoires.';
    } else if (missingTitle) {
      validationMessage = 'Le titre est obligatoire.';
    }
    this.error.set(validationMessage);
    return false;
  }

  protected searchReports(): void {
    this.error.set('');
    const query = this.selectedDate() ? `?date=${this.selectedDate()}` : '';
    this.http.get<Reporting[]>(`/api/reports/manager${query}`).subscribe({
      next: (reports) => this.reportings.set(reports),
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Les reportings n’ont pas pu être chargés.')),
    });
  }

  protected searchMyReports(): void {
    this.error.set('');
    const query = this.employeeSelectedDate() ? `?date=${this.employeeSelectedDate()}` : '';
    this.http.get<Reporting[]>(`/api/reports/mine${query}`).subscribe({
      next: (reports) => this.reportings.set(reports),
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Votre historique n’a pas pu être chargé.')),
    });
  }

  protected react(report: Reporting, reaction: 'LIKE' | 'DISLIKE'): void {
    this.error.set('');
    this.http.patch<Reporting>(`/api/reports/manager/${report.id}/reaction`, { reaction }).subscribe({
      next: (updated) => {
        this.reportings.update(items => items.map(item => item.id === updated.id ? updated : item));
        if (this.selectedReport()?.id === updated.id) this.selectedReport.set(updated);
      },
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'La réaction n’a pas pu être enregistrée.')),
    });
  }

  protected reactionCount(report: Reporting, reaction: 'LIKE' | 'DISLIKE'): number {
    return report.reaction === reaction ? 1 : 0;
  }

  protected toggleReportDetails(report: Reporting): void {
    this.selectedReport.update(selected => selected?.id === report.id ? null : report);
  }

  protected selectEmployee(employee: Employee): void {
    this.selectedEmployeeId.set(employee.id);
    this.selectedReport.set(null);
  }

  protected clearEmployeeFilter(): void {
    this.selectedEmployeeId.set(null);
    this.selectedReport.set(null);
  }

  protected visibleReportings(): Reporting[] {
    const employeeId = this.selectedEmployeeId();
    return employeeId === null
      ? this.reportings()
      : this.reportings().filter(report => report.employeeId === employeeId);
  }

  protected createEmployee(): void {
    this.error.set('');
    this.notice.set('');
    this.http.post<Employee>('/api/manager/employees', {
      firstName: this.employeeFirstName(), lastName: this.employeeLastName(),
      email: this.employeeEmail(), password: this.employeePassword(),
      phone: `${this.employeePhoneCountryCode()} ${this.employeePhone()}`, department: this.employeeDepartment()
    }).subscribe({
      next: () => {
        this.notice.set('Compte employé créé.');
        this.employeeFirstName.set(''); this.employeeLastName.set(''); this.employeeEmail.set('');
        this.employeePassword.set(''); this.employeePasswordVisible.set(false); this.employeeDepartment.set(''); this.employeePhone.set(''); this.loadEmployees();
      },
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Le compte n’a pas pu être créé.')),
    });
  }

  protected toggleEmployee(employee: Employee): void {
    this.error.set('');
    const status = employee.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.http.patch<Employee>(`/api/manager/employees/${employee.id}/status`, { status }).subscribe({
      next: (updated) => this.employees.update(items => items.map(item => item.id === updated.id ? updated : item)),
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Le statut du compte n’a pas pu être modifié.')),
    });
  }

  protected countTodayReports(): number {
    const today = this.today.toISOString().slice(0, 10);
    return this.reportings().filter(report => report.reportDate === today).length;
  }

  protected activeEmployeeCount(): number {
    return this.employees().filter(employee => employee.status === 'ACTIVE').length;
  }

  protected formatReportDate(date: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit', month: 'long', year: 'numeric'
    }).format(new Date(`${date}T00:00:00`));
  }

  private loadWorkspace(profile: AuthResponse): void {
    if (profile.role !== 'EMPLOYEE') { this.searchReports(); this.loadEmployees(); }
    else this.loadEmployeeReports();
  }

  private loadEmployees(): void {
    this.error.set('');
    this.http.get<Employee[]>('/api/manager/employees').subscribe({
      next: (employees) => this.employees.set(employees),
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'La liste des employés n’a pas pu être chargée.')),
    });
  }

  protected loadEmployeeReports(): void {
    this.error.set('');
    this.http.get<Reporting[]>('/api/reports/mine').subscribe({
      next: (reports) => this.reportings.set(reports),
      error: (response: HttpErrorResponse) => this.error.set(apiErrorMessage(response, 'Votre historique n’a pas pu être chargé.')),
    });
  }
}
