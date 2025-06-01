import api from './api';

const bookingService = {
  // Initiate a new booking
  initiateBooking: async (bookingData) => {
    try {
      const response = await api.post('/booking/seats', bookingData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Get booking details by ID
  getBooking: async (holdId) => {
    try {
      const response = await api.get(`/booking/bookings?holdId=${holdId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },
};

export default bookingService;
