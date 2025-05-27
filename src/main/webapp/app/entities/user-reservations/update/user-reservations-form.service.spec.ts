import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../user-reservations.test-samples';

import { UserReservationsFormService } from './user-reservations-form.service';

describe('UserReservations Form Service', () => {
  let service: UserReservationsFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserReservationsFormService);
  });

  describe('Service methods', () => {
    describe('createUserReservationsFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createUserReservationsFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            totalSize: expect.any(Object),
            usedSize: expect.any(Object),
            activated: expect.any(Object),
            createdBy: expect.any(Object),
            createdDate: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IUserReservations should create a new form with FormGroup', () => {
        const formGroup = service.createUserReservationsFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            totalSize: expect.any(Object),
            usedSize: expect.any(Object),
            activated: expect.any(Object),
            createdBy: expect.any(Object),
            createdDate: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getUserReservations', () => {
      it('should return NewUserReservations for default UserReservations initial value', () => {
        const formGroup = service.createUserReservationsFormGroup(sampleWithNewData);

        const userReservations = service.getUserReservations(formGroup) as any;

        expect(userReservations).toMatchObject(sampleWithNewData);
      });

      it('should return NewUserReservations for empty UserReservations initial value', () => {
        const formGroup = service.createUserReservationsFormGroup();

        const userReservations = service.getUserReservations(formGroup) as any;

        expect(userReservations).toMatchObject({});
      });

      it('should return IUserReservations', () => {
        const formGroup = service.createUserReservationsFormGroup(sampleWithRequiredData);

        const userReservations = service.getUserReservations(formGroup) as any;

        expect(userReservations).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IUserReservations should not enable id FormControl', () => {
        const formGroup = service.createUserReservationsFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewUserReservations should disable id FormControl', () => {
        const formGroup = service.createUserReservationsFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
