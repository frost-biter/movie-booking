import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';


// Components
import AdminLogin from './components/auth/AdminLogin';
import AdminRegister from './components/auth/AdminRegister';
import CitySelector from './components/guest/CitySelector';
import Layout from './components/common/Layout';
import AdminDashboard from './components/admin/AdminDashboard';
import GuestDashboard from './components/guest/GuestDashboard';
import BookingStatus from './components/booking/BookingStatus';
import PaymentPage from './components/payment/PaymentPage';

// Types
import { User } from './types';

// Constants
const USER_STORAGE_KEY = 'movieBookingUser';

// Create a client for React Query
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Main App Content Component
const AppContent: React.FC = () => {
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem(USER_STORAGE_KEY);
    return storedUser ? JSON.parse(storedUser) : null;
  });
  const [selectedCity, setSelectedCity] = useState<string>('');
  const [showRegister, setShowRegister] = useState(false);
  const location = useLocation();

  // Check if user is authenticated
  const isAuthenticated = !!user;
  const isAdmin = user?.role === 'admin';
  const isGuest = user?.role === 'guest';

  // Handle login
  const handleLogin = (userData: User) => {
    const userWithRole: User = {
      ...userData,
      role: userData.role || 'admin' // Use provided role or default to 'admin'
    };
    setUser(userWithRole);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userWithRole));
  };

  // Handle logout
  const handleLogout = () => {
    setUser(null);
    setSelectedCity('');
    localStorage.removeItem(USER_STORAGE_KEY);
  };

  // Handle city selection for guests
  const handleCitySet = (city: string) => {
    setSelectedCity(city);
    const guestUser: User = { 
      id: `guest-${Date.now()}`, 
      username: 'guest', 
      role: 'guest' 
    };
    setUser(guestUser);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(guestUser));
  };

  // Redirect to login if not authenticated and not on a public route
  if (!isAuthenticated && !['/login', '/register', '/'].includes(location.pathname)) {
    return <Navigate to="/" replace />;
  }

  // Redirect to dashboard if already authenticated
  if (isAuthenticated && ['/login', '/register', '/'].includes(location.pathname)) {
    const redirectPath = isAdmin ? '/admin/dashboard' : '/guest/dashboard';
    return <Navigate to={redirectPath} replace />;
  }

  // Render the appropriate component based on authentication status and user role
  return (
    <>
      <Routes>
        {/* Public Routes */}
        <Route
          path="/"
          element={
            <CitySelector 
              onCitySet={handleCitySet} 
              initialCity={selectedCity} 
            />
          }
        />
        
        <Route
          path="/login"
          element={
            <>
              <AdminLogin onLogin={handleLogin} />
              {!showRegister && (
                <div className="fixed bottom-4 right-4">
                  <button
                    onClick={() => setShowRegister(true)}
                    className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg"
                  >
                    Register Admin
                  </button>
                </div>
              )}
            </>
          }
        />
        
        <Route
          path="/register"
          element={
            <AdminRegister 
              onBack={() => setShowRegister(false)} 
              onRegisterSuccess={() => setShowRegister(false)}
            />
          }
        />
        
        {/* Protected Admin Routes */}
        <Route
          path="/admin/*"
          element={
            isAdmin ? (
              <Layout 
                title="Admin Dashboard" 
                onLogout={handleLogout}
                showHeader={true}
              >
                <AdminDashboard onLogout={handleLogout} />
              </Layout>
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
        
        {/* Protected Guest Routes */}
        <Route
          path="/guest/*"
          element={
            isGuest ? (
              <Layout 
                title={selectedCity ? `Movies in ${selectedCity}` : 'Select a City'} 
                onLogout={handleLogout}
                showHeader={true}
              >
                <GuestDashboard 
                  city={selectedCity} 
                  onCityChange={() => {
                    handleLogout();
                    setSelectedCity('');
                  }} 
                />
              </Layout>
            ) : (
              <Navigate to="/" replace />
            )
          }
        />

        {/* Public Payment Route */}
        <Route 
          path="/payment" 
          element={
            <Layout title="Complete Payment" showHeader={true}>
              <PaymentPage />
            </Layout>
          } 
        />

        {/* Public Booking Status Route */}
        <Route 
          path="/booking/status" 
          element={
            <Layout title="Booking Status" showHeader={true}>
              {location.search.includes('holdId=') ? (
                <BookingStatus holdId={new URLSearchParams(location.search).get('holdId') || ''} />
              ) : (
                <div className="flex flex-col items-center justify-center min-h-screen">
                  <div className="text-center p-6 bg-white rounded-lg shadow-md">
                    <h2 className="text-xl font-semibold mb-4">No Booking Found</h2>
                    <p className="mb-4">Please check your booking link or return to the home page.</p>
                    <button
                      onClick={() => window.location.href = '/'}
                      className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                    >
                      Back to Home
                    </button>
                  </div>
                </div>
              )}
            </Layout>
          } 
        />

        {/* Catch all route */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      
      <ToastContainer 
        position="top-right"
        autoClose={5000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="dark"
      />
    </>
  );
};

// App Component
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
        <div className="min-h-screen bg-gray-900 text-white">
          <AppContent />
        </div>
      {process.env.NODE_ENV === 'development' && (
        <div style={{ position: 'fixed', bottom: '10px', right: '10px', zIndex: 1000 }}>
          <ReactQueryDevtools initialIsOpen={false} />
        </div>
      )}
    </QueryClientProvider>
  );
}
