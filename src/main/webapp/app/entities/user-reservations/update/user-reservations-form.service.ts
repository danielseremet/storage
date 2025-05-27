import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IUserReservations, NewUserReservations } from '../user-reservations.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IUserReservations for edit and NewUserReservationsFormGroupInput for create.
 */
type UserReservationsFormGroupInput = IUserReservations | PartialWithRequiredKeyOf<NewUserReservations>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IUserReservations | NewUserReservations> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

type UserReservationsFormRawValue = FormValueOf<IUserReservations>;

type NewUserReservationsFormRawValue = FormValueOf<NewUserReservations>;

type UserReservationsFormDefaults = Pick<NewUserReservations, 'id' | 'activated' | 'createdDate'>;

type UserReservationsFormGroupContent = {
  id: FormControl<UserReservationsFormRawValue['id'] | NewUserReservations['id']>;
  totalSize: FormControl<UserReservationsFormRawValue['totalSize']>;
  usedSize: FormControl<UserReservationsFormRawValue['usedSize']>;
  activated: FormControl<UserReservationsFormRawValue['activated']>;
  createdBy: FormControl<UserReservationsFormRawValue['createdBy']>;
  createdDate: FormControl<UserReservationsFormRawValue['createdDate']>;
  user: FormControl<UserReservationsFormRawValue['user']>;
};

export type UserReservationsFormGroup = FormGroup<UserReservationsFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class UserReservationsFormService {
  createUserReservationsFormGroup(userReservations: UserReservationsFormGroupInput = { id: null }): UserReservationsFormGroup {
    const userReservationsRawValue = this.convertUserReservationsToUserReservationsRawValue({
      ...this.getFormDefaults(),
      ...userReservations,
    });
    return new FormGroup<UserReservationsFormGroupContent>({
      id: new FormControl(
        { value: userReservationsRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      totalSize: new FormControl(userReservationsRawValue.totalSize),
      usedSize: new FormControl(userReservationsRawValue.usedSize),
      activated: new FormControl(userReservationsRawValue.activated),
      createdBy: new FormControl(userReservationsRawValue.createdBy),
      createdDate: new FormControl(userReservationsRawValue.createdDate),
      user: new FormControl(userReservationsRawValue.user),
    });
  }

  getUserReservations(form: UserReservationsFormGroup): IUserReservations | NewUserReservations {
    return this.convertUserReservationsRawValueToUserReservations(
      form.getRawValue() as UserReservationsFormRawValue | NewUserReservationsFormRawValue,
    );
  }

  resetForm(form: UserReservationsFormGroup, userReservations: UserReservationsFormGroupInput): void {
    const userReservationsRawValue = this.convertUserReservationsToUserReservationsRawValue({
      ...this.getFormDefaults(),
      ...userReservations,
    });
    form.reset(
      {
        ...userReservationsRawValue,
        id: { value: userReservationsRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): UserReservationsFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      activated: false,
      createdDate: currentTime,
    };
  }

  private convertUserReservationsRawValueToUserReservations(
    rawUserReservations: UserReservationsFormRawValue | NewUserReservationsFormRawValue,
  ): IUserReservations | NewUserReservations {
    return {
      ...rawUserReservations,
      createdDate: dayjs(rawUserReservations.createdDate, DATE_TIME_FORMAT),
    };
  }

  private convertUserReservationsToUserReservationsRawValue(
    userReservations: IUserReservations | (Partial<NewUserReservations> & UserReservationsFormDefaults),
  ): UserReservationsFormRawValue | PartialWithRequiredKeyOf<NewUserReservationsFormRawValue> {
    return {
      ...userReservations,
      createdDate: userReservations.createdDate ? userReservations.createdDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
