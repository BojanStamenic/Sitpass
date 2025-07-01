export interface User {
  id: number;
  email: string;
  name: string;
  surname: string;
  address: string;
  phoneNumber?: string; // Ako je opcionalno
  city?: string;
  zipCode?: string;
  password?: string;
  birthday?: Date;
}
