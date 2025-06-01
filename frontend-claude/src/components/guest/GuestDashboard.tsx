import React, { useState, useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Film, Clock, MapPin, ChevronRight } from 'lucide-react';
import { apiCall } from '../../services/api';
import { Movie, Show, Seat, Theatre } from '../../types';

interface MovieWithDetails extends Movie {
  genre?: string;
  language?: string;
}

interface ShowWithScreen extends Omit<Show, 'theatre' | 'price'> {
  screen?: string;
  screenId?: number;
  theatre: Theatre & { 
    cityName?: string;
    city?: string;
  };
  startTime?: number[];
  endTime?: number[];
  price: number;
}

interface BookingResponse {
  holdId: string;
  message: string;
  paymentAddress?: string;
  paymentMethod?: string;
  price?: number;
}

interface GuestDashboardProps {
  city: string;
  onCityChange: () => void;
}

const GuestDashboard: React.FC<GuestDashboardProps> = ({ city, onCityChange }) => {
  const queryClient = useQueryClient();
  const [selectedMovie, setSelectedMovie] = useState<MovieWithDetails | null>(null);
  const [selectedShow, setSelectedShow] = useState<ShowWithScreen | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);
  const [currentView, setCurrentView] = useState<'movies' | 'shows' | 'seats' | 'booking'>('movies');
  const [error, setError] = useState<string | null>(null);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<'UPI' | 'ETH'>('UPI');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [phoneError, setPhoneError] = useState<string | null>(null);

  // Reset selections when city changes
  React.useEffect(() => {
    setSelectedMovie(null);
    setSelectedShow(null);
    setSelectedSeats([]);
    setCurrentView('movies');
  }, [city]);

  // Fetch movies when city changes or in movies view
  const { data: movies = [], isLoading: isLoadingMovies, refetch: refetchMovies } = useQuery<MovieWithDetails[]>({
    queryKey: ['movies', city],
    queryFn: async () => {
      const data = await apiCall(`/movies/list`);
      return Array.isArray(data) ? data : [];
    },
    enabled: false, // We'll manually trigger this query
  });

  // Fetch movies when city changes or when in movies view
  useEffect(() => {
    if (city) {
      refetchMovies();
    }
  }, [city, refetchMovies]);

  // Also fetch movies when switching to movies view
  useEffect(() => {
    if (currentView === 'movies' && city) {
      refetchMovies();
    }
  }, [currentView, city, refetchMovies]);

  // Fetch shows for selected movie
  const { data: showsData = [], isLoading: isLoadingShows } = useQuery<any[]>({
    queryKey: ['shows', selectedMovie?.movieId],
    queryFn: async () => {
      if (!selectedMovie) return [];
      const data = await apiCall(`/shows/movie-id-${selectedMovie.movieId}`);
      console.log('Shows API Response:', {
        movieId: selectedMovie.movieId,
        movieName: selectedMovie.movieName,
        response: data,
        timestamp: new Date().toISOString()
      });
      return Array.isArray(data) ? data : [];
    },
    enabled: !!selectedMovie && currentView === 'shows',
  });
  
  // Flatten the shows array from the nested structure and add theatre info
  const shows = showsData.flatMap(theatre => 
    theatre.shows?.map((show: any) => {
      const processedShow = {
        ...show,
        theatre: {
          theatreId: theatre.theatreId,
          theatreName: theatre.theatreName,
          address: theatre.address,
          city: theatre.city
        },
        // Ensure we have startTime and endTime
        startTime: show.startTime || [],
        endTime: show.endTime || []
      };
      
      console.log('Processed Show:', {
        showId: processedShow.showId,
        movieId: processedShow.movieId,
        theatre: processedShow.theatre.theatreName,
        startTime: processedShow.startTime,
        endTime: processedShow.endTime,
        screenId: processedShow.screenId,
        timestamp: new Date().toISOString()
      });
      
      return processedShow;
    }) || []
  );

  // Fetch seats for selected show
  const { data: seats = [], isLoading: isLoadingSeats, refetch: refetchSeats } = useQuery<Seat[], Error>({
    queryKey: ['seats', selectedShow?.showId],
    queryFn: async (): Promise<Seat[]> => {
      if (!selectedShow) {
        console.log('No show selected, skipping seat fetch');
        return [];
      }
      
          console.group(`=== Fetching Seats for Show ${selectedShow.showId} ===`);
      console.log('Timestamp:', new Date().toISOString());
      console.log('Show details:', {
        movie: selectedMovie?.movieName,
        theatre: selectedShow.theatre?.theatreName,
        screenId: selectedShow.screenId,
        showTime: selectedShow.startTime ? formatShowTime(selectedShow.startTime) : 'N/A'
      });
      
      // Clear previous selections when fetching new seats
      setSelectedSeats([]);
      
      try {
        const endpoint = `/shows/show-id-${selectedShow.showId}`;
        console.log('Making API call to:', endpoint);
        
        const startTime = performance.now();
        const response = await apiCall<Array<{
          seatId: number;
          row: string;
          seatNo: number;
          category: string;
          seatIdentifier: string;
          available?: boolean;
          heldBy?: string | null;
        }>>(endpoint);
        const endTime = performance.now();
        
        console.log('Seat API response received in', (endTime - startTime).toFixed(2), 'ms');
        console.log('Raw seat data:', response);
        
        if (!Array.isArray(response)) {
          console.error('Expected array but got:', response);
          console.groupEnd();
          return [];
        }
        
        // Count available/held seats
        const seatStatus = response.reduce((acc, seat) => {
          if (seat.available === false || seat.heldBy) {
            acc.held++;
          } else {
            acc.available++;
          }
          return acc;
        }, { available: 0, held: 0 });
        
        console.log('Seat status:', seatStatus);
        
        // Transform the API response to match our Seat interface
        const processedSeats = response.map(seat => ({
          seatId: seat.seatId.toString(),
          seatNumber: seat.seatIdentifier,
          seatType: seat.category.toLowerCase(),
          available: seat.available !== false, // Rely on backend's availability status
          theatreId: selectedShow.theatreId || 0,
          price: selectedShow.price || 0
        }));
        
        console.log('Processed seats (first 3):', processedSeats.slice(0, 3));
        
        console.log('Processed seats (first 3):', processedSeats.slice(0, 3));
        console.groupEnd();
        
        return processedSeats;
        
      } catch (error) {
        console.error('Error fetching seats:', {
          error,
          message: error instanceof Error ? error.message : 'Unknown error',
          stack: error instanceof Error ? error.stack : undefined
        });
        console.groupEnd();
        return [];
      }
    },
    enabled: !!selectedShow && currentView === 'seats'
  });

  // Format show time from Java LocalDateTime array [year, month, day, hour, minute]
  const formatShowTime = (timeArray: number[]): string => {
    if (!Array.isArray(timeArray) || timeArray.length < 5) {
      console.error('Invalid time array format:', timeArray);
      return 'Invalid Time';
    }
    
    try {
      // Note: JavaScript months are 0-indexed (0-11), so we subtract 1 from month
      const [year, month, day, hours, minutes] = timeArray;
      const date = new Date(year, month - 1, day, hours, minutes);
      
      if (isNaN(date.getTime())) {
        console.error('Invalid date created from array:', timeArray);
        return 'Invalid Time';
      }
      
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch (error) {
      console.error('Error formatting show time:', error);
      return 'Invalid Time';
    }
  };

  // Clear any held seats when navigating away
  useEffect(() => {
    return () => {
      // This will run when the component unmounts
      if (selectedSeats.length > 0) {
        console.log('Clearing selected seats due to navigation');
        setSelectedSeats([]);
      }
    };
  }, [selectedSeats.length]);

  // Reset selections when going back
  const handleBackToMovies = () => {
    setSelectedMovie(null);
    setSelectedShow(null);
    setSelectedSeats([]);
    setCurrentView('movies');
  };

  const handleBackToShows = () => {
    setSelectedShow(null);
    setSelectedSeats([]);
    setCurrentView('shows');
  };

  const handleBackToSeats = () => {
    setCurrentView('seats');
  };

  const handleSelectMovie = (movie: MovieWithDetails) => {
    setSelectedMovie(movie);
    setCurrentView('shows');
  };

  const handleSelectShow = (show: ShowWithScreen) => {
    console.log('Show selected:', {
      showId: show.showId,
      movieId: show.movieId,
      theatre: show.theatre?.theatreName,
      screenId: show.screenId,
      startTime: show.startTime,
      endTime: show.endTime,
      timestamp: new Date().toISOString()
    });
    setSelectedShow(show);
    setCurrentView('seats');
  };

  const toggleSeatSelection = (seat: Seat) => {
    // Only allow selecting available seats
    if (!seat.available) {
      return;
    }
    
    setSelectedSeats(prev => {
      const isSelected = prev.some(s => s.seatId === seat.seatId);
      return isSelected 
        ? prev.filter(s => s.seatId !== seat.seatId)
        : [...prev, seat];
    });
  };

  const handleProceedToBooking = () => {
    if (selectedSeats.length === 0) {
      setError('Please select at least one seat');
      return;
    }
    console.log('Proceeding to booking with:', {
      showId: selectedShow?.showId,
      movieId: selectedMovie?.movieId,
      seats: selectedSeats,
      totalPrice: selectedSeats.length * (selectedShow?.price || 0)
    });
    setCurrentView('booking');
  };

  const navigate = useNavigate();

  const handleConfirmBooking = async () => {
    console.group('=== Starting Booking Process ===');
    console.log('Booking initiated at:', new Date().toISOString());
    
    if (!selectedShow || selectedSeats.length === 0) {
      const errorMsg = 'Please select at least one seat';
      console.warn('Booking validation failed:', errorMsg);
      setError(errorMsg);
      console.groupEnd();
      return;
    }
    
    // Log selected seats
    console.log('Attempting to book seats:', {
      showId: selectedShow.showId,
      movieName: selectedMovie?.movieName,
      seatCount: selectedSeats.length,
      seatIds: selectedSeats.map(s => s.seatId),
      seatNumbers: selectedSeats.map(s => s.seatNumber)
    });
    
    // Validate phone number
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneNumber || !phoneRegex.test(phoneNumber)) {
      const errorMsg = 'Please enter a valid 10-digit phone number';
      console.warn('Phone validation failed:', { phoneNumber, isValid: false });
      setPhoneError(errorMsg);
      console.groupEnd();
      return;
    }
    
    console.log('Starting booking process with payment method:', paymentMethod);
    setIsBooking(true);
    setBookingError(null);
    setPhoneError(null);
    
    try {
      // Define booking data type
      interface BookingRequest {
        showId: number;
        seatIds: number[];
        paymentMethod: string;  // Accept any payment method string
        phoneNumber: string;
      }

      // Ensure data types match backend expectations
      const bookingData: BookingRequest = {
        showId: Number(selectedShow.showId), // Convert to number (will be sent as number in JSON)
        seatIds: selectedSeats.map(seat => Number(seat.seatId)), // Convert string IDs to numbers
        paymentMethod: paymentMethod,
        phoneNumber: phoneNumber
      };
      
      console.log('Submitting booking:', bookingData);
      
      // Make the API call with proper typing
      console.log('Attempting to book seats:', {
        showId: bookingData.showId,
        seatIds: bookingData.seatIds,
        paymentMethod: bookingData.paymentMethod
      });
      
      const response = await apiCall<BookingResponse>('/booking/seats', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bookingData),
      });
      
      console.log('Booking response:', response);
      
      console.log('Booking response:', response);
      
      if (!response || !response.holdId) {
        throw new Error('No holdId received from server');
      }
      
      console.log('Booking successful, invalidating seat cache...');
      const invalidationStart = performance.now();
      
      try {
        await queryClient.invalidateQueries({ 
          queryKey: ['seats', selectedShow.showId],
          exact: true
        });
        console.log('Seat cache invalidated in', (performance.now() - invalidationStart).toFixed(2), 'ms');
        
        // Optionally force a refetch to ensure we have the latest data
        console.log('Triggering seat refetch...');
        const refetchStart = performance.now();
        const refetchResult = await refetchSeats();
        console.log('Seat refetch completed in', (performance.now() - refetchStart).toFixed(2), 'ms', {
          success: !refetchResult.isError,
          data: refetchResult.data ? `Array(${refetchResult.data.length})` : 'no data',
          error: refetchResult.error?.message
        });
      } catch (invalidationError) {
        console.error('Error during seat cache invalidation:', invalidationError);
      }
      
      // Check if payment method and address are available
      const responsePaymentMethod = response.paymentMethod;
      const paymentAddress = response.paymentAddress;
      
      console.log('Payment details from server:', {
        paymentMethod: responsePaymentMethod,
        paymentAddress: paymentAddress ? '***' + paymentAddress.slice(-4) : 'none',
        holdId: response.holdId,
        price: response.price
      });
      
      if (!responsePaymentMethod || !paymentAddress) {
        console.log('No payment required, redirecting to booking status');
        navigate(`/booking/status?holdId=${response.holdId}`);
      } else {
        console.log('Payment required, redirecting to payment page');
        navigate('/payment', {
          state: {
            holdId: response.holdId,
            paymentAddress: paymentAddress,
            paymentMethod: responsePaymentMethod,
            amount: response.price || 0,
            movieName: selectedMovie?.movieName || 'Movie',
            theatreName: selectedShow?.theatre?.theatreName || 'Theatre',
            showTime: selectedShow?.startTime ? formatShowTime(selectedShow.startTime) : 'N/A',
            seats: selectedSeats.map(seat => seat.seatNumber)
          }
        });
      }
      
    } catch (error) {
      let errorMessage = 'Failed to complete booking';
      
      // Handle different types of errors
      if (error instanceof Error) {
        // Check if this is an API error with a response
        const apiError = error as any;
        if (apiError.response) {
          // Handle 400 Bad Request with specific error message
          if (apiError.response.status === 400 && apiError.response.data?.message) {
            errorMessage = apiError.response.data.message;
          } else if (apiError.response.status === 409) {
            // Handle 409 Conflict (seat already held)
            errorMessage = apiError.response.data?.message || 'The selected seats are no longer available';
          }
        } else {
          errorMessage = error.message;
        }
      }
      
      console.error('Booking error:', {
        message: errorMessage,
        error: error,
        timestamp: new Date().toISOString(),
        showId: selectedShow?.showId,
        seatIds: selectedSeats.map(s => s.seatId)
      });
      
      setBookingError(errorMessage);
      
      // Refresh seat availability after an error
      try {
        console.log('Refreshing seat data after booking error...');
        await refetchSeats();
      } catch (refreshError) {
        console.error('Failed to refresh seat data:', refreshError);
      }
    } finally {
      console.log('Booking process completed at:', new Date().toISOString());
      setIsBooking(false);
      console.groupEnd();
    }
  };

  // Shows view
  if (currentView === 'shows' && selectedMovie) {
    return (
      <div className="space-y-6">
        <button
          onClick={handleBackToMovies}
          className="flex items-center text-blue-400 hover:text-blue-300 mb-4"
        >
          ← Back to Movies
        </button>
        
        <h2 className="text-2xl font-bold text-white">
          Select Show for {selectedMovie.movieName}
        </h2>
        
        {error && (
          <div className="bg-red-600 text-white p-4 rounded-lg mb-6">
            {error}
            <button onClick={() => setError('')} className="float-right font-bold">×</button>
          </div>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {shows.length > 0 ? (
            shows.map((show) => (
              <div 
                key={show.showId}
                className="bg-gray-800 p-4 rounded-lg cursor-pointer hover:bg-gray-700"
                onClick={() => handleSelectShow(show)}
              >
                <div className="flex justify-between items-center">
                  <div className="flex flex-col">
                    <span className="text-lg font-semibold text-white">
                      {formatShowTime(show.startTime)}
                    </span>
                    <span className="text-xs text-gray-400">
                      to {formatShowTime(show.endTime)}
                    </span>
                  </div>
                  <span className="text-green-400">₹{show.price || 'N/A'}</span>
                </div>
                <div className="mt-2 text-gray-400 text-sm">
                  <div className="flex items-center">
                    <MapPin size={14} className="mr-1 flex-shrink-0" />
                    <span className="truncate">
                      {show.theatre?.theatreName || 'Theater Name'}
                      {show.theatre?.city && `, ${show.theatre.city}`}
                    </span>
                  </div>
                  <div className="flex items-center mt-1">
                    <Clock size={14} className="mr-1" />
                    {Math.floor(selectedMovie.duration / 60)}h {selectedMovie.duration % 60}m
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="col-span-full text-center text-gray-400 py-8">
              No shows available for {selectedMovie.movieName} in {city}.
            </div>
          )}
        </div>
      </div>
    );
  }

  // Seats view
  if (currentView === 'seats' && selectedShow && selectedMovie) {
    return (
      <div className="space-y-6">
        <button
          onClick={handleBackToShows}
          className="flex items-center text-blue-400 hover:text-blue-300 mb-4"
        >
          ← Back to Shows
        </button>
        
        <div className="bg-gray-800 p-6 rounded-lg">
          <h2 className="text-2xl font-bold text-white mb-2">{selectedMovie.movieName}</h2>
          <div className="text-gray-400 mb-6">
            {formatShowTime(selectedShow.startTime || [])} • {selectedShow.theatre?.theatreName}
            {selectedShow.theatre?.city && `, ${selectedShow.theatre.city}`}
          </div>
          
          {/* Screen */}
          <div className="bg-gray-700 text-white text-center py-2 mb-8 mx-auto w-3/4 rounded">
            Screen
          </div>
          
          {/* Seats Grid */}
          {isLoadingSeats ? (
            <div className="flex justify-center items-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-8 gap-2 mb-8">
                {seats.map((seat) => (
                  <button
                    key={seat.seatId}
                    onClick={() => toggleSeatSelection(seat)}
                    disabled={!seat.available}
                    className={`h-8 rounded flex items-center justify-center text-sm
                      ${!seat.available 
                        ? 'bg-gray-700 text-gray-600 cursor-not-allowed' 
                        : selectedSeats.some(s => s.seatId === seat.seatId)
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-600 text-white hover:bg-gray-500'}`}
                  >
                    {seat.seatNumber}
                  </button>
                ))}
              </div>
              
              <div className="flex justify-between items-center">
                <div className="text-gray-400">
                  {selectedSeats.length} seat{selectedSeats.length !== 1 ? 's' : ''} selected • 
                  ₹{selectedSeats.length * (selectedShow.price || 0)}
                </div>
                <button
                  onClick={handleProceedToBooking}
                  disabled={selectedSeats.length === 0}
                  className={`px-6 py-2 rounded-lg font-medium
                    ${selectedSeats.length > 0 
                      ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                      : 'bg-gray-600 text-gray-400 cursor-not-allowed'}`}
                >
                  Proceed to Book
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    );
  }

  // Booking Confirmation View
  if (currentView === 'booking' && selectedShow && selectedMovie) {
    const totalAmount = selectedSeats.length * (selectedShow.price || 0);
    
    return (
      <div className="max-w-2xl mx-auto space-y-6">
        <h2 className="text-2xl font-bold text-white">Booking Status</h2>
        
        {isBooking ? (
          <div className="bg-green-900/20 border border-green-800 rounded-lg p-6 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-green-500 mx-auto mb-4"></div>
            <h3 className="text-xl font-semibold text-green-400 mb-4">Processing Your Booking</h3>
            <p className="text-green-300 mb-6">Your booking is being processed. You will be redirected to the booking status page shortly.</p>
          </div>
        ) : bookingError ? (
          <div className="bg-red-900/30 border border-red-800 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-red-400 mb-2">Booking Failed</h3>
            <p className="text-red-300 mb-4">{bookingError}</p>
            <button
              onClick={handleBackToSeats}
              className="px-6 py-2 bg-red-700 hover:bg-red-600 text-white rounded-lg"
            >
              Back to Seats
            </button>
          </div>
        ) : (
          <div className="bg-gray-800 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">Confirm Your Booking</h3>
            
            <div className="space-y-4 mb-6">
              <div className="flex justify-between">
                <span className="text-gray-400">Movie:</span>
                <span className="text-white">{selectedMovie.movieName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Theater:</span>
                <span className="text-white">{selectedShow.theatre?.theatreName || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Show Time:</span>
                <span className="text-white">
                  {selectedShow.startTime ? formatShowTime(selectedShow.startTime) : 'N/A'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Seats:</span>
                <span className="text-white">
                  {selectedSeats.map(seat => seat.seatNumber).join(', ')}
                </span>
              </div>
              
              {/* Payment Method Selection */}
              <div className="mt-4">
                <label className="block text-gray-400 text-sm mb-2">Payment Method</label>
                <div className="flex gap-4">
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      className="form-radio text-blue-600"
                      checked={paymentMethod === 'UPI'}
                      onChange={() => setPaymentMethod('UPI')}
                    />
                    <span className="ml-2 text-white">UPI</span>
                  </label>
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      className="form-radio text-blue-600"
                      checked={paymentMethod === 'ETH'}
                      onChange={() => setPaymentMethod('ETH')}
                    />
                    <span className="ml-2 text-white">Ethereum (ETH)</span>
                  </label>
                </div>
              </div>
              
              {/* Phone Number Input */}
              <div className="mt-4">
                <label htmlFor="phoneNumber" className="block text-gray-400 text-sm mb-2">
                  Phone Number (for booking confirmation)
                </label>
                <input
                  type="tel"
                  id="phoneNumber"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  className={`w-full px-3 py-2 bg-gray-700 border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500 ${phoneError ? 'border-red-500' : 'border-gray-600'}`}
                  placeholder="Enter 10-digit phone number"
                />
                {phoneError && (
                  <p className="mt-1 text-sm text-red-400">{phoneError}</p>
                )}
              </div>
              
              <div className="border-t border-gray-700 my-4"></div>
              <div className="flex justify-between text-lg font-semibold">
                <span>Total Amount:</span>
                <span>₹{totalAmount.toFixed(2)}</span>
              </div>
            </div>
            
            <div className="flex justify-end gap-4">
              <button
                onClick={handleBackToSeats}
                className="px-6 py-2 border border-gray-600 rounded-lg text-white hover:bg-gray-700"
              >
                Back to Seats
              </button>
              <button
                onClick={handleConfirmBooking}
                className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg disabled:opacity-50"
                disabled={isBooking || !phoneNumber}
              >
                {isBooking ? 'Processing...' : 'Confirm Booking'}
              </button>
            </div>
          </div>
        )}
      </div>
    );
  }

  // Default loading state
  if (isLoadingMovies || isLoadingShows) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  // Movies view (default)
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Now Showing in {city}</h2>
        <button
          onClick={onCityChange}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
        >
          <MapPin size={18} />
          Change City
        </button>
      </div>
      
      {error && (
        <div className="bg-red-600 text-white p-4 rounded-lg mb-6">
          {error}
          <button onClick={() => setError('')} className="float-right font-bold">×</button>
        </div>
      )}
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {movies.length > 0 ? (
          movies.map((movie) => (
            <div 
              key={movie.movieId} 
              className="bg-gray-800 rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => handleSelectMovie(movie)}
            >
              <div className="h-48 bg-gray-700 flex items-center justify-center">
                <Film size={48} className="text-gray-500" />
              </div>
              <div className="p-4">
                <h3 className="text-xl font-semibold text-white mb-2">{movie.movieName}</h3>
                <div className="flex justify-between items-center text-gray-400 text-sm">
                  <span>{movie.genre || 'N/A'}</span>
                  <span>{Math.floor(movie.duration / 60)}h {movie.duration % 60}m</span>
                </div>
                <button className="mt-4 w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg flex items-center justify-center gap-2">
                  Book Now
                  <ChevronRight size={18} />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="col-span-full text-center text-gray-400 py-8">
            No movies found in {city}. Try another city.
          </div>
        )}
      </div>
    </div>
  );
};

export default GuestDashboard;
