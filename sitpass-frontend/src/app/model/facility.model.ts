import { Discipline } from '../model/discipline.model';
import { WorkDay } from '../model/workday.model';

export interface Facility {
  id: number;
  name: string;
  description: string;
  createdAt: string; // Assuming createdAt is in string format
  address: string;
  city: string;
  totalRating: number;
  active: boolean;
  images: string[];
  disciplines: Discipline[];
  workDays: WorkDay[];
  pdfObjectKey?: string;
  pdfFileName?: string;
  highlight?: string;
}
