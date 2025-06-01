import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  // Check if user is authenticated via session cookie
  const checkAuth = async () => {
    try {
      const response = await authAPI.getCurrentUser();
      if (response && response.data) {
        const userData = response.data;
        const user = {
          ...userData,
          isAuthenticated: true,
          isAdmin: userData.role === 'admin'
        };
        setUser(user);
        return user;
      }
      return null;
    } catch (error) {
      console.error('Auth check failed:', error);
      return null;
    } finally {
      setLoading(false);
    }
  };

  // Initialize auth state
  useEffect(() => {
    const initAuth = async () => {
      // Only check auth for admin routes
      if (window.location.pathname.startsWith('/admin')) {
        await checkAuth();
      } else {
        setLoading(false);
      }
    };
    initAuth();
  }, []);

  const login = async (credentials) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authAPI.login(credentials);
      const userData = response.data.user;
      
      const user = {
        ...userData,
        isAuthenticated: true,
        isAdmin: userData.role === 'admin'
      };
      
      setUser(user);
      
      // Redirect based on role
      const redirectPath = location.state?.from?.pathname || (user.isAdmin ? '/admin/dashboard' : '/')
      navigate(redirectPath);
      
      return { success: true };
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Login failed. Please check your credentials.';
      setError(errorMessage);
      return { success: false, message: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authAPI.register(userData);
      const userData = response.data.user;
      
      const user = {
        ...userData,
        isAuthenticated: true,
        isAdmin: userData.role === 'admin'
      };
      
      // No need to store token in localStorage with cookie-based auth
      
      setUser(user);
      navigate(user.isAdmin ? '/admin/dashboard' : '/');
      
      return { success: true };
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Registration failed. Please try again.';
      setError(errorMessage);
      return { success: false, message: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      await authAPI.logout();
    } catch (err) {
      console.error('Logout failed:', err);
    } finally {
      // Clear user data
      setUser(null);
      
      // Redirect to home page
      navigate('/');
      
      // Refresh to clear any cached data
      window.location.reload();
    }
  };

  const value = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user?.isAuthenticated,
    isAdmin: user?.isAdmin || false,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
