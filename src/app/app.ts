import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthResponse } from './auth.types';
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
  protected readonly employeeDepartment = signal('');
  protected readonly employeePhone = signal('');
  protected readonly employeePhoneCountryCode = signal('+225');
  protected readonly countryCodes = COUNTRY_CODES;
  protected readonly managerSection = signal<'reports' | 'employees'>('reports');
  protected readonly passwordPanelOpen = signal(false);
  protected readonly currentPassword = signal('');
  protected readonly newPassword = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly currentPasswordVisible = signal(false);
  protected readonly newPasswordVisible = signal(false);
  protected readonly confirmPasswordVisible = signal(false);
  protected readonly passwordNotice = signal('');
  protected readonly passwordError = signal('');

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

  protected togglePasswordPanel(): void {
    this.passwordPanelOpen.update(open => !open);
    this.passwordNotice.set('');
    this.passwordError.set('');
  }

  protected togglePasswordVisibility(field: 'current' | 'new' | 'confirm'): void {
    if (field === 'current') this.currentPasswordVisible.update(visible => !visible);
    if (field === 'new') this.newPasswordVisible.update(visible => !visible);
    if (field === 'confirm') this.confirmPasswordVisible.update(visible => !visible);
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
        response.error?.detail ?? response.error?.message ?? 'Le mot de passe n’a pas pu être modifié.'),
    });
  }

  protected saveDraft(): void {
    this.notice.set('');
    const payload = {
      title: this.reportTitle(), description: this.reportDescription()
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
      error: (response: HttpErrorResponse) => this.error.set(this.reportingError(response, 'Le brouillon n’a pas pu être enregistré.')),
    });
  }

  protected editDraft(report: Reporting): void {
    this.editingReportId.set(report.id);
    this.reportTitle.set(report.title);
    this.reportDescription.set(report.description);
    this.notice.set('Brouillon chargé pour modification.');
  }

  protected submitReport(): void {
    if (!confirm('Confirmer l’envoi ?\n\nVoulez-vous confirmer l’envoi de ce reporting ?')) return;
    const payload = {
      title: this.reportTitle(), description: this.reportDescription()
    };
    const draftRequest = this.editingReportId()
      ? this.http.put<Reporting>(`/api/reports/${this.editingReportId()}`, payload)
      : this.http.post<Reporting>('/api/reports', payload);
    draftRequest.subscribe({
      next: (draft) => this.http.post<Reporting>(`/api/reports/${draft.id}/submit`, {}).subscribe({
        next: () => { this.notice.set('Reporting envoyé définitivement.'); this.editingReportId.set(null); this.loadEmployeeReports(); },
        error: (response: HttpErrorResponse) => this.error.set(this.reportingError(response, 'Le reporting n’a pas pu être envoyé.')),
      }),
      error: (response: HttpErrorResponse) => this.error.set(this.reportingError(response, 'Le reporting n’a pas pu être enregistré.')),
    });
  }

  protected searchReports(): void {
    const query = this.selectedDate() ? `?date=${this.selectedDate()}` : '';
    this.http.get<Reporting[]>(`/api/reports/manager${query}`).subscribe({
      next: (reports) => this.reportings.set(reports),
      error: () => this.error.set('Les reportings n’ont pas pu être chargés.'),
    });
  }

  protected searchMyReports(): void {
    const query = this.employeeSelectedDate() ? `?date=${this.employeeSelectedDate()}` : '';
    this.http.get<Reporting[]>(`/api/reports/mine${query}`).subscribe({
      next: (reports) => this.reportings.set(reports),
      error: (response: HttpErrorResponse) => this.error.set(this.reportingError(response, 'Votre historique n’a pas pu être chargé.')),
    });
  }

  protected react(report: Reporting, reaction: 'LIKE' | 'DISLIKE'): void {
    this.http.patch<Reporting>(`/api/reports/manager/${report.id}/reaction`, { reaction }).subscribe({
      next: (updated) => {
        this.reportings.update(items => items.map(item => item.id === updated.id ? updated : item));
        if (this.selectedReport()?.id === updated.id) this.selectedReport.set(updated);
      },
      error: () => this.error.set('La réaction n’a pas pu être enregistrée.'),
    });
  }

  protected selectReport(report: Reporting): void {
    this.selectedReport.set(report);
  }

  protected selectEmployee(employee: Employee): void {
    this.selectedEmployeeId.set(employee.id);
  }

  protected clearEmployeeFilter(): void {
    this.selectedEmployeeId.set(null);
  }

  protected visibleReportings(): Reporting[] {
    const employeeId = this.selectedEmployeeId();
    return employeeId === null
      ? this.reportings()
      : this.reportings().filter(report => report.employeeId === employeeId);
  }

  protected createEmployee(): void {
    this.http.post<Employee>('/api/manager/employees', {
      firstName: this.employeeFirstName(), lastName: this.employeeLastName(),
      email: this.employeeEmail(), password: this.employeePassword(),
      phone: `${this.employeePhoneCountryCode()} ${this.employeePhone()}`, department: this.employeeDepartment()
    }).subscribe({
      next: () => {
        this.notice.set('Compte employé créé.');
        this.employeeFirstName.set(''); this.employeeLastName.set(''); this.employeeEmail.set('');
        this.employeePassword.set(''); this.employeeDepartment.set(''); this.employeePhone.set(''); this.loadEmployees();
      },
      error: (response: HttpErrorResponse) => this.error.set(response.error?.detail ?? 'Le compte n’a pas pu être créé.'),
    });
  }

  protected toggleEmployee(employee: Employee): void {
    const status = employee.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.http.patch<Employee>(`/api/manager/employees/${employee.id}/status`, { status }).subscribe({
      next: (updated) => this.employees.update(items => items.map(item => item.id === updated.id ? updated : item)),
      error: () => this.error.set('Le statut du compte n’a pas pu être modifié.'),
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

  private reportingError(response: HttpErrorResponse, fallback: string): string {
    if (response.status === 0) return 'Le serveur est inaccessible. Vérifiez que le backend et PostgreSQL sont démarrés.';
    return response.error?.detail ?? response.error?.message ?? `${fallback} (erreur ${response.status}).`;
  }

  private loadWorkspace(profile: AuthResponse): void {
    if (profile.role === 'MANAGER') { this.searchReports(); this.loadEmployees(); }
    else this.loadEmployeeReports();
  }

  private loadEmployees(): void {
    this.http.get<Employee[]>('/api/manager/employees').subscribe({
      next: (employees) => this.employees.set(employees),
      error: () => this.error.set('La liste des employés n’a pas pu être chargée.'),
    });
  }

  protected loadEmployeeReports(): void {
    this.http.get<Reporting[]>('/api/reports/mine').subscribe({
      next: (reports) => this.reportings.set(reports),
      error: () => this.error.set('Votre historique n’a pas pu être chargé.'),
    });
  }
}
