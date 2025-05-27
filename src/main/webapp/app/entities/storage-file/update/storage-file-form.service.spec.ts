import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../storage-file.test-samples';

import { StorageFileFormService } from './storage-file-form.service';

describe('StorageFile Form Service', () => {
  let service: StorageFileFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StorageFileFormService);
  });

  describe('Service methods', () => {
    describe('createStorageFileFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createStorageFileFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            size: expect.any(Object),
            mimeType: expect.any(Object),
            path: expect.any(Object),
            createdBy: expect.any(Object),
            createdDate: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IStorageFile should create a new form with FormGroup', () => {
        const formGroup = service.createStorageFileFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            size: expect.any(Object),
            mimeType: expect.any(Object),
            path: expect.any(Object),
            createdBy: expect.any(Object),
            createdDate: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getStorageFile', () => {
      it('should return NewStorageFile for default StorageFile initial value', () => {
        const formGroup = service.createStorageFileFormGroup(sampleWithNewData);

        const storageFile = service.getStorageFile(formGroup) as any;

        expect(storageFile).toMatchObject(sampleWithNewData);
      });

      it('should return NewStorageFile for empty StorageFile initial value', () => {
        const formGroup = service.createStorageFileFormGroup();

        const storageFile = service.getStorageFile(formGroup) as any;

        expect(storageFile).toMatchObject({});
      });

      it('should return IStorageFile', () => {
        const formGroup = service.createStorageFileFormGroup(sampleWithRequiredData);

        const storageFile = service.getStorageFile(formGroup) as any;

        expect(storageFile).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IStorageFile should not enable id FormControl', () => {
        const formGroup = service.createStorageFileFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewStorageFile should disable id FormControl', () => {
        const formGroup = service.createStorageFileFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
