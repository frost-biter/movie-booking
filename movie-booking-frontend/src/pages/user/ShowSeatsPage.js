import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Spinner, Alert, Button, Card } from 'react-bootstrap';
import { FaArrowLeft, FaCheckCircle } from 'react-icons/fa';
import showService from '../../services/showService';

const categoryColors = {
  Regular: 'outline-primary',
  Premium: 'outline-warning',
  VIP: 'outline-danger',
  // add more categories and their colors here if needed
};

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
        
        const seatList = await showService.getSeatsForShow(showId);

        const seatsData = (seatList || []).map(seat => ({
          id: seat.seatId,
          row: seat.row,
          number: seat.seatNo,
          category: seat.category || 'Regular',
          seatName: seat.seatIdentifier || `${seat.row}${seat.seatNo}`,
          isBooked: seat.isBooked || false
        }));

        // Group seats by row for layout
        const groupedSeats = seatsData.reduce((acc, seat) => {
          acc[seat.row] = acc[seat.row] || [];
          acc[seat.row].push(seat);
          return acc;
        }, {});

        // Sort each row's seats by number ascending
        Object.keys(groupedSeats).forEach(row => {
          groupedSeats[row].sort((a, b) => a.number - b.number);
        });

        setShowDetails(showId); // You may want to fetch more details here separately
        setSeats(groupedSeats);
      } catch (err) {
        console.error('Error fetching show seats:', err);
        setError('Failed to load show details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (showId) fetchShowSeats();
  }, [showId]);

  const toggleSeatSelection = (seatId) => {
    setSelectedSeats(prev => 
      prev.includes(seatId)
        ? prev.filter(id => id !== seatId)
        : [...prev, seatId]
    );
  };

  const handleProceedToPayment = () => {
   console.log('Selected Seats:', selectedSeats);
    navigate(`/book-seats`, {
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
        <Spinner animation="border" role="status" />
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
            {/* You can replace showDetails with actual data if you fetch it */}
            <h3>{showDetails.movieName || `Show ${showId}`}</h3>
            <p>Theater: {showDetails.theatreName || 'Theatre 1'}</p>
            <p>Screen: {showDetails.screenName || 'Screen 1'}</p>
            <p>
              {showDetails.startTime ? new Date(showDetails.startTime).toLocaleString() : ''} -{' '}
              {showDetails.endTime ? new Date(showDetails.endTime).toLocaleTimeString() : ''}
            </p>
          </Card.Body>
        </Card>
      )}

      <div className="screen-display text-center mb-4 p-3 bg-light rounded">
        <h4>Screen This Way</h4>
        <div className="screen-line mx-auto" style={{ width: '60%', height: '8px', backgroundColor: '#999', borderRadius: 4 }}></div>
      </div>

      {/* Legend */}
      <div className="d-flex justify-content-center mb-3 gap-3 flex-wrap">
        <Button size="sm" variant="outline-primary" disabled className="d-flex align-items-center gap-1">
          <FaCheckCircle /> Available
        </Button>
        <Button size="sm" variant="secondary" disabled className="d-flex align-items-center gap-1">
          <FaCheckCircle /> Booked
        </Button>
        <Button size="sm" variant="success" disabled className="d-flex align-items-center gap-1">
          <FaCheckCircle /> Selected
        </Button>
      </div>

      {/* Seats grouped by row */}
      <div className="seats-layout mb-4">
        {Object.keys(seats).sort().map(row => (
          <div key={row} className="d-flex align-items-center mb-2" style={{ gap: '8px' }}>
            <div style={{ width: 30, fontWeight: 'bold' }}>{row}</div>
            <div className="d-flex flex-wrap" style={{ gap: '8px' }}>
              {seats[row].map(seat => {
                const isSelected = selectedSeats.includes(seat.id);
                const isBooked = seat.isBooked;
                const variant = isBooked
                  ? 'secondary'
                  : isSelected
                  ? 'success'
                  : categoryColors[seat.category] || 'outline-primary';

                return (
                  <Button
                    key={seat.id}
                    size="sm"
                    variant={variant}
                    disabled={isBooked}
                    onClick={() => !isBooked && toggleSeatSelection(seat.id)}
                    style={{
                      minWidth: 40,
                      userSelect: 'none',
                      cursor: isBooked ? 'not-allowed' : 'pointer',
                      borderRadius: 6,
                      fontWeight: '500',
                      padding: '0.25rem 0.5rem',
                      fontSize: '0.85rem'
                    }}
                    title={`${seat.seatName} - ${seat.category} - ${isBooked ? 'Booked' : isSelected ? 'Selected' : 'Available'}`}
                  >
                    {seat.seatName}
                    {isBooked && <FaCheckCircle className="ms-1" />}
                  </Button>
                );
              })}
            </div>
          </div>
        ))}
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
