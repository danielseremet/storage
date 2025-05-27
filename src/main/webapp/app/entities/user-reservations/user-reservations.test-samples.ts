import dayjs from 'dayjs/esm';

import { IUserReservations, NewUserReservations } from './user-reservations.model';

export const sampleWithRequiredData: IUserReservations = {
  id: 15591,
};

export const sampleWithPartialData: IUserReservations = {
  id: 8945,
};

export const sampleWithFullData: IUserReservations = {
  id: 9381,
  totalSize: 29028,
  usedSize: 19993,
  activated: true,
  createdBy: 'blend shadowy',
  createdDate: dayjs('2025-05-21T19:35'),
};

export const sampleWithNewData: NewUserReservations = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
