import { HttpErrorResponse } from '@angular/common/http';

interface ApiErrorBody {
  detail?: unknown;
  message?: unknown;
  fieldErrors?: Record<string, unknown>;
}

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
    .map(([field, fieldMessage]) => `${field}: ${fieldMessage}`);

  if (fieldErrors.length) return `${message || 'Certains champs sont invalides.'} ${fieldErrors.join(' ')}`;
  return detail || message || `${fallback} (erreur ${error.status}).`;
}