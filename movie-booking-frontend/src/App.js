import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import { ToastContainer } from 'react-toastify';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'react-toastify/dist/ReactToastify.css';

// Layout Components
import Header from './components/layout/Header';
import UserHeader from './components/layout/UserHeader';
import Footer from './components/layout/Footer';

// User Pages
import HomePage from './pages/user/HomePage';
import MovieListPage from './pages/user/MovieListPage';
import MovieDetailPage from './pages/user/MovieDetailPage';
import MovieShowsPage from './pages/user/MovieShowsPage';
import ShowSeatsPage from './pages/user/ShowSeatsPage';
import SeatSelectionPage from './pages/user/SeatSelectionPage';
import CitySelectionPage from './pages/user/CitySelectionPage';

// Admin Pages
import AdminLogin from './pages/admin/AdminLogin';
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminMoviesPage from './pages/admin/AdminMoviesPage';
import MovieFormPage from './pages/admin/MovieFormPage';

// Context
import { AuthProvider, useAuth } from './context/AuthContext';

// Protected Admin Route Component
const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin } = useAuth();
  const location = useLocation();
  
  if (!isAuthenticated) {
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }
  
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
};

// Main App Layout
const AppLayout = () => {
  const { isAuthenticated, isAdmin } = useAuth();
  const [selectedCity, setSelectedCity] = useState(localStorage.getItem('selectedCity') || '');

  const handleCitySelect = (city) => {
    setSelectedCity(city);
    localStorage.setItem('selectedCity', city);
  };

  // Show city selection if no city is selected
  if (!selectedCity && !window.location.pathname.startsWith('/admin')) {
    return <CitySelectionPage onSelectCity={handleCitySelect} />;
  }

  return (
    <div className="d-flex flex-column min-vh-100">
      {isAuthenticated && isAdmin ? <Header /> : <UserHeader city={selectedCity} onCityChange={handleCitySelect} />}
      <ToastContainer position="top-right" autoClose={3000} />
      <main className="flex-grow-1 py-4">
        <Container>
          <Routes>
            {/* User Routes */}
            <Route path="/" element={<HomePage city={selectedCity} />} />
            <Route path="/movies" element={<MovieListPage city={selectedCity} />} />
            <Route path="/movies/:id" element={<MovieDetailPage city={selectedCity} />} />
            <Route path="/shows/movie/:movieId" element={<MovieShowsPage city={selectedCity} />} />
            <Route path="/shows/show/:showId" element={<ShowSeatsPage city={selectedCity} />} />
            <Route path="/book-seats/:showId" element={<SeatSelectionPage />} />

            {/* Admin Routes */}
            <Route path="/admin/login" element={<AdminLogin />} />
            <Route
              path="/admin/*"
              element={
                <AdminRoute>
                  <AdminLayout />
                </AdminRoute>
              }
            >
              <Route index element={<AdminDashboard />} />
              <Route path="movies" element={<AdminMoviesPage />} />
              <Route path="movies/new" element={<MovieFormPage />} />
              <Route path="movies/edit/:id" element={<MovieFormPage />} />
            </Route>

            {/* 404 Route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Container>
      </main>
      <Footer />
    </div>
  );
};

// Main App Component
function App() {
  return (
    <Router>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </Router>
  );
}

export default App;
