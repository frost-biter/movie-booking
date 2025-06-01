import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Spinner, Alert, Button, Row, Col, Card } from 'react-bootstrap';
import { FaArrowLeft, FaChair, FaCheckCircle } from 'react-icons/fa';
import showService from '../../services/showService';

const ShowSeatsPage = () => {
  const { showId } = useParams();
  const navigate = useNavigate();
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [showDetails, setShowDetails] = useState(null);

  useEffect(() => {
    const fetchShowSeats = async () => {
      try {
        setLoading(true);
        setError('');
        
        // Fetch show details and seats
        const showData = await showService.getSeatsForShow(showId);
        setShowDetails(showData.show);
        setSeats(showData.seats || []);
      } catch (err) {
        console.error('Error fetching show seats:', err);
        setError('Failed to load show details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (showId) {
      fetchShowSeats();
    }
  }, [showId]);

  const toggleSeatSelection = (seatId) => {
    setSelectedSeats(prev => 
      prev.includes(seatId)
        ? prev.filter(id => id !== seatId)
        : [...prev, seatId]
    );
  };

  const handleProceedToPayment = () => {
    // Navigate to payment page with selected seats
    navigate(`/payment`, { 
      state: { 
        showId,
        selectedSeats,
        showDetails
      } 
    });
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
        <Button variant="secondary" onClick={() => navigate(-1)} className="mt-3">
          <FaArrowLeft className="me-2" /> Go Back
        </Button>
      </Container>
    );
  }

  return (
    <Container className="my-5">
      <Button variant="outline-secondary" onClick={() => navigate(-1)} className="mb-4">
        <FaArrowLeft className="me-2" /> Back to Shows
      </Button>
      
      {showDetails && (
        <Card className="mb-4">
          <Card.Body>
            <h3>{showDetails.movieName}</h3>
            <p className="mb-1">Theater: {showDetails.theatreName}</p>
            <p className="mb-1">Screen: {showDetails.screenName || 'Screen 1'}</p>
            <p className="mb-0">
              {new Date(showDetails.startTime).toLocaleString()} - {new Date(showDetails.endTime).toLocaleTimeString()}
            </p>
          </Card.Body>
        </Card>
      )}

      <div className="screen-display text-center mb-4 p-3 bg-light rounded">
        <h4>Screen This Way</h4>
        <div className="screen-line"></div>
      </div>

      <div className="seats-layout mb-4">
        <Row className="justify-content-center">
          {seats.map(seat => (
            <Col key={seat.id} xs="auto" className="mb-2">
              <Button
                variant={seat.isBooked ? 'secondary' : selectedSeats.includes(seat.id) ? 'success' : 'outline-primary'}
                disabled={seat.isBooked}
                onClick={() => !seat.isBooked && toggleSeatSelection(seat.id)}
                className="seat-button"
              >
                {seat.seatNumber}
                {seat.isBooked && <FaCheckCircle className="ms-1" />}
              </Button>
            </Col>
          ))}
        </Row>
      </div>

      <div className="text-center">
        <Button 
          variant="primary" 
          size="lg" 
          disabled={selectedSeats.length === 0}
          onClick={handleProceedToPayment}
        >
          Proceed to Pay ({selectedSeats.length} {selectedSeats.length === 1 ? 'Seat' : 'Seats'})
        </Button>
      </div>
    </Container>
  );
};

export default ShowSeatsPage;
