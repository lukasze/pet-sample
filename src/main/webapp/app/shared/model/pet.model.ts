import { IOwner } from 'app/shared/model/owner.model';

export interface IPet {
  id?: number;
  name?: string | null;
  kind?: string | null;
  age?: number | null;
  owner?: IOwner | null;
}

export const defaultValue: Readonly<IPet> = {};
