import api from './api';

console.log('API instance in showService:', api);

/**
 * Service for handling all show-related API calls
 */
const showService = {
  /**
   * Get all shows for a specific movie
   * @param {string|number} movieId - The ID of the movie
   * @returns {Promise<Array>} List of shows for the movie
   */
  getShowsByMovie: async (movieId) => {
    try {
      // The API interceptor already returns response.data
      // Remove 'movie-id-' prefix if it exists in the movieId
      const cleanMovieId = movieId.startsWith('movie-id-') ? movieId.substring(9) : movieId;
      return await api.get(`/shows/movie-id-${cleanMovieId}`);
    } catch (error) {
      console.error('Error in getShowsByMovie:', error);
      throw error;
    }
  },

  /**
   * Get seats for a specific show
   * @param {string|number} showId - The ID of the show
   * @returns {Promise<Array>} List of seats with their availability
   */
  getSeatsForShow: async (showId) => {
    try {
      // The API interceptor already returns response.data
      // Remove 'show-id-' prefix if it exists in the showId
      const cleanShowId = showId.startsWith('show-id-') ? showId.substring(8) : showId;
      return await api.get(`/shows/show-id-${cleanShowId}`);
    } catch (error) {
      console.error('Error in getSeatsForShow:', error);
      throw error;
    }
  },



  // Placeholder for admin endpoints
  // addShow: async (showData) => {
  //   try {
  //     const response = await api.post('/admin/shows', showData);
  //     return response.data;
  //   } catch (error) {
  //     throw error.response?.data || error.message;
  //   }
  // },

  // updateShow: async (showId, showData) => {
  //   try {
  //     const response = await api.put(`/admin/shows/${showId}`, showData);
  //     return response.data;
  //   } catch (error) {
  //     throw error.response?.data || error.message;
  //   }
  // },

  // deleteShow: async (showId) => {
  //   try {
  //     const response = await api.delete(`/admin/shows/${showId}`);
  //     return response.data;
  //   } catch (error) {
  //     throw error.response?.data || error.message;
  //   }
  // },

  // getShowsByTheatre: async (theatreId) => {
  //   try {
  //     const response = await api.get(`/theatres/${theatreId}/shows`);
  //     return response.data;
  //   } catch (error) {
  //     throw error.response?.data || error.message;
  //   }
  // },
};

export default showService;
