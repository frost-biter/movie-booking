import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Card, Form, Button, Spinner, Alert, Row, Col } from 'react-bootstrap';
import { FaArrowLeft, FaSave } from 'react-icons/fa';
import movieService from '../../services/movieService';

const MovieFormPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    duration: 120,
    language: 'English',
    releaseDate: new Date().toISOString().split('T')[0],
    genre: [],
    cast: '',
    director: '',
    posterUrl: '',
    trailerUrl: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Available genres
  const genreOptions = [
    'Action', 'Adventure', 'Animation', 'Comedy', 'Crime', 'Documentary',
    'Drama', 'Family', 'Fantasy', 'History', 'Horror', 'Music', 'Mystery',
    'Romance', 'Sci-Fi', 'Thriller', 'War', 'Western'
  ];

  // Fetch movie data if in edit mode
  useEffect(() => {
    if (isEditMode) {
      const fetchMovie = async () => {
        try {
          setLoading(true);
          // Note: This endpoint needs to be implemented in the backend
          // const movie = await movieService.getMovieById(id);
          // setFormData({
          //   ...movie,
          //   releaseDate: movie.releaseDate.split('T')[0]
          // });
        } catch (err) {
          console.error('Error fetching movie:', err);
          setError('Failed to load movie data. Please try again.');
        } finally {
          setLoading(false);
        }
      };

      fetchMovie();
    }
  }, [id, isEditMode]);

  const handleChange = (e) => {
    const { name, value, type, options } = e.target;
    
    if (type === 'select-multiple') {
      const selectedOptions = Array.from(options)
        .filter(option => option.selected)
        .map(option => option.value);
      setFormData(prev => ({
        ...prev,
        [name]: selectedOptions
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: type === 'number' ? parseInt(value) : value
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Format the data before sending
      const movieData = {
        title: formData.title,
        description: formData.description,
        duration: parseInt(formData.duration, 10),
        language: formData.language,
        releaseDate: formData.releaseDate,
        genre: formData.genre.join(', '), // Convert array to comma-separated string
        cast: formData.cast,
        director: formData.director,
        posterUrl: formData.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster',
        trailerUrl: formData.trailerUrl
      };

      if (isEditMode) {
        // Update existing movie - not implemented in backend yet
        // await movieService.updateMovie(id, movieData);
        alert('Movie update functionality is not yet implemented');
      } else {
        // Add new movie
        await movieService.addMovie(movieData);
        alert('Movie added successfully!');
        navigate('/admin/movies');
      }
    } catch (err) {
      console.error('Error saving movie:', err);
      setError(err.message || 'Failed to save movie. Please check all fields and try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading && isEditMode) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
      </Container>
    );
  }

  return (
    <Container fluid className="py-4">
      <Button 
        variant="outline-secondary" 
        onClick={() => navigate(-1)}
        className="mb-4"
      >
        <FaArrowLeft className="me-2" /> Back to Movies
      </Button>

      <h1 className="mb-4">{isEditMode ? 'Edit Movie' : 'Add New Movie'}</h1>

      {error && <Alert variant="danger">{error}</Alert>}

      <Card>
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Row>
              <Col md={8}>
                <Form.Group className="mb-3">
                  <Form.Label>Title *</Form.Label>
                  <Form.Control
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                  />
                </Form.Group>

                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Duration (minutes) *</Form.Label>
                      <Form.Control
                        type="number"
                        name="duration"
                        min="1"
                        value={formData.duration}
                        onChange={handleChange}
                        required
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Release Date *</Form.Label>
                      <Form.Control
                        type="date"
                        name="releaseDate"
                        value={formData.releaseDate}
                        onChange={handleChange}
                        required
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <Form.Label>Genre *</Form.Label>
                  <Form.Select
                    multiple
                    name="genre"
                    value={formData.genre}
                    onChange={handleChange}
                    required
                  >
                    {genreOptions.map(genre => (
                      <option key={genre} value={genre}>
                        {genre}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    Hold Ctrl (or Cmd on Mac) to select multiple genres
                  </Form.Text>
                </Form.Group>
              </Col>

              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Language *</Form.Label>
                  <Form.Select
                    name="language"
                    value={formData.language}
                    onChange={handleChange}
                    required
                  >
                    <option value="English">English</option>
                    <option value="Hindi">Hindi</option>
                    <option value="Tamil">Tamil</option>
                    <option value="Telugu">Telugu</option>
                    <option value="Kannada">Kannada</option>
                    <option value="Malayalam">Malayalam</option>
                    <option value="Other">Other</option>
                  </Form.Select>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Director *</Form.Label>
                  <Form.Control
                    type="text"
                    name="director"
                    value={formData.director}
                    onChange={handleChange}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Cast</Form.Label>
                  <Form.Control
                    type="text"
                    name="cast"
                    value={formData.cast}
                    onChange={handleChange}
                    placeholder="Comma-separated list of actors"
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Poster URL</Form.Label>
                  <Form.Control
                    type="url"
                    name="posterUrl"
                    value={formData.posterUrl}
                    onChange={handleChange}
                    placeholder="https://example.com/poster.jpg"
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Trailer URL</Form.Label>
                  <Form.Control
                    type="url"
                    name="trailerUrl"
                    value={formData.trailerUrl}
                    onChange={handleChange}
                    placeholder="https://youtube.com/watch?v=..."
                  />
                </Form.Group>
              </Col>
            </Row>

            <div className="d-flex justify-content-end mt-4">
              <Button 
                variant="secondary" 
                onClick={() => navigate('/admin/movies')}
                className="me-2"
              >
                Cancel
              </Button>
              <Button 
                variant="primary" 
                type="submit"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <Spinner
                      as="span"
                      animation="border"
                      size="sm"
                      role="status"
                      aria-hidden="true"
                      className="me-2"
                    />
                    Saving...
                  </>
                ) : (
                  <>
                    <FaSave className="me-2" />
                    {isEditMode ? 'Update Movie' : 'Save Movie'}
                  </>
                )}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default MovieFormPage;
