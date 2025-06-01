import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Container, 
  Row, 
  Col, 
  Card, 
  Table, 
  Button, 
  Spinner,
  Alert,
  InputGroup,
  Badge,
  Form
} from 'react-bootstrap';
import { 
  FaPlus, 
  FaSearch, 
  FaFilm,
  FaCalendarAlt,
  FaEdit
} from 'react-icons/fa';
import { toast } from 'react-toastify';
import movieService from '../../services/movieService';

const AdminMoviesPage = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch movies from API
  const fetchMovies = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await movieService.getMoviesByCity();
      setMovies(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching movies:', err);
      const errorMessage = err.message || 'Failed to load movies. Please try again later.';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, []);

  // Filter movies based on search term
  const filteredMovies = movies.filter(movie => 
    movie.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (movie.director && movie.director.toLowerCase().includes(searchTerm.toLowerCase())) ||
    (movie.genre && movie.genre.toLowerCase().includes(searchTerm.toLowerCase()))
  );
  
  // Format duration in hours and minutes
  const formatDuration = (minutes) => {
    if (!minutes) return 'N/A';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  };

  if (loading) {
    return (
      <Container className="text-center py-5">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
      </Container>
    );
  }

  return (
    <Container fluid className="py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="mb-0">Manage Movies</h1>
        <Button as={Link} to="/admin/movies/new" variant="primary">
          <FaPlus className="me-2" /> Add Movie
        </Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="mb-4">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h5 className="mb-0">All Movies</h5>
            <div style={{ maxWidth: '300px' }}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  type="text"
                  placeholder="Search by title, director, or genre..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </div>
          </div>
          
          {loading ? (
            <div className="text-center py-5">
              <Spinner animation="border" role="status">
                <span className="visually-hidden">Loading...</span>
              </Spinner>
              <p className="mt-2">Loading movies...</p>
            </div>
          ) : filteredMovies.length === 0 ? (
            <div className="text-center py-5">
              <FaFilm size={48} className="text-muted mb-3" />
              <h5>No movies found</h5>
              <p className="text-muted">
                {searchTerm ? 'Try a different search term' : 'Start by adding a new movie'}
              </p>
              {!searchTerm && (
                <Button as={Link} to="/admin/movies/new" variant="primary">
                  <FaPlus className="me-2" /> Add Movie
                </Button>
              )}
            </div>
          ) : (
            <div className="table-responsive">
              <Table hover className="align-middle">
                <thead>
                  <tr>
                    <th>Poster</th>
                    <th>Title</th>
                    <th>Genre</th>
                    <th>Language</th>
                    <th>Duration</th>
                    <th>Release Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredMovies.map((movie) => (
                    <tr key={movie.id}>
                      <td>
                        <img 
                          src={movie.posterUrl || 'https://via.placeholder.com/50x75?text=No+Poster'} 
                          alt={movie.title}
                          style={{ width: '50px', height: '75px', objectFit: 'cover' }}
                          className="rounded"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = 'https://via.placeholder.com/50x75?text=No+Poster';
                          }}
                        />
                      </td>
                      <td>
                        <div className="fw-bold">{movie.title}</div>
                        <small className="text-muted">{movie.director}</small>
                      </td>
                      <td>
                        {movie.genre?.split(',').map((g, i) => (
                          <Badge key={i} bg="secondary" className="me-1 mb-1">
                            {g.trim()}
                          </Badge>
                        ))}
                      </td>
                      <td>{movie.language}</td>
                      <td>{formatDuration(movie.duration)}</td>
                      <td>
                        {movie.releaseDate ? (
                          <>
                            <FaCalendarAlt className="me-1" />
                            {new Date(movie.releaseDate).toLocaleDateString()}
                          </>
                        ) : 'N/A'}
                      </td>
                      <td>
                        <Button 
                          variant="outline-primary" 
                          size="sm" 
                          as={Link}
                          to={`/admin/movies/edit/${movie.id}`}
                          className="me-2"
                          title="Edit Movie"
                        >
                          <FaEdit />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default AdminMoviesPage;
