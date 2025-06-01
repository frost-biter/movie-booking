import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { apiCall } from '../../services/api';
import { Loader2, CheckCircle2, XCircle, ArrowLeft } from 'lucide-react';

type PaymentMethod = 'UPI' | 'ETH';

interface PaymentPageLocationState {
  holdId: string;
  paymentAddress: string;
  paymentMethod: PaymentMethod;
  amount: number;
  movieName: string;
  theatreName: string;
  showTime: string;
  seats: string[];
}

interface PaymentStatusResponse {
  status: number;
  message: string;
}

const PaymentPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [paymentStatus, setPaymentStatus] = useState<'pending' | 'processing' | 'confirmed' | 'failed'>('pending');
  const [error, setError] = useState<string | null>(null);
  
  // Get and validate payment details from location state
  const state = location.state as PaymentPageLocationState | null;
  
  // Normalize payment method for display
  const normalizedPaymentMethod = state?.paymentMethod?.toUpperCase() || 'PAYMENT';
  
  if (!state) {
    return (
      <div className="text-center p-8">
        <h2 className="text-xl font-semibold mb-4">Invalid Payment Session</h2>
        <p className="mb-4">Please start the booking process again.</p>
        <button
          onClick={() => navigate('/')}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Back to Home
        </button>
      </div>
    );
  }

  const { 
    holdId, 
    paymentAddress, 
    paymentMethod,
    amount, 
    movieName, 
    theatreName, 
    showTime,
    seats 
  } = state;
  
  // Poll payment status
  useEffect(() => {
    if (paymentStatus !== 'processing') return;

    const checkPaymentStatus = async (): Promise<boolean> => {
      try {
        const response = await apiCall<PaymentStatusResponse>(
          `/api/payments/status/${paymentMethod.toUpperCase()}/${encodeURIComponent(paymentAddress)}?requiredAmount=${amount}`
        );

        console.log('Payment status response:', response);

        switch (response.status) {
          case 200: // Payment successful
          setPaymentStatus('confirmed');
            // Redirect to booking status after a short delay
            setTimeout(() => {
              navigate(`/booking/status?holdId=${holdId}`);
            }, 2000);
          return true;
          case 202: // Payment pending
            return false; // Continue polling
          case 400: // Payment failed or invalid amount
            setPaymentStatus('failed');
            setError(response.message || 'Payment failed. Please try again.');
            return true;
          default:
            throw new Error(response.message || 'Unknown payment status');
        }
      } catch (err) {
        console.error('Error checking payment status:', err);
        setPaymentStatus('failed');
        setError('Error verifying payment. Please check your booking status later.');
        return true;
      }
    };

    // Initial check
    let isComplete = false;
    checkPaymentStatus().then(complete => {
      isComplete = complete;
    });

    if (isComplete) return;

    // Set up polling if not complete
    const pollInterval = setInterval(async () => {
      const complete = await checkPaymentStatus();
      if (complete) {
        clearInterval(pollInterval);
      }
    }, 5000); // Poll every 5 seconds

    return () => clearInterval(pollInterval);
  }, [paymentStatus, paymentMethod, paymentAddress, amount, holdId, navigate]);

  const handleBack = () => {
    navigate(-1);
  };

  const handlePaymentComplete = () => {
    setPaymentStatus('processing');
  };

  return (
    <div className="max-w-2xl mx-auto p-6 bg-gray-900 text-white">
      <button 
        onClick={handleBack}
        className="flex items-center text-blue-400 hover:text-blue-300 mb-6"
      >
        <ArrowLeft size={20} className="mr-1" /> Back
      </button>

      <h1 className="text-2xl font-bold mb-6">Complete Your Payment</h1>
      
      <div className="bg-gray-800 rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Booking Summary</h2>
        <div className="space-y-2 mb-6">
          <p><span className="text-gray-400">Movie:</span> {movieName}</p>
          <p><span className="text-gray-400">Theatre:</span> {theatreName}</p>
          <p><span className="text-gray-400">Show Time:</span> {showTime}</p>
          <p><span className="text-gray-400">Seats:</span> {seats.join(', ')}</p>
          <p className="text-xl font-semibold mt-4">
            Total Amount: <span className="text-yellow-400">₹{amount.toFixed(2)}</span>
          </p>
        </div>

        {paymentStatus === 'pending' && (
          <div className="text-center py-8">
            <div className="mb-6 p-4 bg-gray-700 rounded-lg">
              <h3 className="font-semibold mb-2">
                Send {paymentMethod === 'UPI' ? 'UPI Payment To' : 'Crypto Payment To'}
              </h3>
              <p className="text-sm text-gray-300 break-all mb-4">{paymentAddress}</p>
              <div className="bg-white p-4 rounded inline-block">
                <div className="w-48 h-48 bg-gray-100 flex items-center justify-center">
                  <div className="text-center p-2">
                    <p className="text-sm text-gray-600 mb-2">
                      {normalizedPaymentMethod === 'UPI' ? 'UPI ID:' : `Send ${normalizedPaymentMethod} to:`}
                    </p>
                    <p className="font-mono text-xs break-all mb-2">{paymentAddress}</p>
                    {normalizedPaymentMethod === 'UPI' ? (
                      <p className="text-xs text-gray-500 mt-2">or scan with any UPI app</p>
                    ) : (
                      <p className="text-xs text-gray-700">Amount: {amount} {normalizedPaymentMethod}</p>
                    )}
                  </div>
                </div>
              </div>
            </div>
            <button
              onClick={handlePaymentComplete}
              className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium"
            >
              I've Sent the Payment
            </button>
          </div>
        )}

        {paymentStatus === 'processing' && (
          <div className="text-center py-8">
            <Loader2 className="h-12 w-12 text-blue-500 animate-spin mx-auto mb-4" />
            <p className="text-lg">Verifying your payment...</p>
            <p className="text-sm text-gray-400 mt-2">This may take a moment</p>
          </div>
        )}

        {paymentStatus === 'confirmed' && (
          <div className="text-center py-8">
            <CheckCircle2 className="h-16 w-16 text-green-500 mx-auto mb-4" />
            <h3 className="text-xl font-semibold mb-2">Payment Confirmed!</h3>
            <p>Redirecting to your booking...</p>
          </div>
        )}

        {paymentStatus === 'failed' && (
          <div className="text-center py-8">
            <XCircle className="h-16 w-16 text-red-500 mx-auto mb-4" />
            <h3 className="text-xl font-semibold mb-2">Payment Failed</h3>
            <p className="text-red-400 mb-4">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Try Again
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentPage;
