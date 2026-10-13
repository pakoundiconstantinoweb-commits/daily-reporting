export interface AuthResponse {
  token: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'SUPER_ADMIN' | 'MANAGER' | 'EMPLOYEE';
  demoExpiresAt?: string | null;
}
