import React, { useState } from 'react';
import { MapPin } from 'lucide-react';
import { apiCall } from '../../services/api';

interface CitySelectorProps {
  onCitySet: (city: string) => void;
  initialCity?: string;
}

const CitySelector: React.FC<CitySelectorProps> = ({ onCitySet, initialCity = '' }) => {
  const [city, setCity] = useState(initialCity);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!city.trim()) {
      setError('Please enter a city name');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      await apiCall(`/city/set?cityName=${encodeURIComponent(city)}`, {
        method: 'POST',
      });
      onCitySet(city);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set city');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-gray-800 p-8 rounded-lg shadow-xl w-full max-w-md">
        <div className="text-center mb-8">
          <MapPin className="w-16 h-16 text-blue-500 mx-auto mb-4" />
          <h1 className="text-3xl font-bold text-white mb-2">Welcome Guest!</h1>
          <p className="text-gray-400">Select your city to start booking movies</p>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              type="text"
              placeholder="Enter your city name"
              className="w-full p-3 bg-gray-700 text-white rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          
          {error && (
            <div className="p-3 bg-red-600 text-white rounded-lg text-sm">
              {error}
            </div>
          )}
          
          <button
            type="submit"
            disabled={loading || !city.trim()}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white p-3 rounded-lg font-semibold disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <span className="animate-spin">↻</span>
                <span>Setting City...</span>
              </>
            ) : 'Start Booking'}
          </button>
        </form>
        
        <div className="mt-6 text-center text-sm text-gray-400">
          <p>No registration required! Just select your city and start browsing movies.</p>
        </div>
      </div>
    </div>
  );
};

export default CitySelector;
