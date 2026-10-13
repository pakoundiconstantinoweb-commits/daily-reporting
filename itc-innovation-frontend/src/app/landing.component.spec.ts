import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { LandingComponent } from './landing.component';

describe('LandingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [provideHttpClient(), provideRouter([])],
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
});
