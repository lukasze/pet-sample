import { IPet } from 'app/shared/model/pet.model';

export interface IOwner {
  id?: number;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  pets?: IPet[] | null;
}

export const defaultValue: Readonly<IOwner> = {};
