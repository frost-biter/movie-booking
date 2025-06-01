import api from './api';

const movieService = {
  /**
   * Get all movies for the current city (city is determined from the cookie)
   * @returns {Promise<Array>} List of movies
   */
  getMoviesByCity: async () => {
    try {
      const response = await api.get('/movies/list');
      // The backend returns the array of movies directly, not wrapped in a 'movies' property
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      console.error('Error fetching movies:', error);
      const errorMessage = error.response?.data || 'Failed to fetch movies';
      console.error('Error details:', errorMessage);
      throw typeof errorMessage === 'string' ? errorMessage : 'Failed to fetch movies';
    }
  },

  /**
   * Add a new movie (Admin only)
   * @param {Object} movieData - Movie details
   * @returns {Promise<Object>} Created movie data
   */
  addMovie: async (movieData) => {
    try {
      // Ensure duration is a number
      const payload = {
        ...movieData,
        duration: parseInt(movieData.duration, 10)
      };
      
      const response = await api.post('/admin/add-movie', payload);
      return response.data;
    } catch (error) {
      console.error('Error adding movie:', error);
      throw error.response?.data?.message || 'Failed to add movie';
    }
  }
};

export default movieService;
