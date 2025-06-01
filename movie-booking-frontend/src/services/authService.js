import api from './api';

const authService = {
  // Admin login
  login: async (username, password) => {
    try {
      const response = await api.post('/admin/login', { username, password });
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = document.cookie
      .split('; ')
      .find(row => row.startsWith('token='))
      ?.split('=')[1];
    return !!token;
  },

  // Logout
  logout: () => {
    document.cookie = 'token=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    window.location.href = '/login';
  },
};

export default authService;
