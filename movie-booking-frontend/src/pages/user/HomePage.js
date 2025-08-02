import React, { useState, useEffect, useCallback } from 'react';
import { 
  Container, Row, Col, Card, Button, Spinner, Alert, 
  Badge, Form, InputGroup, Dropdown, Modal, Carousel 
} from 'react-bootstrap';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api, { moviesAPI, cityAPI } from '../../services/api';
import { 
  FaStar, FaSearch, FaMapMarkerAlt, 
  FaTicketAlt, FaCalendarAlt, FaInfoCircle, FaArrowRight 
} from 'react-icons/fa';
import { format } from 'date-fns';
import './HomePage.css';

const HomePage = ({ city, onCityChange }) => {
  const [movies, setMovies] = useState([]);
  const [filteredMovies, setFilteredMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('All');
  const [selectedGenre, setSelectedGenre] = useState('All');
  const [showFilters, setShowFilters] = useState(false);
  const [showMovieDetails, setShowMovieDetails] = useState(null);
  const [trendingMovies, setTrendingMovies] = useState([]);
  const [upcomingMovies, setUpcomingMovies] = useState([]);
  const navigate = useNavigate();
  
  const { isAuthenticated, user } = useAuth();

  // Fetch all movies
  const fetchMovies = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Set city first
      if (city) {
        console.log('Setting city:', city);
        await cityAPI.setCity(city);
      }
      
      // Fetch all movies
      console.log('Fetching all movies...');
      const allMovies = await moviesAPI.getAllMovies();
      
      // Process movies to match expected format
      const processedMovies = Array.isArray(allMovies) ? allMovies.map(movie => ({
        movieId: movie.movieId,
        movieName: movie.movieName,
        genre: movie.genre || 'Drama',
        language: movie.language || 'English',
        duration: movie.duration || 120, // Default to 120 minutes if not provided
        rating: movie.rating || 5.5,
        posterUrl: movie.posterUrl || 'https://w0.peakpx.com/wallpaper/488/147/HD-wallpaper-uwu-otakus-pink-anime-cute-anime-girl-e-girl-waifu.jpg',
        releaseDate: movie.releaseDate || new Date().toISOString()
      })) : [];
      
      setMovies(processedMovies);
      setFilteredMovies(processedMovies);
      
      // For now, use the same movies for trending and upcoming
      // In a real app, you would have separate API calls for these
      const trending = [...processedMovies]
        .sort((a, b) => (b.rating || 0) - (a.rating || 0))
        .slice(0, 5);
      
      const upcoming = [...processedMovies]
        .sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate))
        .slice(0, 5);
      
      setTrendingMovies(trending);
      setUpcomingMovies(upcoming);
      
    } catch (err) {
      console.error('Error fetching movies:', err);
      setError('Failed to load movies. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [city]);

  // Filter movies based on search and filters
  useEffect(() => {
    let result = [...movies];
    
    // Apply search term filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      result = result.filter(movie => 
        movie.title.toLowerCase().includes(searchLower) ||
        (movie.genre && movie.genre.toLowerCase().includes(searchLower)) ||
        (movie.language && movie.language.toLowerCase().includes(searchLower))
      );
    }
    
    // Apply language filter
    if (selectedLanguage !== 'All') {
      result = result.filter(movie => 
        movie.language === selectedLanguage
      );
    }
    
    // Apply genre filter
    if (selectedGenre !== 'All') {
      result = result.filter(movie => 
        movie.genre && movie.genre.split(',').map(g => g.trim()).includes(selectedGenre)
      );
    }
    
    setFilteredMovies(result);
  }, [searchTerm, selectedLanguage, selectedGenre, movies]);

  // Fetch movies when city changes
  useEffect(() => {
    fetchMovies();
  }, [fetchMovies, city]);

  // Handle movie selection
  const handleMovieSelect = (movie) => {
    setShowMovieDetails(movie);
  };

  // Handle quick book - navigate directly to shows page
  const handleQuickBook = (movie) => {
    if (!movie || !movie.movieId) {
      console.error('Invalid movie data:', movie);
      return;
    }
    
    // Navigate directly to the shows page for this movie
    console.log("Naviagating to movie page for movieId:", movie.movieId);

    navigate(`/shows/movie/${movie.movieId}`);
  };

  // Get unique languages and genres for filters
  const languages = ['All', ...new Set(movies.flatMap(movie => movie.language ? [movie.language] : []))];
  const genres = ['All', ...new Set(movies.flatMap(movie => 
    movie.genre ? movie.genre.split(',').map(g => g.trim()) : []
  ))];

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">{error}</Alert>
      </Container>
    );
  }

  return (
    <div className="home-page">
      {/* Hero Section */}
      <div className="hero-section text-white py-5 mb-5 bg-primary bg-gradient">
        <Container>
          <div className="text-center py-5">
            <h1 className="display-4 fw-bold mb-4">Book Your Movie Experience in {city}</h1>
            <p className="lead mb-4">Discover and book tickets for the latest movies in your city</p>
            <div className="d-flex justify-content-center gap-3">
              <Button as={Link} to="/movies" variant="light" size="lg" className="px-4">
                <FaTicketAlt className="me-2" /> Book Now
              </Button>
              {!isAuthenticated && (
                <Button as={Link} to="/admin/login" variant="outline-light" size="lg">
                  Admin Login
                </Button>
              )}
            </div>
          </div>
        </Container>
      </div>

      <Container>
        {/* Search and Filters */}
        <div className="search-filters mb-4">
          <Row className="align-items-center">
            <Col md={6} className="mb-3 mb-md-0">
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  type="text"
                  placeholder="Search movies by name, genre or language..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={6} className="d-flex justify-content-md-end">
              <Dropdown className="me-2">
                <Dropdown.Toggle variant="outline-secondary" id="language-filter">
                  {selectedLanguage === 'All' ? 'All Languages' : selectedLanguage}
                </Dropdown.Toggle>
                <Dropdown.Menu>
                  {languages.map((lang) => (
                    <Dropdown.Item 
                      key={lang} 
                      active={selectedLanguage === lang}
                      onClick={() => setSelectedLanguage(lang)}
                    >
                      {lang}
                    </Dropdown.Item>
                  ))}
                </Dropdown.Menu>
              </Dropdown>
              
              <Dropdown>
                <Dropdown.Toggle variant="outline-secondary" id="genre-filter">
                  {selectedGenre === 'All' ? 'All Genres' : selectedGenre}
                </Dropdown.Toggle>
                <Dropdown.Menu>
                  {genres.map((genre) => (
                    <Dropdown.Item 
                      key={genre} 
                      active={selectedGenre === genre}
                      onClick={() => setSelectedGenre(genre)}
                    >
                      {genre}
                    </Dropdown.Item>
                  ))}
                </Dropdown.Menu>
              </Dropdown>
            </Col>
          </Row>
        </div>

        {/* Trending Movies Carousel */}
        {trendingMovies.length > 0 && (
          <section className="mb-5">
            <h2 className="section-title mb-4">Trending Now</h2>
            <Carousel fade className="hero-carousel">
              {trendingMovies.map((movie) => (
                <Carousel.Item key={movie.movieId} className="hero-slide">
                  <div 
                    className="hero-background"
                    style={{
                      backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)), url(${movie.backdropUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'})`
                    }}
                  >
                    <Container>ś
                      <Row className="align-items-center">
                        <Col md={6}>
                          <h2 className="display-5 fw-bold mb-3">{movie.movieName}</h2>
                          <div className="d-flex align-items-center mb-3">
                            <Badge bg="warning" className="me-2">{movie.language}</Badge>
                            <Badge bg="info" className="me-2">{movie.genre?.split(',')[0]}</Badge>
                            <Badge bg="light" text="dark" className="me-2">
                              <FaStar className="text-warning me-1" /> {movie.rating || 'N/A'}

                            </Badge>
                          </div>
                          <p className="lead mb-4">{movie.movieName?.substring(0, 150)}...</p>
                          <div className="d-flex gap-3">
                            <Button 
                              variant="primary" 
                              size="lg" 
                              onClick={() => handleQuickBook(movie)}
                            >
                              <FaTicketAlt className="me-2" /> Book Now
                            </Button>
                            <Button 
                              variant="outline-light" 
                              size="lg"
                              onClick={() => handleMovieSelect(movie)}
                            >
                              <FaInfoCircle className="me-2" /> More Info
                            </Button>
                          </div>
                        </Col>
                        <Col md={6} className="d-none d-md-block">
                          <img 
                            src={movie.posterUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'}
                            alt={movie.movieName}
                            className="img-fluid rounded shadow"
                            style={{ maxHeight: '400px' }}
                          />
                        </Col>
                      </Row>
                    </Container>
                  </div>
                </Carousel.Item>
              ))}
            </Carousel>
          </section>
        )}

        {/* Now Showing Section */}
        <section className="mb-5">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="section-title">Now Showing</h2>
            <Button 
              variant="link" 
              onClick={() => navigate(`/movies/now-showing`)}
              className="text-decoration-none"
            >
              View All <FaArrowRight className="ms-1" />
            </Button>
          </div>
          
          {filteredMovies.length > 0 ? (
            <Row xs={2} md={3} lg={4} xl={5} className="g-4">
              {filteredMovies.slice(0, 10).map((movie) => (
                <Col key={movie.movieId}>
                  <Card className="h-100 movie-card">
                    <div className="movie-poster-container">
                      <Card.Img 
                        variant="top" 
                        src={movie.posterUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'}
                        alt={movie.movieName}
                        className="movie-poster"
                      />
                      <div className="movie-overlay">
                        <div className="d-flex flex-column h-100 justify-content-between p-3">
                          <div>
                            <Badge bg="warning" className="mb-2">{movie.language}</Badge>
                            <Badge bg="info" className="ms-1">{movie.genre?.split(',')[0]}</Badge>
                          </div>
                          <div>
                            <Button 
                              variant="primary" 
                              size="sm" 
                              className="w-100 mb-2"
                              onClick={() => handleQuickBook(movie)}
                            >
                              <FaTicketAlt className="me-1" /> Book Now
                            </Button>
                            <Button 
                              variant="outline-light" 
                              size="sm" 
                              className="w-100"
                              onClick={() => handleMovieSelect(movie)}
                            >
                              <FaInfoCircle className="me-1" /> View Details
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                    <Card.Body className="p-3">
                      <Card.Title className="movie-title text-truncate">{movie.movieName}</Card.Title>
                      <div className="d-flex justify-content-between align-items-center">
                        <div className="d-flex align-items-center">
                          <FaStar className="text-warning me-1" />
                          <span>{movie.rating || 'N/A'}</span>
                        </div>
                        <Badge bg="secondary">{movie.duration || 'N/A'} min</Badge>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          ) : (
            <Alert variant="info">
              No movies found matching your criteria. Try adjusting your filters.
            </Alert>
          )}
        </section>

        {/* Upcoming Movies Section */}
        {upcomingMovies.length > 0 && (
          <section className="mb-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h2 className="section-title">Coming Soon</h2>
              <Button 
                variant="link" 
                onClick={() => navigate('/movies/upcoming')}
                className="text-decoration-none"
              >
                View All <FaArrowRight className="ms-1" />
              </Button>
            </div>
            <Row xs={2} md={3} lg={4} xl={5} className="g-4">
              {upcomingMovies.slice(0, 5).map((movie) => (
                <Col key={movie.id}>
                  <Card className="h-100">
                    <div className="position-relative">
                      <Card.Img 
                        variant="top" 
                        src={movie.posterUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'}
                        alt={movie.movieName}
                        className="upcoming-movie-poster"
                      />
                      <div className="upcoming-badge">Coming Soon</div>
                    </div>
                    <Card.Body>
                      <Card.Title className="movie-title text-truncate">{movie.movieName}</Card.Title>
                      <div className="text-muted small">
                        <div className="mb-1">
                          <FaCalendarAlt className="me-1" /> 
                          {movie.releaseDate ? format(new Date(movie.releaseDate), 'MMM d, yyyy') : 'Coming Soon'}
                        </div>
                        <div className="d-flex justify-content-between">
                          <span>{movie.genre?.split(',')[0]}</span>
                          <span>{movie.language}</span>
                        </div>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </section>
        )}
      </Container>

      {/* Movie Details Modal */}
      <Modal 
        show={!!showMovieDetails} 
        onHide={() => setShowMovieDetails(null)}
        size="lg"
        centered
      >
        {showMovieDetails && (
          <>
            <Modal.Header closeButton>
              <Modal.Title>{showMovieDetails.movieName}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Row>
                <Col md={4}>
                  <img 
                    src={showMovieDetails.posterUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'}
                    alt={showMovieDetails.movieName}
                    className="img-fluid rounded"
                  />
                </Col>
                <Col md={8}>
                  <div className="mb-3">
                    <div className="d-flex flex-wrap gap-2 mb-2">
                      <Badge bg="primary">{showMovieDetails.language}</Badge>
                      <Badge bg="success">{showMovieDetails.genre}</Badge>
                      <Badge bg="warning">{showMovieDetails.duration} min</Badge>
                      <Badge bg="info">IMDb: {showMovieDetails.rating || 'N/A'}</Badge>
                    </div>
                    <p className="text-muted">
                      <FaMapMarkerAlt className="me-1" /> 
                      {showMovieDetails.theatres?.join(', ') || 'Not showing in any theatres'}
                    </p>
                  </div>
                  <h5>Synopsis</h5>
                  <p>{showMovieDetails.description || 'No description available.'}</p>
                  <div className="mt-4">
                    <h6>Show Times</h6>
                    <div className="d-flex flex-wrap gap-2">
                      {showMovieDetails.showTimes?.length > 0 ? (
                        showMovieDetails.showTimes.map((time, index) => (
                          <Button 
                            key={index} 
                            variant="outline-primary" 
                            size="sm"
                            onClick={() => handleQuickBook(showMovieDetails, time)}
                          >
                            {format(new Date(time), 'h:mm a')}
                          </Button>
                        ))
                      ) : (
                        <p className="text-muted">No showtimes available</p>
                      )}
                    </div>
                  </div>
                </Col>
              </Row>
            </Modal.Body>
            <Modal.Footer>
              <Button 
                variant="secondary" 
                onClick={() => setShowMovieDetails(null)}
              >
                Close
              </Button>
              <Button 
                variant="primary"
                onClick={() => handleQuickBook(showMovieDetails)}
              >
                <FaTicketAlt className="me-1" /> Book Now
              </Button>
            </Modal.Footer>
          </>
        )}
      </Modal>
    </div>
  );
};

export default HomePage;
