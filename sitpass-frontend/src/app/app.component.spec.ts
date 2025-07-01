import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([]), AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should log navigation on clicking links', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    // Spy on console log
    const consoleSpy = spyOn(console, 'log');

    const registerLink = compiled.querySelector('a[href="/register"]');
    const loginLink = compiled.querySelector('a[href="/login"]');
    const adminLink = compiled.querySelector('a[href="/admin"]');

    // Simulate clicks
    registerLink?.dispatchEvent(new Event('click'));
    loginLink?.dispatchEvent(new Event('click'));
    adminLink?.dispatchEvent(new Event('click'));

    expect(consoleSpy).toHaveBeenCalledWith('Navigacija na:', '/register');
    expect(consoleSpy).toHaveBeenCalledWith('Navigacija na:', '/login');
    expect(consoleSpy).toHaveBeenCalledWith('Navigacija na:', '/admin');
  });
});
