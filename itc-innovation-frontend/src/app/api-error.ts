import { HttpErrorResponse } from '@angular/common/http';

interface ApiErrorBody {
  detail?: unknown;
  message?: unknown;
  fieldErrors?: Record<string, unknown>;
}

const FIELD_LABELS: Record<string, string> = {
  fullName: 'Nom complet',
  email: 'Adresse e-mail',
  phone: 'Numéro de téléphone',
  consentGiven: 'Consentement',
  title: 'Titre',
  description: 'Description',
  firstName: 'Prénom',
  lastName: 'Nom',
  password: 'Mot de passe',
  currentPassword: 'Mot de passe actuel',
  newPassword: 'Nouveau mot de passe',
  confirmPassword: 'Confirmation du mot de passe',
  department: 'Service',
};

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) {
    return 'Impossible de joindre le serveur. Vérifiez votre connexion et réessayez.';
  }

  if (typeof error.error === 'string' && error.error.trim()) return error.error;

  const body = error.error as ApiErrorBody | null;
  const detail = typeof body?.detail === 'string' ? body.detail : '';
  const message = typeof body?.message === 'string' ? body.message : '';
  const fieldErrors = Object.entries(body?.fieldErrors ?? {})
    .filter((entry): entry is [string, string] => typeof entry[1] === 'string')
    .map(([field, fieldMessage]) => {
      const label = FIELD_LABELS[field] || field;
      if (field === 'consentGiven') {
        return 'Veuillez accepter la conservation de vos coordonnées.';
      }
      return `${label} : ${fieldMessage.trim()}`;
    });

  if (fieldErrors.length) {
    return fieldErrors.join(' · ');
  }
  return detail || message || `${fallback} (erreur ${error.status}).`;
}