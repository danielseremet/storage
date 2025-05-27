import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUserReservations } from '../user-reservations.model';
import { UserReservationsService } from '../service/user-reservations.service';
import { UserReservationsFormGroup, UserReservationsFormService } from './user-reservations-form.service';

@Component({
  selector: 'jhi-user-reservations-update',
  templateUrl: './user-reservations-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class UserReservationsUpdateComponent implements OnInit {
  isSaving = false;
  userReservations: IUserReservations | null = null;

  usersSharedCollection: IUser[] = [];

  protected userReservationsService = inject(UserReservationsService);
  protected userReservationsFormService = inject(UserReservationsFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: UserReservationsFormGroup = this.userReservationsFormService.createUserReservationsFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ userReservations }) => {
      this.userReservations = userReservations;
      if (userReservations) {
        this.updateForm(userReservations);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const userReservations = this.userReservationsFormService.getUserReservations(this.editForm);
    if (userReservations.id !== null) {
      this.subscribeToSaveResponse(this.userReservationsService.update(userReservations));
    } else {
      this.subscribeToSaveResponse(this.userReservationsService.create(userReservations));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IUserReservations>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(userReservations: IUserReservations): void {
    this.userReservations = userReservations;
    this.userReservationsFormService.resetForm(this.editForm, userReservations);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, userReservations.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.userReservations?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
