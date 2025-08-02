import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Spinner, Alert, Button, Badge } from 'react-bootstrap';
import { FaMapMarkerAlt, FaFilm, FaClock } from 'react-icons/fa';
import showService from '../../services/showService';

const MovieShowsPage = () => {
  const { movieId } = useParams();
  const navigate = useNavigate();
  const [theatres, setTheatres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [movieName, setMovieName] = useState('');

  // Get dates for the next 7 days
  const dates = Array.from({ length: 7 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() + i);
    return date.toISOString().split('T')[0];
  });

  useEffect(() => {
    const fetchShows = async () => {
      try {
        setLoading(true);
        setError('');
        


        console.log('Fetching shows for movie ID:', movieId);
        
        const theatresData = await showService.getShowsByMovie(movieId);
        console.log('Shows Data:', theatresData);
        
        if (!theatresData || theatresData.length === 0) {
          setError('No shows found for this movie.');
          setTheatres([]);
          return;
        }

        // Set movie name from the first theatre (all should have the same movie)
        if (theatresData[0]?.movieName) {
          setMovieName(theatresData[0].movieName);
        } else {
          setMovieName('Movie');
        }

        // Process theatres and their shows
        const processedTheatres = theatresData.map(theatre => {
          // Process shows for this theatre
          const shows = (theatre.shows || []).map(show => ({
            id: show.showId,
            movieId: show.movieId,
            theatreId: show.theatreId,
            screenId: show.screenId,
            startTime: show.startTime ? new Date(show.startTime) : null,
            endTime: show.endTime ? new Date(show.endTime) : null,
            // Add formatted time for display
            formattedTime: show.startTime 
              ? new Date(show.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : 'Time not available',
            screenName: `Screen ${show.screenId || '1'}`
          }));

          return {
            id: theatre.theatreId,
            name: theatre.theatreName || 'Unknown Theatre',
            address: theatre.address || '',
            city: theatre.city || 'Unknown City',
            shows: shows
          };
        });

        // Filter out theatres with no shows
        const theatresWithShows = processedTheatres.filter(theatre => theatre.shows.length > 0);
        
        if (theatresWithShows.length === 0) {
          setError('No shows available for the selected date.');
        }

        setTheatres(theatresWithShows);
      } catch (err) {
        console.error('Error fetching shows:', err);
        setError('Failed to load shows. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (movieId) {
      fetchShows();
    }
  }, [movieId]);

  const handleTimeSelect = (show) => {
    navigate(`/shows/show/${show.id}`);
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

  return (
    <Container className="my-5">
      <h2 className="mb-4">
        {movieName ? `Shows for ${movieName}` : 'Select Show'}
      </h2>
      
      {/* Date Selector */}
      <div className="mb-4">
        <h5>Select Date</h5>
        <div className="d-flex gap-2 overflow-auto py-2">
          {dates.map((date, index) => (
            <Button
              key={date}
              variant={date === selectedDate ? 'primary' : 'outline-primary'}
              onClick={() => setSelectedDate(date)}
              className="flex-shrink-0"
            >
              {index === 0 ? 'Today' : index === 1 ? 'Tomorrow' : new Date(date).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
            </Button>
          ))}
        </div>
      </div>

      {/* Theatres and Shows */}
      <div className="mb-4">
        {theatres.length === 0 ? (
          <Alert variant="info">No shows available for the selected date.</Alert>
        ) : (
          <div className="theatre-list">
            {theatres.map((theatre) => (
              <Card key={theatre.id} className="mb-4">
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-start">
                    <div>
                      <h4 className="mb-1">{theatre.name}</h4>
                      <p className="text-muted mb-2">
                        <FaMapMarkerAlt className="me-1" />
                        {theatre.address}, {theatre.city}
                      </p>
                    </div>
                    <Badge bg="light" text="dark" className="fs-6">
                      {theatre.shows.length} {theatre.shows.length === 1 ? 'show' : 'shows'}
                    </Badge>
                  </div>
                  
                  <div className="mt-3">
                    <h6 className="text-muted mb-2">
                      <FaFilm className="me-2" />
                      Available Shows
                    </h6>
                    <div className="d-flex flex-wrap gap-2">
                      {theatre.shows.map((show) => (
                        <Button
                          key={show.id}
                          variant="outline-primary"
                          className="d-flex align-items-center"
                          onClick={() => handleTimeSelect(show)}
                        >
                          <FaClock className="me-2" />
                          {show.formattedTime}
                          <small className="ms-2 text-muted">{show.screenName}</small>
                        </Button>
                      ))}
                    </div>
                  </div>
                </Card.Body>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Container>
  );
};

export default MovieShowsPage;
