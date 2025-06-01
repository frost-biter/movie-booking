import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Spinner, Form, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useSearchParams } from 'react-router-dom';
import movieService from '../../services/movieService';

const MovieListPage = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchTerm, setSearchTerm] = useState(searchParams.get('q') || '');
  const [filters, setFilters] = useState({
    genre: searchParams.get('genre') || '',
    language: searchParams.get('language') || '',
  });

  // Available filters (you can fetch these from the API in a real app)
  const genres = ['Action', 'Comedy', 'Drama', 'Thriller', 'Horror', 'Sci-Fi', 'Romance'];
  const languages = ['English', 'Hindi', 'Tamil', 'Telugu', 'Malayalam', 'Kannada'];

  useEffect(() => {
    const fetchMovies = async () => {
      try {
        setLoading(true);
        // In a real app, you would pass the filters to the API
        const data = await movieService.getMoviesByCity(1); // Default to cityId 1 for now
        
        // Apply filters locally for demo purposes
        let filteredMovies = [...data];
        
        if (searchTerm) {
          filteredMovies = filteredMovies.filter(movie => 
            movie.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            movie.description?.toLowerCase().includes(searchTerm.toLowerCase())
          );
        }
        
        if (filters.genre) {
          filteredMovies = filteredMovies.filter(movie => 
            movie.genre?.toLowerCase().includes(filters.genre.toLowerCase())
          );
        }
        
        if (filters.language) {
          filteredMovies = filteredMovies.filter(movie => 
            movie.language?.toLowerCase() === filters.language.toLowerCase()
          );
        }
        
        setMovies(filteredMovies);
      } catch (err) {
        console.error('Error fetching movies:', err);
        setError('Failed to load movies. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, [searchTerm, filters]);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (searchTerm) params.set('q', searchTerm);
    if (filters.genre) params.set('genre', filters.genre);
    if (filters.language) params.set('language', filters.language);
    setSearchParams(params);
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const clearFilters = () => {
    setSearchTerm('');
    setFilters({ genre: '', language: '' });
    setSearchParams({});
  };

  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-2">Loading movies...</p>
      </Container>
    );
  }

  return (
    <Container className="my-5">
      <h1 className="mb-4">Now Showing</h1>
      
      {/* Search and Filter Section */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3">
              <Col md={4}>
                <Form.Control
                  type="text"
                  placeholder="Search movies..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </Col>
              <Col md={3}>
                <Form.Select 
                  name="genre" 
                  value={filters.genre}
                  onChange={handleFilterChange}
                >
                  <option value="">All Genres</option>
                  {genres.map(genre => (
                    <option key={genre} value={genre}>{genre}</option>
                  ))}
                </Form.Select>
              </Col>
              <Col md={3}>
                <Form.Select 
                  name="language" 
                  value={filters.language}
                  onChange={handleFilterChange}
                >
                  <option value="">All Languages</option>
                  {languages.map(lang => (
                    <option key={lang} value={lang}>{lang}</option>
                  ))}
                </Form.Select>
              </Col>
              <Col md={2}>
                <Button variant="primary" type="submit" className="w-100">
                  Apply
                </Button>
              </Col>
              {(searchTerm || filters.genre || filters.language) && (
                <Col xs={12} className="mt-2">
                  <Button 
                    variant="link" 
                    onClick={clearFilters}
                    className="p-0"
                  >
                    Clear all filters
                  </Button>
                </Col>
              )}
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {/* Movies Grid */}
      {error ? (
        <div className="alert alert-danger">{error}</div>
      ) : movies.length === 0 ? (
        <div className="text-center my-5">
          <h3>No movies found</h3>
          <p>Try adjusting your search or filters</p>
          <Button variant="outline-primary" onClick={clearFilters}>
            Clear all filters
          </Button>
        </div>
      ) : (
        <Row xs={1} md={2} lg={3} xl={4} className="g-4">
          {movies.map((movie) => (
            <Col key={movie.id}>
              <Card className="h-100 shadow-sm">
                <div style={{ height: '400px', overflow: 'hidden' }}>
                  <Card.Img 
                    variant="top" 
                    src={movie.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster'} 
                    alt={movie.title}
                    style={{ 
                      height: '100%', 
                      width: '100%', 
                      objectFit: 'cover',
                      transition: 'transform 0.3s ease-in-out'
                    }}
                    className="hover-zoom"
                  />
                </div>
                <Card.Body className="d-flex flex-column">
                  <Card.Title className="text-truncate">{movie.title}</Card.Title>
                  <div className="mb-2">
                    <span className="badge bg-primary me-1">{movie.language}</span>
                    <span className="badge bg-secondary me-1">{movie.genre}</span>
                    <span className="badge bg-info">{movie.duration} min</span>
                  </div>
                  <Card.Text className="text-muted small flex-grow-1">
                    {movie.description?.substring(0, 100)}...
                  </Card.Text>
                  <div className="d-flex justify-content-between align-items-center mt-3">
                    <div className="d-flex align-items-center">
                      <i className="bi bi-star-fill text-warning me-1"></i>
                      <span>{movie.rating || 'N/A'}</span>
                    </div>
                    <Button 
                      as={Link} 
                      to={`/movies/${movie.id}`} 
                      variant="primary" 
                      size="sm"
                    >
                      Book Now
                    </Button>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </Container>
  );
};

export default MovieListPage;
