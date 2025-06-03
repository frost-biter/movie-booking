// import React, { useState, useEffect } from 'react';
// import { useParams, useNavigate } from 'react-router-dom';
// import { Container, Card, Button, Spinner, Alert, Row, Col, Badge } from 'react-bootstrap';
// import { FaCheckCircle, FaPrint, FaEnvelope, FaTicketAlt, FaMapMarkerAlt, FaCalendarAlt, FaClock, FaChair } from 'react-icons/fa';
// import { useAuth } from '../context/AuthContext';
// import bookingService from '../services/bookingService';

// const BookingConfirmationPage = () => {
//   const { bookingId } = useParams();
//   const navigate = useNavigate();
//   const { user } = useAuth();
  
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState('');
//   const [booking, setBooking] = useState(null);
//   const [emailSending, setEmailSending] = useState(false);
//   const [emailSent, setEmailSent] = useState(false);

//   // Mock booking data - in a real app, this would come from the API
 
//   useEffect(() => {
//     const fetchBookingDetails = async () => {
//       try {
//         setLoading(true);
//         // In a real app, you would fetch booking details from the API
//         // const data = await bookingService.getBooking(bookingId);
//         // setBooking(data);
        
//         // Using mock data for now
//         setTimeout(() => {
//           setBooking(mockBooking);
//           setLoading(false);
//         }, 1000);
        
//       } catch (err) {
//         console.error('Error fetching booking details:', err);
//         setError('Failed to load booking details. Please try again later.');
//         setLoading(false);
//       }
//     };

//     fetchBookingDetails();
//   }, [bookingId]);

//   const handlePrint = () => {
//     window.print();
//   };

//   const handleEmailTicket = async () => {
//     if (!user?.email) {
//       setError('No email address found. Please update your profile.');
//       return;
//     }

//     try {
//       setEmailSending(true);
//       // In a real app, you would call an API to send the email
//       await new Promise(resolve => setTimeout(resolve, 1500));
//       setEmailSent(true);
      
//       // Reset the success message after 5 seconds
//       setTimeout(() => setEmailSent(false), 5000);
      
//     } catch (err) {
//       console.error('Error sending email:', err);
//       setError('Failed to send email. Please try again.');
//     } finally {
//       setEmailSending(false);
//     }
//   };

//   const formatDate = (dateString) => {
//     const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
//     return new Date(dateString).toLocaleDateString('en-US', options);
//   };

//   if (loading) {
//     return (
//       <Container className="text-center my-5">
//         <Spinner animation="border" role="status">
//           <span className="visually-hidden">Loading...</span>
//         </Spinner>
//         <p className="mt-2">Loading your booking details...</p>
//       </Container>
//     );
//   }

//   if (error) {
//     return (
//       <Container className="my-5">
//         <Alert variant="danger">{error}</Alert>
//         <Button variant="outline-primary" onClick={() => navigate('/')} className="mt-3">
//           Back to Home
//         </Button>
//       </Container>
//     );
//   }

//   if (!booking) {
//     return (
//       <Container className="my-5">
//         <Alert variant="warning">Booking not found</Alert>
//         <Button variant="outline-primary" onClick={() => navigate('/')} className="mt-3">
//           Back to Home
//         </Button>
//       </Container>
//     );
//   }

//   return (
//     <Container className="my-5">
//       <div className="text-center mb-5">
//         <FaCheckCircle size={64} className="text-success mb-3" />
//         <h1 className="mb-3">Booking Confirmed!</h1>
//         <p className="lead">Your booking ID: <strong>{booking.id}</strong></p>
        
//         <div className="d-flex justify-content-center gap-3 mb-5">
//           <Button variant="outline-primary" onClick={handlePrint}>
//             <FaPrint className="me-2" /> Print Ticket
//           </Button>
//           <Button 
//             variant="outline-secondary" 
//             onClick={handleEmailTicket}
//             disabled={emailSending || emailSent}
//           >
//             {emailSending ? (
//               <>
//                 <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
//                 Sending...
//               </>
//             ) : emailSent ? (
//               <>
//                 <FaCheckCircle className="me-2" /> Sent!
//               </>
//             ) : (
//               <>
//                 <FaEnvelope className="me-2" /> Email Ticket
//               </>
//             )}
//           </Button>
//         </div>
        
//         {emailSent && (
//           <Alert variant="success" className="d-inline-block">
//             Ticket has been sent to {user.email}
//           </Alert>
//         )}
//       </div>
      
//       <Card className="mb-4">
//         <Card.Body>
//           <Row>
//             <Col md={4}>
//               <img 
//                 src={booking.movie.posterUrl} 
//                 alt={booking.movie.title} 
//                 className="img-fluid rounded"
//                 style={{ maxHeight: '400px' }}
//               />
//             </Col>
//             <Col md={8}>
//               <h2>{booking.movie.title}</h2>
//               <div className="d-flex flex-wrap gap-2 mb-3">
//                 <Badge bg="secondary">{booking.movie.language}</Badge>
//                 <Badge bg="info">{booking.movie.duration}</Badge>
//                 <Badge bg={booking.status === 'CONFIRMED' ? 'success' : 'warning'}>
//                   {booking.status}
//                 </Badge>
//               </div>
              
