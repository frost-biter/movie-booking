import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiCall } from '../../services/api';
import { useNavigate } from 'react-router-dom';

interface BookingStatusProps {
  holdId: string;
}

interface Seat {
  seatId: number;
  row: string;
  seatNo: number;
  category: string;
  seatIdentifier: string;
}

interface BookingStatusResponse {
  showId: number;
  movieName: string;
  theatreName: string;
  showTime: number[]; // [year, month, day, hour, minute]
  seats: Seat[];
  phoneNumber: string;
  bookingTime: number[];
  amount: number; // Total amount to be paid
  status?: 'PENDING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED';
  bookingId?: string;
  message?: string;
}

const BookingStatus: React.FC<BookingStatusProps> = ({ holdId }) => {
  const navigate = useNavigate();
  const [pollingInterval, setPollingInterval] = useState(2000); // Start with 2 seconds
  
  const fetchBookingStatus = async (): Promise<BookingStatusResponse> => {
    try {
      const response = await apiCall<BookingStatusResponse>(`/booking/bookings?holdId=${holdId}`);
      return response;
    } catch (err: any) {
      if (err?.response?.status === 404) {
        // Handle 404 - Booking not found
        throw new Error('Booking not found. Please check your booking ID and try again.');
      }
      console.error('Error fetching booking status:', err);
      throw err instanceof Error ? err : new Error('Failed to fetch booking status');
    }
  };

  const { data, isLoading, error, isError } = useQuery<BookingStatusResponse, Error>({
    queryKey: ['bookingStatus', holdId],
    queryFn: fetchBookingStatus,
    refetchInterval: pollingInterval,
    enabled: !!holdId
  });

  // Handle polling stop when booking reaches final state
  useEffect(() => {
    if (data?.status) {
      const finalStatuses = ['CONFIRMED', 'FAILED', 'CANCELLED'] as const;
      if (finalStatuses.includes(data.status as any)) {
        setPollingInterval(0);
      }
    }
  }, [data?.status]);
  
  // Cast the data to the expected type
  const bookingData = data as BookingStatusResponse;

  const formatShowTime = (timeArray: number[]) => {
    if (!timeArray || timeArray.length < 5) return 'N/A';
    const [year, month, day, hours, minutes] = timeArray;
    const date = new Date(year, month - 1, day, hours, minutes);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const getStatusColor = (status = 'PENDING') => {
    switch (status) {
      case 'CONFIRMED':
        return 'text-green-600';
      case 'FAILED':
        return 'text-red-600';
      case 'PENDING':
        return 'text-yellow-600';
      default:
        return 'text-gray-600';
    }
  };

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4">
        <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <h2 className="text-xl font-semibold mb-2">Processing your booking...</h2>
        <p className="text-gray-600">Please wait while we confirm your booking</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4 text-center">
        <div className="text-red-500 text-5xl mb-4">⚠️</div>
        <h2 className="text-2xl font-bold text-red-600 mb-2">Error Loading Booking Status</h2>
        <p className="text-gray-600 mb-6">
          {error?.message || 'Failed to load booking status. Please try again later.'}
        </p>
        <button
          onClick={() => navigate('/')}
          className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
        >
          Back to Home
        </button>
      </div>
    );
  }

  if (!bookingData) {
    return (
      <div className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">Booking Not Found</h1>
          <p className="mb-6">We couldn't find the booking details. Please check your booking reference and try again.</p>
          <button
            onClick={() => navigate('/')}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  // If we have booking data with seats and other details, consider it CONFIRMED
  // If the API returns data, the booking exists and is confirmed
  // If the API returns 404, it will be caught in the error handler
  const bookingStatus = bookingData?.seats?.length > 0 ? 'CONFIRMED' : 'PENDING';

  return (
    <div className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md">
      <div className="text-center mb-8">
        <h1 className="text-2xl font-bold mb-2">Booking Status</h1>
        <div className={`text-lg font-semibold ${getStatusColor(bookingStatus)}`}>
          Status: {bookingStatus}
        </div>
      </div>

      <div className="mb-6 p-4 bg-gray-50 rounded-md">
        <h3 className="font-semibold text-lg mb-2">Show Details</h3>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-gray-600">Movie</p>
            <p className="font-medium">{bookingData?.movieName || 'N/A'}</p>
          </div>
          <div>
            <p className="text-gray-600">Theatre</p>
            <p className="font-medium">{bookingData?.theatreName || 'N/A'}</p>
          </div>
          <div>
            <p className="text-gray-600">Show Time</p>
            <p className="font-medium">{bookingData?.showTime ? formatShowTime(bookingData.showTime) : 'N/A'}</p>
          </div>
          <div>
            <p className="text-gray-600">Booking Time</p>
            <p className="font-medium">{bookingData?.bookingTime ? formatShowTime(bookingData.bookingTime) : 'N/A'}</p>
          </div>
        </div>
      </div>

      {bookingData?.seats && bookingData.seats.length > 0 ? (
        <div className="mb-6">
          <h3 className="font-semibold text-lg mb-2">Selected Seats</h3>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            {bookingData.seats.map((seat, index) => (
              <div key={`${seat.row}-${seat.seatNo}-${index}`} className="p-3 border rounded-lg bg-gray-50">
                <p className="font-medium">Seat {seat.seatIdentifier}</p>
                <p className="text-sm text-gray-600">Row {seat.row}, Seat {seat.seatNo}</p>
                <p className="text-sm text-gray-600">Class: {seat.category}</p>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="mb-6 p-4 bg-yellow-50 text-yellow-800 rounded-md">
          <p>No seat information available</p>
        </div>
      )}

      <div className="border-t border-gray-200 pt-4 mb-6">
        <div className="flex justify-between items-center">
          <span className="text-lg font-medium">Total Amount:</span>
          <span className="text-xl font-bold">₹{bookingData?.amount?.toFixed(2) || '0.00'}</span>
        </div>
      </div>

      {bookingStatus === 'CONFIRMED' && (
        <div className="p-4 bg-green-50 text-green-800 rounded-md mb-6">
          <p className="font-medium">Booking Confirmed!</p>
          {bookingData.bookingId && <p className="mt-1 text-sm">Booking ID: {bookingData.bookingId}</p>}
        </div>
      )}
      {bookingStatus === 'PENDING' && (
        <div className="p-4 bg-yellow-50 text-yellow-800 rounded-md mb-6">
          <p className="font-medium">Processing Your Booking</p>
          <p className="mt-1 text-sm">Please wait while we confirm your booking. This may take a moment.</p>
        </div>
      )}
      {!bookingData && (
        <div className="p-4 bg-red-50 text-red-800 rounded-md mb-6">
          <p className="font-medium">Booking Not Found</p>
          <p className="mt-1">We couldn't find your booking details. Please check your booking reference.</p>
        </div>
      )}

      <div className="flex flex-col sm:flex-row justify-between gap-4 mt-8">
        <button
          onClick={() => navigate('/')}
          className="px-6 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
        >
          Back to Home
        </button>
        
        {bookingStatus === 'CONFIRMED' && (
          <div className="flex gap-3">
            <button
              onClick={() => navigate(`/booking/status?holdId=${holdId}`)}
              className="px-6 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition-colors"
            >
              Refresh Status
            </button>
            <button
              onClick={() => {
                // TODO: Implement download ticket functionality
                alert('Download ticket functionality will be implemented here');
              }}
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              Download Ticket
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default BookingStatus;
