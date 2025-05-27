import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { UserReservationsDetailComponent } from './user-reservations-detail.component';

describe('UserReservations Management Detail Component', () => {
  let comp: UserReservationsDetailComponent;
  let fixture: ComponentFixture<UserReservationsDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserReservationsDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./user-reservations-detail.component').then(m => m.UserReservationsDetailComponent),
              resolve: { userReservations: () => of({ id: 9666 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(UserReservationsDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserReservationsDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load userReservations on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', UserReservationsDetailComponent);

      // THEN
      expect(instance.userReservations()).toEqual(expect.objectContaining({ id: 9666 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
