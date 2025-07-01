// src/app/model/rate.model.ts
import { Review } from './review.model';

export interface Rate {
  id: number;
  equipment: number;
  staff: number;
  hygiene: number;
  space: number;
  review: Review | null;
}
