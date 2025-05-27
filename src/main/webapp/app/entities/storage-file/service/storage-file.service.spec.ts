import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IStorageFile } from '../storage-file.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../storage-file.test-samples';

import { RestStorageFile, StorageFileService } from './storage-file.service';

const requireRestSample: RestStorageFile = {
  ...sampleWithRequiredData,
  createdDate: sampleWithRequiredData.createdDate?.toJSON(),
};

describe('StorageFile Service', () => {
  let service: StorageFileService;
  let httpMock: HttpTestingController;
  let expectedResult: IStorageFile | IStorageFile[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(StorageFileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a StorageFile', () => {
      const storageFile = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(storageFile).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a StorageFile', () => {
      const storageFile = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(storageFile).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a StorageFile', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of StorageFile', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a StorageFile', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addStorageFileToCollectionIfMissing', () => {
      it('should add a StorageFile to an empty array', () => {
        const storageFile: IStorageFile = sampleWithRequiredData;
        expectedResult = service.addStorageFileToCollectionIfMissing([], storageFile);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(storageFile);
      });

      it('should not add a StorageFile to an array that contains it', () => {
        const storageFile: IStorageFile = sampleWithRequiredData;
        const storageFileCollection: IStorageFile[] = [
          {
            ...storageFile,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addStorageFileToCollectionIfMissing(storageFileCollection, storageFile);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a StorageFile to an array that doesn't contain it", () => {
        const storageFile: IStorageFile = sampleWithRequiredData;
        const storageFileCollection: IStorageFile[] = [sampleWithPartialData];
        expectedResult = service.addStorageFileToCollectionIfMissing(storageFileCollection, storageFile);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(storageFile);
      });

      it('should add only unique StorageFile to an array', () => {
        const storageFileArray: IStorageFile[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const storageFileCollection: IStorageFile[] = [sampleWithRequiredData];
        expectedResult = service.addStorageFileToCollectionIfMissing(storageFileCollection, ...storageFileArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const storageFile: IStorageFile = sampleWithRequiredData;
        const storageFile2: IStorageFile = sampleWithPartialData;
        expectedResult = service.addStorageFileToCollectionIfMissing([], storageFile, storageFile2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(storageFile);
        expect(expectedResult).toContain(storageFile2);
      });

      it('should accept null and undefined values', () => {
        const storageFile: IStorageFile = sampleWithRequiredData;
        expectedResult = service.addStorageFileToCollectionIfMissing([], null, storageFile, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(storageFile);
      });

      it('should return initial array if no StorageFile is added', () => {
        const storageFileCollection: IStorageFile[] = [sampleWithRequiredData];
        expectedResult = service.addStorageFileToCollectionIfMissing(storageFileCollection, undefined, null);
        expect(expectedResult).toEqual(storageFileCollection);
      });
    });

    describe('compareStorageFile', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareStorageFile(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 11027 };
        const entity2 = null;

        const compareResult1 = service.compareStorageFile(entity1, entity2);
        const compareResult2 = service.compareStorageFile(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 11027 };
        const entity2 = { id: 11476 };

        const compareResult1 = service.compareStorageFile(entity1, entity2);
        const compareResult2 = service.compareStorageFile(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 11027 };
        const entity2 = { id: 11027 };

        const compareResult1 = service.compareStorageFile(entity1, entity2);
        const compareResult2 = service.compareStorageFile(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