//               <div className="mb-4">
//                 <h5 className="d-flex align-items-center mb-3">
//                   <FaTicketAlt className="me-2" /> Booking Details
//                 </h5>
//                 <div className="ms-4">
//                   <p className="mb-2">
//                     <strong>Booking ID:</strong> {booking.id}
//                   </p>
//                   <p className="mb-2">
//                     <strong>Booking Date:</strong> {formatDate(booking.bookingDate)}
//                   </p>
//                   <p className="mb-2">
//                     <strong>Total Amount:</strong> ₹{booking.totalAmount.toFixed(2)}
//                   </p>
//                 </div>
//               </div>
              
//               <div className="mb-4">
//                 <h5 className="d-flex align-items-center mb-3">
//                   <FaMapMarkerAlt className="me-2" /> Venue Details
//                 </h5>
//                 <div className="ms-4">
//                   <p className="mb-1 fw-bold">{booking.theatre.name} - {booking.theatre.screen}</p>
//                   <p className="text-muted mb-2">{booking.theatre.address}</p>
//                 </div>
//               </div>
              
//               <div className="mb-4">
//                 <h5 className="d-flex align-items-center mb-3">
//                   <FaCalendarAlt className="me-2" /> Show Time
//                 </h5>
//                 <div className="ms-4">
//                   <p className="mb-1">
//                     <strong>Date:</strong> {formatDate(booking.show.date)}
//                   </p>
//                   <p className="mb-1">
//                     <strong>Time:</strong> {booking.show.time}
//                   </p>
//                   <p className="mb-1">
//                     <strong>Format:</strong> {booking.show.format} • {booking.show.language}
//                   </p>
//                 </div>
//               </div>
              
//               <div>
//                 <h5 className="d-flex align-items-center mb-3">
//                   <FaChair className="me-2" /> Seats
//                 </h5>
//                 <div className="d-flex flex-wrap gap-2 ms-4">
//                   {booking.seats.map((seat, index) => (
//                     <Badge key={index} bg="primary" className="px-3 py-2">
//                       {seat}
//                     </Badge>
//                   ))}
//                 </div>
//               </div>
//             </Col>
//           </Row>
//         </Card.Body>
//       </Card>
      
//       <Card className="mb-4">
//         <Card.Body>
//           <h5 className="mb-3">Important Information</h5>
//           <ul className="mb-0">
//             <li>Please arrive at the cinema at least 30 minutes before the showtime.</li>
//             <li>Carry a valid ID proof along with this booking confirmation.</li>
//             <li>Entry will not be allowed after the showtime.</li>
//             <li>Refunds will be processed as per the cancellation policy.</li>
//           </ul>
//         </Card.Body>
//       </Card>
      
//       <div className="text-center mt-5">
//         <Button variant="primary" size="lg" onClick={() => navigate('/')} className="me-2">
//           Back to Home
//         </Button>
//         <Button variant="outline-primary" size="lg" onClick={() => navigate('/movies')}>
//           Browse More Movies
//         </Button>
//       </div>
//     </Container>
//   );
// };

// export default BookingConfirmationPage;



// import React, { useState, useEffect, useRef } from 'react';
// import { useParams, useNavigate } from 'react-router-dom';

// const BookingConfirmationPage = () => {
//   const { holdId } = useParams(); // or bookingId if renamed
//   const navigate = useNavigate();

//   const [status, setStatus] = useState('');
//   const [error, setError] = useState('');
//   const [ticket, setTicket] = useState(null);
//   const countdownInterval = useRef(null);

//   // Helper to stop countdown polling (you may want to adjust depending on your existing logic)
//   const stopPaymentPolling = () => {
//     if (countdownInterval.current) {
//       clearInterval(countdownInterval.current);
//       countdownInterval.current = null;
//       setStatus('');
//     }
//   };

//   // Ticket fetch & poll
//   const fetchTicket = async (holdIdToFetch) => {
//     setStatus('Checking payment and booking status...');

//     let attempts = 0;
//     const maxAttempts = 10;
//     const pollingInterval = 2000; // 2 seconds

//     const poll = async () => {
//       try {
//         const ticketRes = await fetch(
//           `http://localhost:8080/booking/bookings?holdId=${holdIdToFetch}`, 
//           { credentials: 'include' }
//         );

//         if (ticketRes.status === 404) {
//           // Ticket not ready yet – retry
//           attempts++;
//           if (attempts < maxAttempts) {
//             setTimeout(poll, pollingInterval);
//           } else {
//             setStatus('Ticket not found after multiple attempts.');
//             setError('Booking timed out. Please try again.');
//           }
//           return;
//         }

//         if (!ticketRes.ok) {
//           throw new Error(`Failed to fetch ticket: ${ticketRes.statusText}`);
//         }

