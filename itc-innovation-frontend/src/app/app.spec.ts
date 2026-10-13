import { TestBed } from '@angular/core/testing';
import { App, filterEmployeesByStatus, filterManagerReportings, formatFrenchReportDate, paginateReportings, reportingsBeforeDate, reportingsForDate, resizeTextareaToContent, sortReportings } from './app';
import { routes } from './app.routes';
import { LandingComponent } from './landing.component';

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

  it('should expose manager signup only through an invitation token route', () => {
    expect(routes.some(route => route.path === 'manager-invite/:token')).toBe(true);
    expect(routes.some(route => route.path === '' && route.component === LandingComponent)).toBe(true);
    expect(routes.some(route => route.path === 'connexion' && route.component === App)).toBe(true);
    expect(routes.some(route => route.path === 'manager-access-c6ea546063f7ae11456a402557415f6b')).toBe(false);
    expect(routes.some(route => route.path === 'admin/create-director')).toBe(false);
  });

  it('sorts reports from newest date to oldest, then by descending id', () => {
    const reports = [
      { id: 1, reportDate: '2026-10-11' },
      { id: 5, reportDate: '2026-10-12' },
      { id: 7, reportDate: '2026-10-12' },
      { id: 3, reportDate: '2026-10-10' },
    ];

    expect(sortReportings(reports)).toEqual([
      { id: 7, reportDate: '2026-10-12' },
      { id: 5, reportDate: '2026-10-12' },
      { id: 1, reportDate: '2026-10-11' },
      { id: 3, reportDate: '2026-10-10' },
    ]);
    expect(reports[0].id).toBe(1);
  });

  it('keeps only today reports in the daily list and moves past dates to history', () => {
    const reports = [
      { id: 1, reportDate: '2026-10-11' },
      { id: 2, reportDate: '2026-10-12' },
      { id: 3, reportDate: '2026-10-10' },
    ];

    expect(reportingsForDate(reports, '2026-10-12')).toEqual([
      { id: 2, reportDate: '2026-10-12' },
    ]);
    expect(reportingsBeforeDate(reports, '2026-10-12')).toEqual([
      { id: 1, reportDate: '2026-10-11' },
      { id: 3, reportDate: '2026-10-10' },
    ]);
    expect(reportingsBeforeDate(reports, '2026-10-13')).toEqual([
      { id: 2, reportDate: '2026-10-12' },
      { id: 1, reportDate: '2026-10-11' },
      { id: 3, reportDate: '2026-10-10' },
    ]);
  });

  it('shows history reports in pages without changing their order', () => {
    const reports = Array.from({ length: 23 }, (_, index) => ({
      id: index + 1,
      reportDate: '2026-10-12',
    }));

    expect(paginateReportings(reports, 1, 20)).toEqual(reports.slice(0, 20));
    expect(paginateReportings(reports, 2, 20)).toEqual(reports.slice(20, 40));
  });

  it('filters manager history by employee and decision together', () => {
    const reports = [
      { id: 1, employeeId: 5, decision: 'VALIDATED' as const },
      { id: 2, employeeId: 5, decision: 'REJECTED' as const },
      { id: 3, employeeId: 6, decision: 'VALIDATED' as const },
      { id: 4, employeeId: 5, decision: 'PENDING' as const },
    ];

    expect(filterManagerReportings(reports, 5, 'VALIDATED')).toEqual([reports[0]]);
    expect(filterManagerReportings(reports, null, 'REJECTED')).toEqual([reports[1]]);
    expect(filterManagerReportings(reports, null, '')).toEqual(reports);
  });

  it('filters employees by active status while keeping all employees available', () => {
    const employees = [
      { id: 1, status: 'ACTIVE' as const },
      { id: 2, status: 'INACTIVE' as const },
    ];

    expect(filterEmployeesByStatus(employees, 'ACTIVE')).toEqual([employees[0]]);
    expect(filterEmployeesByStatus(employees, 'INACTIVE')).toEqual([employees[1]]);
    expect(filterEmployeesByStatus(employees, '')).toEqual(employees);
  });

  it('formats report dates with a short French weekday and classic date', () => {
    expect(formatFrenchReportDate('2026-10-12')).toBe('lun 12/10/2026');
  });

  it('resizes the employee description field to fit its content', () => {
    const textarea = document.createElement('textarea');
    Object.defineProperty(textarea, 'scrollHeight', { configurable: true, value: 176 });

    resizeTextareaToContent(textarea);

    expect(textarea.style.height).toBe('176px');
  });

});
