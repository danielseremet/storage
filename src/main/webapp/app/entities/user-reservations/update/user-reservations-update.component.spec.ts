import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { UserReservationsService } from '../service/user-reservations.service';
import { IUserReservations } from '../user-reservations.model';
import { UserReservationsFormService } from './user-reservations-form.service';

import { UserReservationsUpdateComponent } from './user-reservations-update.component';

describe('UserReservations Management Update Component', () => {
  let comp: UserReservationsUpdateComponent;
  let fixture: ComponentFixture<UserReservationsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let userReservationsFormService: UserReservationsFormService;
  let userReservationsService: UserReservationsService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UserReservationsUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(UserReservationsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(UserReservationsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    userReservationsFormService = TestBed.inject(UserReservationsFormService);
    userReservationsService = TestBed.inject(UserReservationsService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const userReservations: IUserReservations = { id: 10172 };
      const user: IUser = { id: 3944 };
      userReservations.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ userReservations });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const userReservations: IUserReservations = { id: 10172 };
      const user: IUser = { id: 3944 };
      userReservations.user = user;

      activatedRoute.data = of({ userReservations });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.userReservations).toEqual(userReservations);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUserReservations>>();
      const userReservations = { id: 9666 };
      jest.spyOn(userReservationsFormService, 'getUserReservations').mockReturnValue(userReservations);
      jest.spyOn(userReservationsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userReservations });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: userReservations }));
      saveSubject.complete();

      // THEN
      expect(userReservationsFormService.getUserReservations).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(userReservationsService.update).toHaveBeenCalledWith(expect.objectContaining(userReservations));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUserReservations>>();
      const userReservations = { id: 9666 };
      jest.spyOn(userReservationsFormService, 'getUserReservations').mockReturnValue({ id: null });
      jest.spyOn(userReservationsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userReservations: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: userReservations }));
      saveSubject.complete();

      // THEN
      expect(userReservationsFormService.getUserReservations).toHaveBeenCalled();
      expect(userReservationsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUserReservations>>();
      const userReservations = { id: 9666 };
      jest.spyOn(userReservationsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userReservations });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(userReservationsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
