import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import UserReservationsResolve from './route/user-reservations-routing-resolve.service';

const userReservationsRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/user-reservations.component').then(m => m.UserReservationsComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/user-reservations-detail.component').then(m => m.UserReservationsDetailComponent),
    resolve: {
      userReservations: UserReservationsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/user-reservations-update.component').then(m => m.UserReservationsUpdateComponent),
    resolve: {
      userReservations: UserReservationsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/user-reservations-update.component').then(m => m.UserReservationsUpdateComponent),
    resolve: {
      userReservations: UserReservationsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default userReservationsRoute;
