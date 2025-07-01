import { Facility } from './facility.model';

// model/exercise.model.ts
export interface Exercise {
  id: number;
  startTime: Date;
  endTime: Date;
  facility: Facility;
  userId: number;
  // Dodajemo naziv teretane radi lakšeg prikaza
}
