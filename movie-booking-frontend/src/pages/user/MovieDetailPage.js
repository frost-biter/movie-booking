import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Spinner, Alert, Button, Tabs, Tab, Badge } from 'react-bootstrap';
import { FaStar, FaClock, FaCalendarAlt, FaLanguage, FaFilm } from 'react-icons/fa';
import { BsCalendarDate } from 'react-icons/bs';
import movieService from '../../services/movieService';
import showService from '../../services/showService';
import { moviesAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const MovieDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('details');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]); // Today's date

  // Get dates for the next 7 days
  const dates = Array.from({ length: 7 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() + i);
    return date.toISOString().split('T')[0];
  });

  useEffect(() => {
    const fetchMovieDetails = async () => {
      try {
        setLoading(true);
        
        // Fetch all movies and find the one with matching ID
        const allMovies = await moviesAPI.getAllMovies();
        const movieData = Array.isArray(allMovies) 
          ? allMovies.find(m => m.movieId == id) || {}
          : {};
          
        setMovie({
          ...movieData,
          // Ensure all required fields have default values
          movieName: movieData.movieName || 'Unknown Movie',
          genre: movieData.genre || 'Drama',
          language: movieData.language || 'English',
          duration: movieData.duration || 120,
          rating: movieData.rating || 0,
          releaseDate: movieData.releaseDate || new Date().toISOString(),
          description: movieData.description || 'No description available.'
        });
        
        // Fetch shows for this movie using showService
        const showsData = await showService.getShowsByMovie(id);
        console.log('Raw shows data:', showsData);
        
        // Process shows data to match expected format
        const processedShows = showsData.flatMap(theatre => {
          // If the item has a shows array, use those shows, otherwise treat the item as a show
          const shows = Array.isArray(theatre.shows) ? theatre.shows : [theatre];
          return shows.map(show => ({
            ...show,
            theatreId: show.theatreId || theatre.theatreId,
            theatreName: show.theatreName || theatre.theatreName || 'Unknown Theatre',
            theatre: show.theatre || {
              theatreId: theatre.theatreId,
              theatreName: theatre.theatreName || 'Unknown Theatre',
              address: theatre.address || '',
              city: theatre.city || 'Unknown City'
            }
          }));
        });
        
        // Further process the shows to format dates
        const processedShowsWithDates = processedShows.map(show => {
          // Handle different response formats for show time
          const showTime = show.startTime
            ? Array.isArray(show.startTime) && show.startTime.length >= 5
              ? new Date(
                  show.startTime[0],
                  show.startTime[1] - 1, // months are 0-indexed in JS
                  show.startTime[2],
                  show.startTime[3] || 0,
                  show.startTime[4] || 0
                )
              : new Date(show.startTime)
            : new Date(); // Default to current time if no start time
            
          // Create timings array in format ['14:30', '18:00', '21:30']
          const timings = [];
          if (showTime) {
            const hours = String(showTime.getHours()).padStart(2, '0');
            const minutes = String(showTime.getMinutes()).padStart(2, '0');
            timings.push(`${hours}:${minutes}`);
          }
          
          return {
            id: show.showId || show.id,
            theatreId: show.theatreId,
            theatreName: show.theatre?.theatreName || show.theatreName || 'Unknown Theatre',
            screenName: show.screenName || 'Screen 1',
            format: show.format || '2D',
            language: show.language || movie.language || 'English',
            price: typeof show.price === 'number' ? show.price : 200,
            timings: timings,
            showTime: showTime,
            theatre: {
              theatreId: show.theatre?.theatreId || show.theatreId,
              theatreName: show.theatre?.theatreName || show.theatreName || 'Unknown Theatre',
              address: show.theatre?.address || show.address || '',
              city: show.theatre?.city || show.city || 'Unknown City'
            }
          };
        });
        
        console.log('Processed shows with dates:', processedShowsWithDates);
        setShows(processedShowsWithDates);
        
      } catch (err) {
        console.error('Error fetching movie details:', err);
        setError('Failed to load movie details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchMovieDetails();
    }
  }, [id]);

  const handleBookNow = (movieId) => {
    if (!isAuthenticated) {
      // Redirect to login with a return URL
      navigate('/login', { state: { from: `/shows/movie/${movieId}` } });
      return;
    }
    navigate(`/shows/movie/${movieId}`);
  };

  const formatTime = (timeString) => {
    const [hours, minutes] = timeString.split(':');
    const date = new Date();
    date.setHours(hours);
    date.setMinutes(minutes);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  if (loading) {
    return (
      <Container className="text-center my-5">
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

  if (!movie) {
    return (
      <Container className="my-5">
        <Alert variant="warning">Movie not found</Alert>
      </Container>
    );
  }

  return (
    <Container className="my-5">
      <Row className="mb-4">
        <Col md={4} lg={3} className="mb-4 mb-md-0">
          <Card className="h-100">
            <Card.Img 
              variant="top" 
              src={movie.posterUrl || 'https://w0.peakpx.com/wallpaper/672/696/HD-wallpaper-ichigo-anime-darling-in-the-franxx-waifu.jpg'}
              alt={movie.title}
              className="img-fluid"
            />
          </Card>
        </Col>

        <Col md={8} lg={9}>
          <h1 className="mb-3">{movie.title} <span className="text-muted">({movie.releaseYear})</span></h1>
          
          <div className="d-flex flex-wrap gap-2 mb-3">
            <Badge bg="primary" className="d-flex align-items-center">
              <FaStar className="me-1" /> {movie.rating || 'N/A'}/10
            </Badge>
            <Badge bg="secondary" className="d-flex align-items-center">
              <FaClock className="me-1" /> {movie.duration} min
            </Badge>
            <Badge bg="info" className="d-flex align-items-center">
              <FaLanguage className="me-1" /> {movie.language}
            </Badge>
            <Badge bg="success" className="d-flex align-items-center">
              <FaFilm className="me-1" /> {movie.genre}
            </Badge>
            <Badge bg="warning" className="d-flex align-items-center">
              <BsCalendarDate className="me-1" /> {movie.releaseDate || 'Coming Soon'}
            </Badge>
          </div>
          
          <p className="lead">{movie.tagline}</p>
          
          <Tabs
            activeKey={activeTab}
            onSelect={(k) => setActiveTab(k)}
            className="mb-4"
          >
            <Tab eventKey="details" title="Details">
              <div className="mt-3">
                <h5>Overview</h5>
                <p>{movie.overview || 'No overview available.'}</p>
                
                <h5>Cast & Crew</h5>
                <p>{movie.cast || 'No cast information available.'}</p>
                
                <h5>Director</h5>
                <p>{movie.director || 'No director information available.'}</p>
              </div>
            </Tab>
            
            <Tab eventKey="showtimes" title="Showtimes">
              <div className="mt-3">
                <h5 className="mb-3">Select Date</h5>
                <div className="d-flex flex-wrap gap-2 mb-4">
                  {dates.map((date) => {
                    const dateObj = new Date(date);
                    const dayName = dateObj.toLocaleDateString('en-US', { weekday: 'short' });
                    const dayNum = dateObj.getDate();
                    const month = dateObj.toLocaleDateString('en-US', { month: 'short' });
                    const isSelected = date === selectedDate;
                    
                    return (
                      <Button
                        key={date}
                        variant={isSelected ? 'primary' : 'outline-secondary'}
                        className="d-flex flex-column align-items-center"
                        style={{ width: '70px' }}
                        onClick={() => setSelectedDate(date)}
                      >
                        <span>{dayName}</span>
                        <span className="fw-bold">{dayNum}</span>
                        <small>{month}</small>
                      </Button>
                    );
                  })}
                </div>
                
                <h5 className="mb-3">Available Shows</h5>
                {shows.length === 0 ? (
                  <Alert variant="info">No shows available for the selected date.</Alert>
                ) : (
                  <div className="row g-3">
                    {shows.map((show) => (
                      <div key={show.id} className="col-md-6 col-lg-4">
                        <Card>
                          <Card.Body>
                            <div className="d-flex justify-content-between align-items-center mb-2">
                              <h6 className="mb-0">{show.theatreName}</h6>
                              <span className="badge bg-secondary">{show.screenName}</span>
                            </div>
                            <div className="d-flex justify-content-between align-items-center mb-3">
                              <span className="text-muted">{show.format} • {show.language}</span>
                              <span className="text-muted">₹{show.price || 200}</span>
                            </div>
                            <div className="d-flex flex-wrap gap-2">
                              {show.timings?.map((time, idx) => (
                                <Button
                                  key={idx}
                                  variant="outline-primary"
                                  size="sm"
                                  onClick={() => handleBookNow(id)}
                                >
                                  {formatTime(time)}
                                </Button>
                              ))}
                            </div>
                          </Card.Body>
                        </Card>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </Tab>
          </Tabs>
        </Col>
      </Row>
      
      {/* Similar Movies Section */}
      <h4 className="mb-4">You May Also Like</h4>
      <div className="d-flex overflow-auto gap-3 pb-3">
        {/* This would be populated with similar movies from the API */}
        <div className="text-center" style={{ minWidth: '150px' }}>
          <Card>
            <Card.Img 
              variant="top" 
              src="https://via.placeholder.com/150x225?text=Movie+1"
              alt="Similar movie"
            />
            <Card.Body className="p-2">
              <Card.Title className="h6 mb-1">Movie Title</Card.Title>
              <small className="text-muted">Action, Thriller</small>
            </Card.Body>
          </Card>
        </div>
        {/* Add more similar movie cards */}
      </div>
    </Container>
  );
};

export default MovieDetailPage;
