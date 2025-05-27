import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IStorageFile } from '../storage-file.model';
import { StorageFileService } from '../service/storage-file.service';

const storageFileResolve = (route: ActivatedRouteSnapshot): Observable<null | IStorageFile> => {
  const id = route.params.id;
  if (id) {
    return inject(StorageFileService)
      .find(id)
      .pipe(
        mergeMap((storageFile: HttpResponse<IStorageFile>) => {
          if (storageFile.body) {
            return of(storageFile.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default storageFileResolve;
