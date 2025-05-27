import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import StorageFileResolve from './route/storage-file-routing-resolve.service';

const storageFileRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/storage-file.component').then(m => m.StorageFileComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/storage-file-detail.component').then(m => m.StorageFileDetailComponent),
    resolve: {
      storageFile: StorageFileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/storage-file-update.component').then(m => m.StorageFileUpdateComponent),
    resolve: {
      storageFile: StorageFileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/storage-file-update.component').then(m => m.StorageFileUpdateComponent),
    resolve: {
      storageFile: StorageFileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default storageFileRoute;
