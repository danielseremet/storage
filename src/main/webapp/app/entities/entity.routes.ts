import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'storage-file',
    data: { pageTitle: 'StorageFiles' },
    loadChildren: () => import('./storage-file/storage-file.routes'),
  },
  {
    path: 'user-reservations',
    data: { pageTitle: 'UserReservations' },
    loadChildren: () => import('./user-reservations/user-reservations.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