//         const ticketData = await ticketRes.json();

//         if (ticketData && ticketData.seats && ticketData.seats.length > 0) {
//           setTicket(ticketData);
//           setStatus('Payment Successful 🎉 Ticket confirmed.');
//           stopPaymentPolling();
//           setError('');
//           return; // stop polling after success
//         } else {
//           attempts++;
//           if (attempts < maxAttempts) {
//             setTimeout(poll, pollingInterval);
//           } else {
//             setStatus('Payment not received or seats not booked.');
//             setError('Payment failed or timed out. Please try again.');
//           }
//         }
//       } catch (err) {
//         console.error('Error fetching ticket:', err);
//         setError(err.message || 'Error fetching ticket');
//         setStatus(null);
//       }
//     };

//     poll();
//   };

//   useEffect(() => {
//     if (holdId) {
//       fetchTicket(holdId);
//     } else {
//       setError('No booking ID provided.');
//     }

//     // Cleanup if needed on unmount
//     return () => {
//       stopPaymentPolling();
//     };
//   }, [holdId]);

//   // Render your ticket details or loading/error UI here
//   // For example:
//   if (error) {
//     return <div>Error: {error}</div>;
//   }

//   if (!ticket) {
//     return <div>{status || 'Loading ticket...'}</div>;
//   }

//   return (
//     <div>
//       <h1>Booking Confirmed!</h1>
//       <p>Booking ID: {ticket.id}</p>
//       {/* Render rest of your ticket details */}
//     </div>
//   );
// };

// export default BookingConfirmationPage;

import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const BookingConfirmationPage = () => {
  const { holdId } = useParams();
  const navigate = useNavigate();

  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [ticket, setTicket] = useState(null);
  const countdownInterval = useRef(null);

  const stopPaymentPolling = () => {
    if (countdownInterval.current) {
      clearInterval(countdownInterval.current);
      countdownInterval.current = null;
      setStatus('');
    }
  };

  const fetchTicket = async (holdIdToFetch) => {
    let isMounted = true;
    let pollTimeout;

    const poll = async () => {
      if (!isMounted) return;
      
      try {
        const ticketRes = await fetch(
          `http://localhost:8080/booking/bookings?holdId=${holdIdToFetch}`,
          { credentials: 'include' }
        );

        if (ticketRes.status === 404) {
          setStatus('Ticket not found after multiple attempts.');
          setError('Booking timed out. Please try again.');
          return;
        }

        if (!ticketRes.ok) {
          throw new Error(`Failed to fetch ticket: ${ticketRes.statusText}`);
        }

        const ticketData = await ticketRes.json();

        if (ticketData && ticketData.seats && ticketData.seats.length > 0) {
          setTicket(ticketData);
          setStatus('Payment Successful 🎉 Ticket confirmed.');
          stopPaymentPolling();
          setError('');
          return;
        } else {
          setStatus('Payment not received or seats not booked.');
          setError('Payment failed or timed out. Please try again.');
        }
      } catch (err) {
        console.error('Error fetching ticket:', err);
        setError(err.message || 'Error fetching ticket');
        setStatus(null);
      }

      if (isMounted) {
        pollTimeout = setTimeout(poll, 2000);
      }
    };

    poll();

    return () => {
      isMounted = false;
      if (pollTimeout) {
        clearTimeout(pollTimeout);
      }
    };
  };

  useEffect(() => {
    if (holdId) {
      fetchTicket(holdId);
    } else {
      setError('No booking ID provided.');
    }

    return () => {
      stopPaymentPolling();
    };
  }, [holdId]);

  const formatDateTime = (arr) => {
    if (!arr || arr.length < 5) return 'Invalid Date';
    const [year, month, day, hour, minute] = arr;
    const date = new Date(year, month - 1, day, hour, minute);
    return date.toLocaleString();
  };

  if (error) {
    return <div style={{ color: 'red' }}>❌ Error: {error}</div>;
  }

  if (!ticket) {
    return <div>{status || 'Loading ticket...'}</div>;
  }

  return (
    <div style={{ maxWidth: '600px', margin: 'auto', padding: '20px', border: '2px solid #ccc', borderRadius: '10px' }}>
      <h1 style={{ color: 'green' }}>🎉 Booking Confirmed!</h1>
      <p><strong>Movie:</strong> {ticket.movieName}</p>
      <p><strong>Theatre:</strong> {ticket.theatreName}</p>
      <p><strong>Show Time:</strong> {formatDateTime(ticket.showTime)}</p>
      <p><strong>Seats:</strong> {ticket.seats.map(seat => seat.seatIdentifier).join(', ')}</p>
      <p><strong>Category:</strong> {ticket.seats[0]?.category}</p>
      <p><strong>Phone Number:</strong> {ticket.phoneNumber}</p>
      <p><strong>Booking Time:</strong> {formatDateTime(ticket.bookingTime)}</p>
    </div>
  );
};

export default BookingConfirmationPage;
