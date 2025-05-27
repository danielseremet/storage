import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IStorageFile, NewStorageFile } from '../storage-file.model';

export type PartialUpdateStorageFile = Partial<IStorageFile> & Pick<IStorageFile, 'id'>;

type RestOf<T extends IStorageFile | NewStorageFile> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

export type RestStorageFile = RestOf<IStorageFile>;

export type NewRestStorageFile = RestOf<NewStorageFile>;

export type PartialUpdateRestStorageFile = RestOf<PartialUpdateStorageFile>;

export type EntityResponseType = HttpResponse<IStorageFile>;
export type EntityArrayResponseType = HttpResponse<IStorageFile[]>;

@Injectable({ providedIn: 'root' })
export class StorageFileService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/storage-files');

  create(storageFile: NewStorageFile): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(storageFile);
    return this.http
      .post<RestStorageFile>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(storageFile: IStorageFile): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(storageFile);
    return this.http
      .put<RestStorageFile>(`${this.resourceUrl}/${this.getStorageFileIdentifier(storageFile)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(storageFile: PartialUpdateStorageFile): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(storageFile);
    return this.http
      .patch<RestStorageFile>(`${this.resourceUrl}/${this.getStorageFileIdentifier(storageFile)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestStorageFile>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestStorageFile[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getStorageFileIdentifier(storageFile: Pick<IStorageFile, 'id'>): number {
    return storageFile.id;
  }

  compareStorageFile(o1: Pick<IStorageFile, 'id'> | null, o2: Pick<IStorageFile, 'id'> | null): boolean {
    return o1 && o2 ? this.getStorageFileIdentifier(o1) === this.getStorageFileIdentifier(o2) : o1 === o2;
  }

  addStorageFileToCollectionIfMissing<Type extends Pick<IStorageFile, 'id'>>(
    storageFileCollection: Type[],
    ...storageFilesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const storageFiles: Type[] = storageFilesToCheck.filter(isPresent);
    if (storageFiles.length > 0) {
      const storageFileCollectionIdentifiers = storageFileCollection.map(storageFileItem => this.getStorageFileIdentifier(storageFileItem));
      const storageFilesToAdd = storageFiles.filter(storageFileItem => {
        const storageFileIdentifier = this.getStorageFileIdentifier(storageFileItem);
        if (storageFileCollectionIdentifiers.includes(storageFileIdentifier)) {
          return false;
        }
        storageFileCollectionIdentifiers.push(storageFileIdentifier);
        return true;
      });
      return [...storageFilesToAdd, ...storageFileCollection];
    }
    return storageFileCollection;
  }

  protected convertDateFromClient<T extends IStorageFile | NewStorageFile | PartialUpdateStorageFile>(storageFile: T): RestOf<T> {
    return {
      ...storageFile,
      createdDate: storageFile.createdDate?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restStorageFile: RestStorageFile): IStorageFile {
    return {
      ...restStorageFile,
      createdDate: restStorageFile.createdDate ? dayjs(restStorageFile.createdDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestStorageFile>): HttpResponse<IStorageFile> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestStorageFile[]>): HttpResponse<IStorageFile[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
