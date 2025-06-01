// Common types
export interface User {
  id: string;
  username: string;
  role: 'admin' | 'guest';
}

// Movie related types
export interface Movie {
  movieId: string | number;
  movieName: string;
  description?: string;
  duration: number;
  genre?: string;
}

// Theatre related types
export interface Theatre {
  theatreId: string | number;
  theatreName: string;
  address: string;
  cityId: number;
  cityName?: string;
}

// Show related types
export interface Show {
  showId: string | number;
  movieId: number;
  theatreId: number;
  showTime: string;
  price: number;
  availableSeats?: number;
  movie?: Movie;
  theatre?: Theatre;
}

// Seat related types
export interface Seat {
  id?: string | number;  // For backward compatibility
  seatId: string | number;
  seatNumber: string;
  seatType: string;
  available: boolean;
  theatreId: number;
  price?: number;
}

// Booking related types
export interface Booking {
  bookingId: string | number;
  showId: number;
  userId?: string;
  bookingTime: string;
  totalAmount: number;
  status: 'CONFIRMED' | 'CANCELLED' | 'PENDING';
  seats: Seat[];
  show?: Show;
}

// City related types
export interface City {
  cityId: number;
  name: string;
}

// Auth related types
export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
}

// API response types
export interface ApiResponse<T = any> {
  data?: T;
  error?: string;
  success: boolean;
  status: number;
}

export interface BookingResponse {
  message: string;
  holdId: string;
  paymentAddress?: string;
  paymentMethod?: string;
  price?: number;
}
