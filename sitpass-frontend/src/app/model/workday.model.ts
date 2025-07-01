import { Facility } from '../model/facility.model';

export interface WorkDay {
  id: number;
  day: string; // Assuming day is a string like 'Monday', 'Tuesday', etc.
  startTime: string; // Should be in 'HH:mm:ss' or appropriate format
  endTime: string;
  validFrom: string;
  facility: Facility; // Should be in 'HH:mm:ss' or appropriate format
}
