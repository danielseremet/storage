import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IUserReservations } from '../user-reservations.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../user-reservations.test-samples';

import { RestUserReservations, UserReservationsService } from './user-reservations.service';

const requireRestSample: RestUserReservations = {
  ...sampleWithRequiredData,
  createdDate: sampleWithRequiredData.createdDate?.toJSON(),
};

describe('UserReservations Service', () => {
  let service: UserReservationsService;
  let httpMock: HttpTestingController;
  let expectedResult: IUserReservations | IUserReservations[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(UserReservationsService);
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

    it('should create a UserReservations', () => {
      const userReservations = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(userReservations).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a UserReservations', () => {
      const userReservations = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(userReservations).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a UserReservations', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of UserReservations', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a UserReservations', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addUserReservationsToCollectionIfMissing', () => {
      it('should add a UserReservations to an empty array', () => {
        const userReservations: IUserReservations = sampleWithRequiredData;
        expectedResult = service.addUserReservationsToCollectionIfMissing([], userReservations);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(userReservations);
      });

      it('should not add a UserReservations to an array that contains it', () => {
        const userReservations: IUserReservations = sampleWithRequiredData;
        const userReservationsCollection: IUserReservations[] = [
          {
            ...userReservations,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addUserReservationsToCollectionIfMissing(userReservationsCollection, userReservations);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a UserReservations to an array that doesn't contain it", () => {
        const userReservations: IUserReservations = sampleWithRequiredData;
        const userReservationsCollection: IUserReservations[] = [sampleWithPartialData];
        expectedResult = service.addUserReservationsToCollectionIfMissing(userReservationsCollection, userReservations);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(userReservations);
      });

      it('should add only unique UserReservations to an array', () => {
        const userReservationsArray: IUserReservations[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const userReservationsCollection: IUserReservations[] = [sampleWithRequiredData];
        expectedResult = service.addUserReservationsToCollectionIfMissing(userReservationsCollection, ...userReservationsArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const userReservations: IUserReservations = sampleWithRequiredData;
        const userReservations2: IUserReservations = sampleWithPartialData;
        expectedResult = service.addUserReservationsToCollectionIfMissing([], userReservations, userReservations2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(userReservations);
        expect(expectedResult).toContain(userReservations2);
      });

      it('should accept null and undefined values', () => {
        const userReservations: IUserReservations = sampleWithRequiredData;
        expectedResult = service.addUserReservationsToCollectionIfMissing([], null, userReservations, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(userReservations);
      });

      it('should return initial array if no UserReservations is added', () => {
        const userReservationsCollection: IUserReservations[] = [sampleWithRequiredData];
        expectedResult = service.addUserReservationsToCollectionIfMissing(userReservationsCollection, undefined, null);
        expect(expectedResult).toEqual(userReservationsCollection);
      });
    });

    describe('compareUserReservations', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareUserReservations(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 9666 };
        const entity2 = null;

        const compareResult1 = service.compareUserReservations(entity1, entity2);
        const compareResult2 = service.compareUserReservations(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 9666 };
        const entity2 = { id: 10172 };

        const compareResult1 = service.compareUserReservations(entity1, entity2);
        const compareResult2 = service.compareUserReservations(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 9666 };
        const entity2 = { id: 9666 };

        const compareResult1 = service.compareUserReservations(entity1, entity2);
        const compareResult2 = service.compareUserReservations(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
