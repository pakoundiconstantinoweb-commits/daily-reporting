import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { LandingComponent } from './landing.component';

describe('LandingComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('presents searchable product information and the 24-hour demo contact form', async () => {
    const fixture = TestBed.createComponent(LandingComponent);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;

    expect(page.querySelector('h1')?.textContent).toContain('reporting quotidien');
    expect(page.querySelector('#demo-name')).toBeTruthy();
    expect(page.querySelector('#demo-email')).toBeTruthy();
    expect(page.querySelector('#demo-phone')).toBeTruthy();
    expect(page.querySelector('#fonctionnalites')).toBeTruthy();
    expect(page.textContent).toContain('24 heures');
  });

  it('offers to resume an active demo from the same browser', async () => {
    localStorage.setItem('itc_token', 'active-demo-token');
    localStorage.setItem('itc_profile', JSON.stringify({
      token: 'active-demo-token',
      userId: 42,
      firstName: 'Tagna',
      lastName: 'Pakoundi',
      email: 'demo@example.com',
      role: 'MANAGER',
      demoExpiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    }));
    const navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl');
    const fixture = TestBed.createComponent(LandingComponent);
    await fixture.whenStable();

    const page = fixture.nativeElement as HTMLElement;
    const email = page.querySelector<HTMLInputElement>('#demo-email')!;
    email.value = 'demo@example.com';
    email.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await fixture.whenStable();

    const resumeButton = [...page.querySelectorAll('button')]
      .find(button => button.textContent?.includes('Reprendre ma démo'));
    expect(resumeButton).toBeTruthy();
    resumeButton?.click();
    expect(navigateByUrl).toHaveBeenCalledWith('/connexion');
  });

  it('re-authenticates with the saved demo credential if its session token is missing', async () => {
    localStorage.setItem('itc_demo_resume', JSON.stringify({
      email: 'demo@example.com',
      password: 'random-demo-password',
      demoExpiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    }));
    const httpTesting = TestBed.inject(HttpTestingController);
    const navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl');
    const fixture = TestBed.createComponent(LandingComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const page = fixture.nativeElement as HTMLElement;
    expect(page.querySelector<HTMLInputElement>('#demo-email')?.value).toBe('demo@example.com');
    const resumeButton = [...page.querySelectorAll('button')]
      .find(button => button.textContent?.includes('Reprendre ma démo'));
    expect(resumeButton).toBeTruthy();
    resumeButton?.click();

    const loginRequest = httpTesting.expectOne('/api/auth/login');
    expect(loginRequest.request.body).toEqual({
      email: 'demo@example.com',
      password: 'random-demo-password',
    });
    loginRequest.flush({
      token: 'renewed-demo-token',
      userId: 42,
      firstName: 'Tagna',
      lastName: 'Pakoundi',
      email: 'demo@example.com',
      role: 'MANAGER',
      demoExpiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    });
    expect(localStorage.getItem('itc_token')).toBe('renewed-demo-token');
    expect(navigateByUrl).toHaveBeenCalledWith('/connexion');
  });
});
