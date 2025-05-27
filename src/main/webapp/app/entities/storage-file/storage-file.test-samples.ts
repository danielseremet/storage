import dayjs from 'dayjs/esm';

import { IStorageFile, NewStorageFile } from './storage-file.model';

export const sampleWithRequiredData: IStorageFile = {
  id: 13108,
};

export const sampleWithPartialData: IStorageFile = {
  id: 4223,
  name: 'broadside versus since',
  mimeType: 'woot',
  createdBy: 'meh woot',
};

export const sampleWithFullData: IStorageFile = {
  id: 27232,
  name: 'winged chow ameliorate',
  size: 23517,
  mimeType: 'pushy',
  path: 'whose nor',
  createdBy: 'consequently wring',
  createdDate: dayjs('2025-05-21T17:46'),
};

export const sampleWithNewData: NewStorageFile = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
