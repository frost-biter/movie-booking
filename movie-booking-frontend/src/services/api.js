import axios from 'axios';


const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  withCredentials: true, // This is important for sending/receiving cookies
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Response interceptor to handle errors globally
api.interceptors.response.use(
  (response) => {
    // For successful responses, return the data directly
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      if (window.location.pathname.startsWith('/admin')) {
        window.location.href = '/admin/login';
      }
    }
    // Return error response data if available
    return Promise.reject(error.response?.data || error.message);
  }
);

// Auth API
export const authAPI = {
  login: (username, password) => 
    api.post('/admin/login', { username, password }),
  
  register: (username, password) =>
    api.post('/admin/register', { username, password }),
};

// Movies API
export const moviesAPI = {
  getAllMovies: () => 
    api.get('/movies/list'),
};

// City API
export const cityAPI = {
  setCity: (cityName) => 
//    api.post(`/city/set?cityName=${encodeURIComponent(cityName)}`)
      api.get(`/city/set?cityName=${encodeURIComponent(cityName)}`)
};

// Payment API
export const paymentAPI = {
  getPaymentStatus: (paymentMethod, paymentAddress, requiredAmount) => 
    api.get(
      `/api/payments/status/${paymentMethod.toUpperCase()}/${encodeURIComponent(paymentAddress)}`,
      { params: { requiredAmount } }
    )
};

// Export the axios instance as the default export
export default api;
