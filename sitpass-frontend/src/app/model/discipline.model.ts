import { Facility } from '../model/facility.model';

export interface Discipline {
  id: number;
  facility: Facility;
  name: string;
}
