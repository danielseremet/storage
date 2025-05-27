import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IStorageFile {
  id: number;
  name?: string | null;
  size?: number | null;
  mimeType?: string | null;
  path?: string | null;
  createdBy?: string | null;
  createdDate?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewStorageFile = Omit<IStorageFile, 'id'> & { id: null };
