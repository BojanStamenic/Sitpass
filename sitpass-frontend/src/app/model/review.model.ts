// src/app/model/review.model.ts
import { User } from './user.model';
import { Facility } from './facility.model';
import { Rate } from './rate.model';

export interface Review {
  id: number;
  createdAt: Date;
  exerciseCount: number;
  hidden: boolean;
  user: User | null;
  facility: Facility | null;
  rates: Rate | null;
  comments: any[]; // Ovaj možete prilagoditi ako imate model za komentare
}
