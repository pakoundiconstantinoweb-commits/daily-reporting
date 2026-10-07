import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the login form', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Connexion');
    expect(compiled.querySelector('form')).toBeTruthy();
  });

  it('should expose manager signup only through the unguessable route', () => {
    expect(routes.some(route => route.path === 'manager-access-c6ea546063f7ae11456a402557415f6b')).toBe(true);
    expect(routes.some(route => route.path === 'admin/create-director')).toBe(false);
  });
});
