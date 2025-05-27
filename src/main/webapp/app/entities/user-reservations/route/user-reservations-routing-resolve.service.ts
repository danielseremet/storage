import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IUserReservations } from '../user-reservations.model';
import { UserReservationsService } from '../service/user-reservations.service';

const userReservationsResolve = (route: ActivatedRouteSnapshot): Observable<null | IUserReservations> => {
  const id = route.params.id;
  if (id) {
    return inject(UserReservationsService)
      .find(id)
      .pipe(
        mergeMap((userReservations: HttpResponse<IUserReservations>) => {
          if (userReservations.body) {
            return of(userReservations.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default userReservationsResolve;
