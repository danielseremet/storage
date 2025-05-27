import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IUserReservations, NewUserReservations } from '../user-reservations.model';

export type PartialUpdateUserReservations = Partial<IUserReservations> & Pick<IUserReservations, 'id'>;

type RestOf<T extends IUserReservations | NewUserReservations> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

export type RestUserReservations = RestOf<IUserReservations>;

export type NewRestUserReservations = RestOf<NewUserReservations>;

export type PartialUpdateRestUserReservations = RestOf<PartialUpdateUserReservations>;

export type EntityResponseType = HttpResponse<IUserReservations>;
export type EntityArrayResponseType = HttpResponse<IUserReservations[]>;

@Injectable({ providedIn: 'root' })
export class UserReservationsService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/user-reservations');

  create(userReservations: NewUserReservations): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userReservations);
    return this.http
      .post<RestUserReservations>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(userReservations: IUserReservations): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userReservations);
    return this.http
      .put<RestUserReservations>(`${this.resourceUrl}/${this.getUserReservationsIdentifier(userReservations)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(userReservations: PartialUpdateUserReservations): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userReservations);
    return this.http
      .patch<RestUserReservations>(`${this.resourceUrl}/${this.getUserReservationsIdentifier(userReservations)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestUserReservations>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestUserReservations[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getUserReservationsIdentifier(userReservations: Pick<IUserReservations, 'id'>): number {
    return userReservations.id;
  }

  compareUserReservations(o1: Pick<IUserReservations, 'id'> | null, o2: Pick<IUserReservations, 'id'> | null): boolean {
    return o1 && o2 ? this.getUserReservationsIdentifier(o1) === this.getUserReservationsIdentifier(o2) : o1 === o2;
  }

  addUserReservationsToCollectionIfMissing<Type extends Pick<IUserReservations, 'id'>>(
    userReservationsCollection: Type[],
    ...userReservationsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const userReservations: Type[] = userReservationsToCheck.filter(isPresent);
    if (userReservations.length > 0) {
      const userReservationsCollectionIdentifiers = userReservationsCollection.map(userReservationsItem =>
        this.getUserReservationsIdentifier(userReservationsItem),
      );
      const userReservationsToAdd = userReservations.filter(userReservationsItem => {
        const userReservationsIdentifier = this.getUserReservationsIdentifier(userReservationsItem);
        if (userReservationsCollectionIdentifiers.includes(userReservationsIdentifier)) {
          return false;
        }
        userReservationsCollectionIdentifiers.push(userReservationsIdentifier);
        return true;
      });
      return [...userReservationsToAdd, ...userReservationsCollection];
    }
    return userReservationsCollection;
  }

  protected convertDateFromClient<T extends IUserReservations | NewUserReservations | PartialUpdateUserReservations>(
    userReservations: T,
  ): RestOf<T> {
    return {
      ...userReservations,
      createdDate: userReservations.createdDate?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restUserReservations: RestUserReservations): IUserReservations {
    return {
      ...restUserReservations,
      createdDate: restUserReservations.createdDate ? dayjs(restUserReservations.createdDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestUserReservations>): HttpResponse<IUserReservations> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestUserReservations[]>): HttpResponse<IUserReservations[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }

}
