import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IStorageFile } from '../storage-file.model';
import { StorageFileService } from '../service/storage-file.service';
import { StorageFileFormGroup, StorageFileFormService } from './storage-file-form.service';

@Component({
  selector: 'jhi-storage-file-update',
  templateUrl: './storage-file-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class StorageFileUpdateComponent implements OnInit {
  isSaving = false;
  storageFile: IStorageFile | null = null;

  usersSharedCollection: IUser[] = [];

  protected storageFileService = inject(StorageFileService);
  protected storageFileFormService = inject(StorageFileFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: StorageFileFormGroup = this.storageFileFormService.createStorageFileFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ storageFile }) => {
      this.storageFile = storageFile;
      if (storageFile) {
        this.updateForm(storageFile);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const storageFile = this.storageFileFormService.getStorageFile(this.editForm);
    if (storageFile.id !== null) {
      this.subscribeToSaveResponse(this.storageFileService.update(storageFile));
    } else {
      this.subscribeToSaveResponse(this.storageFileService.create(storageFile));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IStorageFile>>): void {
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

  protected updateForm(storageFile: IStorageFile): void {
    this.storageFile = storageFile;
    this.storageFileFormService.resetForm(this.editForm, storageFile);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, storageFile.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.storageFile?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
