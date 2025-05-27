import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IUserReservations {
  id: number;
  totalSize?: number | null;
  usedSize?: number | null;
  activated?: boolean | null;
  createdBy?: string | null;
  createdDate?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewUserReservations = Omit<IUserReservations, 'id'> & { id: null };
