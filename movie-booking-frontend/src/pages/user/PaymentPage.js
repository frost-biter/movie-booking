import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { FaArrowLeft } from 'react-icons/fa';

const PAYMENT_METHODS = ['UPI', 'ETH', 'CARD', 'WALLET', 'NETBANKING'];

const PaymentPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Extract from previous page
  const { showId, selectedSeats, showDetails } = location.state || {};

  // Declare all hooks at the top unconditionally
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [upiId, setUpiId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [status, setStatus] = useState(null);
  const [holdId, setHoldId] = useState(null);
  const [paymentInfo, setPaymentInfo] = useState(null);
  const [ticket, setTicket] = useState(null);
  const [countdown, setCountdown] = useState(null); // seconds left in countdown

  const countdownInterval = useRef(null);
  const paymentPollingInterval = useRef(null);

  // Cleanup intervals on unmount
  useEffect(() => {
    return () => {
      if (countdownInterval.current) clearInterval(countdownInterval.current);
      if (paymentPollingInterval.current) clearInterval(paymentPollingInterval.current);
    };
  }, []);

  // Early return if invalid booking data - after all hooks declared
  if (!showId || !selectedSeats || selectedSeats.length === 0) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">No booking information found. Please select seats first.</Alert>
        <Button onClick={() => navigate(-1)}>Go Back</Button>
      </Container>
    );
  }

  // Format countdown display mm:ss
  const formatCountdown = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const startCountdown = () => {
    setCountdown(300); // 5 minutes in seconds

    countdownInterval.current = setInterval(() => {
      setCountdown((prev) => {
        if (prev === 1) {
          clearInterval(countdownInterval.current);
          countdownInterval.current = null;

          stopPaymentPolling();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  // Payment status polling method
  const pollPaymentStatus = (method, address, amount, holdIdToFetch) => {
    console.log('Starting payment polling for', method, address, amount);
  
    if (paymentPollingInterval.current) clearInterval(paymentPollingInterval.current);
  
    paymentPollingInterval.current = setInterval(async () => {
        try {
          const status = await checkPaymentStatus(method, address, amount);
      
          if (status === 'success') {
            clearInterval(paymentPollingInterval.current);
            paymentPollingInterval.current = null;
            setStatus('Payment Successful 🎉 Ticket confirmed.');
            setCountdown(null);
            if (countdownInterval.current) {
              clearInterval(countdownInterval.current);
              countdownInterval.current = null;
            }
            if (holdIdToFetch) {
                navigate(`/ticket/${holdIdToFetch}`);
              } else {
                console.error("Cannot navigate: holdId is null");
              }
          } else if (status === 'pending') {
            setStatus('Payment Pending... Please complete the payment.');
          }
        } catch (error) {
          console.error('Polling error:', error);
          setStatus(null);
          setError(error.message || 'Error checking payment status');
        }
      }, 3000);
      
  };
  
  const stopPaymentPolling = () => {
    if (paymentPollingInterval.current) {
      clearInterval(paymentPollingInterval.current);
      paymentPollingInterval.current = null;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setStatus(null);
    setTicket(null);
    setLoading(true);

    const payload = {
      showId: Number(showId),         // Convert to number ✅
      seatIds: selectedSeats.map(Number), // Just to be extra safe ✅
      paymentMethod,
      phoneNumber
    };

    try {
      console.log('Initiating booking with payload:', payload);
      const res = await fetch('http://localhost:8080/booking/seats', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errData = await res.json();
        throw new Error(errData.message || 'Failed to initiate booking');
      }

      const data = await res.json();
      setHoldId(data.holdId);
      setPaymentInfo({
        method: data.paymentMethod,
        address: data.paymentAddress,
        amount: data.price,
      });

      setStatus('Payment Pending... Please complete the payment within 5 minutes.');
      setLoading(false);

      // Start countdown timer and payment status polling after payment initiation
      startCountdown();
      pollPaymentStatus(data.paymentMethod, data.paymentAddress, data.price, data.holdId);
    } catch (err) {
      setError(err.message || 'Error initiating payment');
      setLoading(false);
    }
  };

  const checkPaymentStatus = async (paymentMethod, address, amount) => {
    console.log(`Checking payment status for ${paymentMethod}/${address}`);
    try {
      const res = await fetch(
        `http://localhost:8080/api/payments/status/${paymentMethod}/${address}?requiredAmount=${amount}`,
        { credentials: 'include' }
      );
  
      const data = await res.json();
      console.log('Payment status response data:', data);
  
      if (res.status === 200 && data.status === 200) {
        return 'success';
      } else if (res.status === 202 && data.status === 202) {
        return 'pending';
      } else {
        throw new Error(data.message || 'Payment failed or unknown error');
      }
    } catch (err) {
      console.error('Error checking payment status:', err);
      throw err;
    }
  };
  
  
  


  
  

  return (
    <Container className="my-5" style={{ maxWidth: '600px' }}>
      <Button variant="outline-secondary" onClick={() => navigate(-1)} className="mb-4">
        <FaArrowLeft className="me-2" /> Back to Seat Selection
      </Button>

      <Card className="mb-4 p-4">
        {status && <Alert variant="info" className="mt-3">{status}</Alert>}

        {paymentInfo && (
          <Alert variant="secondary" className="mt-3">
            <p><strong>Send {paymentInfo.amount} via {paymentInfo.method} to:</strong></p>
            <p style={{ fontFamily: 'monospace' }}>{paymentInfo.address}</p>
            <Button
              size="sm"
              variant="outline-primary"
              onClick={() => navigator.clipboard.writeText(paymentInfo.address)}
            >
              Copy Address
            </Button>
          </Alert>
        )}

        {countdown !== null && countdown > 0 && (
          <Alert variant="warning" className="mt-3">
            Payment time remaining: <strong>{formatCountdown(countdown)}</strong>
          </Alert>
        )}

        <h4>Booking Summary</h4>
        <p><strong>Show:</strong> {showDetails?.movieName || `Show ${showId}`}</p>
        <p><strong>Seats:</strong> {selectedSeats.join(', ')}</p>
      </Card>

      <Card className="p-4">
        <h4>Select Payment Method</h4>
        <Form onSubmit={handleSubmit} disabled={!!ticket || countdown > 0}>
          <Form.Group className="mb-3">
            <Form.Label>Payment Method</Form.Label>
            <Form.Select
              value={paymentMethod}
              onChange={e => setPaymentMethod(e.target.value)}
              disabled={!!ticket || countdown > 0}
            >
              {PAYMENT_METHODS.map(method => (
                <option key={method} value={method}>{method}</option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Phone Number</Form.Label>
            <Form.Control
              type="text"
              value={phoneNumber}
              onChange={e => setPhoneNumber(e.target.value)}
              placeholder="Enter your phone number"
              disabled={!!ticket || countdown > 0}
            />
          </Form.Group>

          {paymentMethod === 'UPI' && (
            <Form.Group className="mb-3">
              <Form.Label>UPI ID</Form.Label>
              <Form.Control
                type="text"
                value={upiId}
                onChange={e => setUpiId(e.target.value)}
                placeholder="example@upi"
                disabled={!!ticket || countdown > 0}
              />
            </Form.Group>
          )}

          {!ticket && (
            <Button type="submit" disabled={loading || countdown > 0}>
              {loading ? 'Processing...' : 'Make Payment'}
            </Button>
          )}
        </Form>
      </Card>

      {ticket && (
        <Card className="mt-4 p-4 border-success">
          <h4 className="text-success">🎉 Booking Confirmed!</h4>
          <p><strong>Booking ID:</strong> {ticket.bookingId}</p>
          <p><strong>Seats:</strong> {ticket.seats.join(', ')}</p>
          <p><strong>Show:</strong> {showDetails?.movieName || `Show ${showId}`}</p>
          <Button onClick={() => navigate('/')}>Go to Home</Button>
        </Card>
      )}

      {error && (
        <Alert variant="danger" className="mt-3">
          {error}
        </Alert>
      )}
    </Container>
  );
};

export default PaymentPage;