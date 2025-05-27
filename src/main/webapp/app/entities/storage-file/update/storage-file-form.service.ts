import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IStorageFile, NewStorageFile } from '../storage-file.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IStorageFile for edit and NewStorageFileFormGroupInput for create.
 */
type StorageFileFormGroupInput = IStorageFile | PartialWithRequiredKeyOf<NewStorageFile>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IStorageFile | NewStorageFile> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

type StorageFileFormRawValue = FormValueOf<IStorageFile>;

type NewStorageFileFormRawValue = FormValueOf<NewStorageFile>;

type StorageFileFormDefaults = Pick<NewStorageFile, 'id' | 'createdDate'>;

type StorageFileFormGroupContent = {
  id: FormControl<StorageFileFormRawValue['id'] | NewStorageFile['id']>;
  name: FormControl<StorageFileFormRawValue['name']>;
  size: FormControl<StorageFileFormRawValue['size']>;
  mimeType: FormControl<StorageFileFormRawValue['mimeType']>;
  path: FormControl<StorageFileFormRawValue['path']>;
  createdBy: FormControl<StorageFileFormRawValue['createdBy']>;
  createdDate: FormControl<StorageFileFormRawValue['createdDate']>;
  user: FormControl<StorageFileFormRawValue['user']>;
};

export type StorageFileFormGroup = FormGroup<StorageFileFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class StorageFileFormService {
  createStorageFileFormGroup(storageFile: StorageFileFormGroupInput = { id: null }): StorageFileFormGroup {
    const storageFileRawValue = this.convertStorageFileToStorageFileRawValue({
      ...this.getFormDefaults(),
      ...storageFile,
    });
    return new FormGroup<StorageFileFormGroupContent>({
      id: new FormControl(
        { value: storageFileRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(storageFileRawValue.name),
      size: new FormControl(storageFileRawValue.size),
      mimeType: new FormControl(storageFileRawValue.mimeType),
      path: new FormControl(storageFileRawValue.path),
      createdBy: new FormControl(storageFileRawValue.createdBy),
      createdDate: new FormControl(storageFileRawValue.createdDate),
      user: new FormControl(storageFileRawValue.user),
    });
  }

  getStorageFile(form: StorageFileFormGroup): IStorageFile | NewStorageFile {
    return this.convertStorageFileRawValueToStorageFile(form.getRawValue() as StorageFileFormRawValue | NewStorageFileFormRawValue);
  }

  resetForm(form: StorageFileFormGroup, storageFile: StorageFileFormGroupInput): void {
    const storageFileRawValue = this.convertStorageFileToStorageFileRawValue({ ...this.getFormDefaults(), ...storageFile });
    form.reset(
      {
        ...storageFileRawValue,
        id: { value: storageFileRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): StorageFileFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdDate: currentTime,
    };
  }

  private convertStorageFileRawValueToStorageFile(
    rawStorageFile: StorageFileFormRawValue | NewStorageFileFormRawValue,
  ): IStorageFile | NewStorageFile {
    return {
      ...rawStorageFile,
      createdDate: dayjs(rawStorageFile.createdDate, DATE_TIME_FORMAT),
    };
  }

  private convertStorageFileToStorageFileRawValue(
    storageFile: IStorageFile | (Partial<NewStorageFile> & StorageFileFormDefaults),
  ): StorageFileFormRawValue | PartialWithRequiredKeyOf<NewStorageFileFormRawValue> {
    return {
      ...storageFile,
      createdDate: storageFile.createdDate ? storageFile.createdDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
