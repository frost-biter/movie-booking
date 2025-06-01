import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { Button, Spinner, Alert, Modal, Badge, Container, Row, Col, Card } from 'react-bootstrap';
import { FaArrowLeft, FaCheckCircle, FaCreditCard, FaUniversity } from 'react-icons/fa';
import { BsFillCreditCardFill } from 'react-icons/bs';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import showService from '../../services/showService';
import moviesAPI from '../../services/movieService';

const SeatSelectionPage = () => {
  const { showId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [show, setShow] = useState(null);
  const [movie, setMovie] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [bookingInProgress, setBookingInProgress] = useState(false);
  const [bookingSuccess, setBookingSuccess] = useState(false);
  const [bookingId, setBookingId] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  
  // Seat layout configuration
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  const seatsPerRow = 10;
  
  // Load show and seat data
  const loadShowData = async () => {
    try {
      setLoading(true);
      
      // Extract show ID from the URL format 'show-id-{id}'
      const extractedShowId = showId.startsWith('show-id-') ? showId.substring(8) : showId;
      
      // Since /shows/show-id-{id} returns seats directly, we'll fetch the show details
      // from the shows list for the movie. First, let's get the movie ID from the URL
      const movieId = showId.match(/show-id-(\d+)/)[1];
      
      if (!movieId) {
        throw new Error('Invalid show ID format');
      }
      
      // Get all shows for the movie to find our show details
      const showsData = await showService.getShowsByMovie(movieId);
      
      // Find the specific show in the shows data
      const currentShow = showsData.flatMap(theatre => 
        Array.isArray(theatre.shows) ? theatre.shows : []
      ).find(show => String(show.id) === String(extractedShowId));
      
      if (!currentShow) {
        throw new Error('Show not found');
      }
      
      // Fetch movie details
      const movieData = await moviesAPI.getMovieById(movieId);
      
      // Fetch seats for this show
      const seatsData = await showService.getSeatsForShow(extractedShowId);
      
      // Process seats data
      const processedSeats = Array.isArray(seatsData) ? seatsData.map(seat => ({
        seatId: seat.seatId || seat.id,
        seatNumber: seat.seatNumber || seat.seatIdentifier,
        seatType: (seat.seatType || seat.category || 'standard').toLowerCase(),
        available: seat.available !== false,
        price: typeof seat.price === 'number' ? seat.price : currentShow.price || 200,
        row: seat.row || String.fromCharCode(65 + Math.floor(Math.random() * 8)), // A-H
        number: seat.number || (seat.seatNumber ? parseInt(seat.seatNumber.match(/\d+$/)?.[0]) || 1 : 1)
      })) : [];
      
      // Update state with fetched data
      setShow({
        ...currentShow,
        price: typeof currentShow.price === 'number' ? currentShow.price : 200
      });
      
      setMovie({
        ...movieData,
        genre: movieData.genre || 'Drama',
        language: movieData.language || 'English',
        duration: movieData.duration || 120,
        rating: movieData.rating || 0
      });
      
      setSeats(processedSeats);
      
      // If no seats data, generate some mock seats as fallback
      if (processedSeats.length === 0) {
        for (let row = 0; row < rows.length; row++) {
          for (let seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
            const seatId = `${rows[row]}${seatNum}`;
            processedSeats.push({
              seatId,
              seatNumber: seatId,
              seatType: Math.random() > 0.7 ? 'premium' : 'standard',
              available: Math.random() > 0.3,
              price: Math.random() > 0.7 ? 250 : 200,
              row: rows[row],
              number: seatNum
            });
          }
        }
      }
      
      setShow({
        ...currentShow,
        price: typeof currentShow.price === 'number' ? currentShow.price : 200
      });
      
      setMovie({
        ...movieData,
        // Ensure all required fields have default values
        genre: movieData.genre || 'Drama',
        language: movieData.language || 'English',
        duration: movieData.duration || 120,
        rating: movieData.rating || 0
      });
      
      setSeats(processedSeats);
      
    } catch (err) {
      console.error('Error loading show data:', err);
      setError('Failed to load show information. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    // Load show data without requiring login
    loadShowData();
  }, [showId]);
  
  const toggleSeatSelection = (seat) => {
    // Only allow selecting available seats
    if (!seat.available) {
      return;
    }
    
    setSelectedSeats(prev => {
      const isSelected = prev.some(s => s.seatId === seat.seatId);
      return isSelected 
        ? prev.filter(s => s.seatId !== seat.seatId)
        : [...prev, { ...seat }];
    });
  };

  const handleProceedToBooking = () => {
    if (selectedSeats.length === 0) {
      setError('Please select at least one seat');
      return;
    }
    setShowPaymentModal(true);
  };
  
  const handleConfirmBooking = async () => {
    if (selectedSeats.length === 0) {
      setError('Please select at least one seat');
      return;
    }

    // Validate phone number
    const phoneRegex = /^\d{10}$/;
    if (!phoneNumber || !phoneRegex.test(phoneNumber)) {
      setPhoneError('Please enter a valid 10-digit phone number');
      return;
    }

    setBookingInProgress(true);
    setError('');
    setPhoneError('');

    try {
      const bookingData = {
        showId: Number(showId),
        seatIds: selectedSeats.map(seat => Number(seat.seatId)),
        paymentMethod: paymentMethod,
        phoneNumber: phoneNumber
      };

      // Make the booking
      const response = await api.bookSeats(bookingData);
      
      if (response && response.holdId) {
        setBookingSuccess(true);
        setBookingId(response.holdId);
        
        // Redirect to booking status page after a short delay
        setTimeout(() => {
          navigate(`/booking/status/${response.holdId}`, {
            state: {
              movieName: movie?.title || 'Movie',
              theatreName: show?.theatre?.theatreName || 'Theatre',
              showTime: show?.showTime || 'N/A',
              seats: selectedSeats.map(s => s.seatNumber),
              totalAmount: calculateTotal()
            }
          });
        }, 2000);
      } else {
        throw new Error('Failed to confirm booking');
      }
    } catch (err) {
      console.error('Booking error:', err);
      setError(err.message || 'Failed to complete booking. Please try again.');
    } finally {
      setBookingInProgress(false);
    }
  };
  
  const calculateTotal = () => {
    return selectedSeats.reduce((total, seat) => total + (seat.price || 0), 0);
  };
  
  const handleProceedToPayment = () => {
    if (selectedSeats.length === 0) {
      setError('Please select at least one seat');
      return;
    }
    setShowPaymentModal(true);
  };
  
  const handleViewBooking = () => {
    navigate(`/booking-confirmation/${bookingId}`);
  };

  const getSeatTypeClass = (seat) => {
    if (!seat.available) return 'seat-unavailable';
    if (selectedSeats.some(s => s.seatId === seat.seatId)) return 'seat-selected';
    if (seat.seatType === 'premium') return 'seat-premium';
    return 'seat-available';
  };

  // Group seats by row for better rendering
  const seatsByRow = {};
  seats.forEach(seat => {
    if (!seatsByRow[seat.row]) {
      seatsByRow[seat.row] = [];
    }
    seatsByRow[seat.row].push(seat);
  });

  if (loading) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Loading seat map...</p>
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="danger">{error}</Alert>
    );
  }

  if (bookingSuccess) {
    return (
      <div className="text-center my-5">
        <div className="mb-4">
          <FaCheckCircle size={80} className="text-success mb-3" />
          <h2>Booking Successful!</h2>
          <p className="lead">Your booking ID is: <strong>{bookingId}</strong></p>
          <p>We've sent a confirmation to your email.</p>
        </div>
        <div className="d-flex justify-content-center gap-3">
          <Button variant="primary" onClick={handleViewBooking}>
            View Booking Details
          </Button>
          <Button variant="outline-secondary" onClick={() => navigate('/')}>
            Back to Home
          </Button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '50vh' }}>
        <Spinner animation="border" variant="primary" />
        <span className="ms-2">Loading seat selection...</span>
      </div>
    );
  }


  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          {error}
          <div className="mt-3">
            <Button variant="outline-danger" onClick={loadShowData}>
              Try Again
            </Button>
          </div>
        </Alert>
      </Container>
    );
  }

  if (bookingSuccess) {
    return (
      <Container className="my-5 text-center">
        <div className="mb-4">
          <FaCheckCircle size={80} className="text-success mb-3" />
          <h2>Booking Successful!</h2>
          <p className="lead">Your booking ID: {bookingId}</p>
          <p>You will receive a confirmation on your registered phone number.</p>
        </div>
        <div>
          <Button variant="primary" onClick={() => navigate('/')} className="me-2">
            Back to Home
          </Button>
          <Button variant="outline-primary" onClick={handleViewBooking}>
            View Booking Details
          </Button>
        </div>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Button 
        variant="outline-primary" 
        onClick={() => navigate(-1)}
        className="mb-4"
      >
        <FaArrowLeft className="me-2" /> Back to Shows
      </Button>

      <Row>
        <Col lg={8}>
          <Card className="mb-4">
            <Card.Body>
              <h2 className="h4 mb-3">Select Your Seats</h2>
              
              {movie && show && (
                <div className="mb-4">
                  <h3>{movie.title}</h3>
                  <p className="text-muted mb-1">
                    {show.theatre?.theatreName || 'Theatre Name'}
                    {show.theatre?.city && `, ${show.theatre.city}`}
                  </p>
                  <p className="text-muted">
                    {show.showTime ? new Date(show.showTime).toLocaleString() : 'Show Time'}
                  </p>
                </div>
              )}

              {/* Screen */}
              <div className="text-center mb-4">
                <div className="screen-indicator">SCREEN THIS WAY</div>
              </div>

              {/* Seats */}
              <div className="seats-container">
                {Object.entries(seatsByRow).map(([row, rowSeats]) => (
                  <div key={row} className="seat-row mb-3">
                    <div className="row-label fw-bold me-2">{row}</div>
                    <div className="d-flex flex-wrap gap-2">
                      {rowSeats.map(seat => (
                        <button
                          key={seat.seatId}
                          className={`seat ${getSeatTypeClass(seat)}`}
                          onClick={() => toggleSeatSelection(seat)}
                          disabled={!seat.available}
                          title={`${seat.seatNumber} - ${seat.available ? 'Available' : 'Not available'}`}
                        >
                          {seat.seatNumber}
                        </button>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              {/* Seat Legend */}
              <div className="d-flex justify-content-center gap-4 mt-4 pt-3 border-top">
                <div className="d-flex align-items-center">
                  <div className="seat-available me-2"></div>
                  <small>Available</small>
                </div>
                <div className="d-flex align-items-center">
                  <div className="seat-selected me-2"></div>
                  <small>Selected</small>
                </div>
                <div className="d-flex align-items-center">
                  <div className="seat-premium me-2"></div>
                  <small>Premium</small>
                </div>
                <div className="d-flex align-items-center">
                  <div className="seat-unavailable me-2"></div>
                  <small>Booked</small>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>

        {/* Booking Summary */}
        <Col lg={4}>
          <Card className="mb-4">
            <Card.Header className="bg-primary text-white">
              <h3 className="h5 mb-0">Booking Summary</h3>
            </Card.Header>
            <Card.Body>
              {movie && show && (
                <div className="mb-3">
                  <h4 className="h6">{movie.title}</h4>
                  <p className="text-muted small mb-1">
                    {show.theatre?.theatreName}
                    {show.theatre?.city && `, ${show.theatre.city}`}
                  </p>
                  <p className="text-muted small">
                    {show.showTime ? new Date(show.showTime).toLocaleDateString() : 'N/A'}
                    {show.showTime && ` • ${new Date(show.showTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`}
                  </p>
                </div>
              )}
              
              <div className="border-top border-bottom py-3 my-3">
                <div className="d-flex justify-content-between mb-2">
                  <span>Seats ({selectedSeats.length})</span>
                  <div>
                    {selectedSeats.map((seat) => (
                      <Badge key={seat.seatId} bg="light" text="dark" className="me-1">
                        {seat.seatNumber}
                        <button 
                          type="button" 
                          className="btn-close btn-close-white btn-sm ms-1" 
                          aria-label="Remove"
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleSeatSelection(seat);
                          }}
                        />
                      </Badge>
                    ))}
                  </div>
                </div>
                
                {selectedSeats.length > 0 && (
                  <div className="d-flex justify-content-between">
                    <span>Subtotal</span>
                    <span>₹{calculateTotal().toFixed(2)}</span>
                  </div>
                )}
              </div>

              <div className="d-flex justify-content-between fw-bold mb-3">
                <span>Amount Payable</span>
                <span>₹{(calculateTotal() * 1.18).toFixed(2)}</span>
              </div>

              <Button 
                variant="primary" 
                className="w-100"
                onClick={handleProceedToBooking}
                disabled={selectedSeats.length === 0 || bookingInProgress}
              >
                {bookingInProgress ? (
                  <>
                    <Spinner
                      as="span"
                      animation="border"
                      size="sm"
                      role="status"
                      aria-hidden="true"
                      className="me-2"
                    />
                    Processing...
                  </>
                ) : (
                  'Proceed to Pay'
                )}
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Payment Modal */}
      <Modal show={showPaymentModal} onHide={() => !bookingInProgress && setShowPaymentModal(false)} centered>
        <Modal.Header closeButton={!bookingInProgress}>
          <Modal.Title>Complete Payment</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {!bookingSuccess ? (
            <>
              <p>You are about to book {selectedSeats.length} seat(s) for a total of <strong>₹{(calculateTotal() * 1.18).toFixed(2)}</strong> including taxes.</p>
              <p>This amount will be charged to your payment method.</p>
              
              <div className="mb-3">
                <label htmlFor="phoneNumber" className="form-label">Phone Number</label>
                <input
                  type="tel"
                  className={`form-control ${phoneError ? 'is-invalid' : ''}`}
                  id="phoneNumber"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="Enter 10-digit phone number"
                  disabled={bookingInProgress}
                />
                {phoneError && <div className="invalid-feedback">{phoneError}</div>}
              </div>
              
              <div className="mb-4">
                <label className="form-label">Payment Method</label>
                <div className="d-flex gap-3">
                  <Button 
                    variant={paymentMethod === 'UPI' ? 'primary' : 'outline-primary'}
                    className="flex-grow-1 d-flex flex-column align-items-center py-3"
                    onClick={() => setPaymentMethod('UPI')}
                    disabled={bookingInProgress}
                  >
                    <BsFillCreditCardFill size={24} className="mb-2" />
                    <span>UPI</span>
                  </Button>
                  <Button 
                    variant={paymentMethod === 'Card' ? 'primary' : 'outline-primary'}
                    className="flex-grow-1 d-flex flex-column align-items-center py-3"
                    onClick={() => setPaymentMethod('Card')}
                    disabled={bookingInProgress}
                  >
                    <FaCreditCard size={24} className="mb-2" />
                    <span>Card</span>
                  </Button>
                  <Button 
                    variant={paymentMethod === 'NetBanking' ? 'primary' : 'outline-primary'}
                    className="flex-grow-1 d-flex flex-column align-items-center py-3"
                    onClick={() => setPaymentMethod('NetBanking')}
                    disabled={bookingInProgress}
                  >
                    <FaUniversity size={24} className="mb-2" />
                    <span>Net Banking</span>
                  </Button>
                </div>
              </div>

              <div className="d-grid gap-2">
                <Button 
                  variant="primary" 
                  size="lg"
                  onClick={handleConfirmBooking}
                  disabled={bookingInProgress || !phoneNumber}
                >
                  {bookingInProgress ? (
                    <>
                      <Spinner
                        as="span"
                        animation="border"
                        size="sm"
                        role="status"
                        aria-hidden="true"
                        className="me-2"
                      />
                      Processing...
                    </>
                  ) : (
                    `Pay ₹${(calculateTotal() * 1.18).toFixed(2)}`
                  )}
                </Button>
                <Button 
                  variant="outline-secondary" 
                  onClick={() => setShowPaymentModal(false)}
                  disabled={bookingInProgress}
                >
                  Cancel
                </Button>
              </div>

              {error && (
                <Alert variant="danger" className="mt-3 mb-0">
                  {error}
                </Alert>
              )}
            </>
          ) : (
            <div className="text-center py-4">
              <FaCheckCircle size={64} className="text-success mb-3" />
              <h4>Booking Confirmed!</h4>
              <p className="text-muted">Your booking ID: <strong>{bookingId}</strong></p>
              <p>You will receive a confirmation message shortly.</p>
              <Button 
                variant="primary" 
                onClick={() => navigate(`/bookings/${bookingId}`)}
                className="mt-3"
              >
                View Booking Details
              </Button>
            </div>
          )}
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default SeatSelectionPage;
