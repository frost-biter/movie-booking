import React, { useState } from 'react';
import { Navbar, Container, Nav, Form } from 'react-bootstrap';
import { FaMapMarkerAlt, FaTicketAlt, FaSearch, FaChevronDown } from 'react-icons/fa';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const UserHeader = ({ city, onCityChange }) => {
  const [selectedCity, setSelectedCity] = useState(city || '');
  const [showDropdown, setShowDropdown] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();
  
  const cities = ['Pune', 'Nagpur', 'Gurugram'].sort();

  const filteredCities = cities.filter(city => 
    city.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const toggleDropdown = (e) => {
    console.log('Toggle dropdown clicked');
    e.stopPropagation();
    setShowDropdown(prev => !prev);
  };

  const handleCitySelect = async (cityName, e) => {
    console.log('City selected:', cityName);
    if (e) e.stopPropagation();
    
    try {
      console.log('Calling select city API...');
      await axios.post(`${process.env.REACT_APP_API_BASE_URL}/api/cities/select`, { city: cityName }, { withCredentials: true });
      
      // Update local state
      setSelectedCity(cityName);
      setShowDropdown(false);
      setSearchTerm('');
      
      // Notify parent component if needed
      if (onCityChange) {
        console.log('Notifying parent of city change:', cityName);
        onCityChange(cityName);
      }
      
      // Navigate to home if on city selection page
      if (window.location.pathname === '/select-city') {
        console.log('Navigating to home...');
        navigate('/');
      }
    } catch (error) {
      console.error('Error selecting city:', error);
    }
  };

  // Close dropdown when clicking outside
  React.useEffect(() => {
    const handleClickOutside = () => {
      console.log('Clicked outside dropdown');
      if (showDropdown) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, [showDropdown]);

  return (
    <Navbar bg="dark" variant="dark" expand="lg" className="mb-4">
      <Container>
        <Navbar.Brand as={Link} to="/" className="d-flex align-items-center">
          <FaTicketAlt className="me-2" />
          <span className="fw-bold">BookMyShow</span>
        </Navbar.Brand>
        
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/" className="px-3">Home</Nav.Link>
            <Nav.Link as={Link} to="/movies" className="px-3">Movies</Nav.Link>
          </Nav>
          
          <Nav className="align-items-center">
            <div className="position-relative">
              <div 
                className="d-flex align-items-center text-white me-3 cursor-pointer"
                onClick={toggleDropdown}
              >
                <FaMapMarkerAlt className="me-1 text-primary" />
                <span className="me-1">{selectedCity || 'Select City'}</span>
                <FaChevronDown size={12} className={showDropdown ? 'rotate-180' : ''} />
              </div>
              
              {showDropdown && (
                <div 
                  className="position-absolute top-100 end-0 mt-2 bg-white rounded shadow-lg" 
                  style={{ width: '300px', zIndex: 1050 }}
                  onClick={(e) => e.stopPropagation()}
                >
                  <div className="p-3 border-bottom">
                    <div className="position-relative">
                      <FaSearch className="position-absolute top-50 start-0 translate-middle-y ms-3 text-muted" />
                      <Form.Control
                        type="text"
                        placeholder="Search city..."
                        className="ps-5"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        autoFocus
                      />
                    </div>
                  </div>
                  <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {filteredCities.length > 0 ? (
                      filteredCities.map((cityName) => (
                        <div
                          key={cityName}
                          className={`px-4 py-2 cursor-pointer hover:bg-light ${selectedCity === cityName ? 'bg-light fw-bold' : ''}`}
                          onClick={(e) => {
                            console.log('City item clicked:', cityName);
                            handleCitySelect(cityName, e);
                          }}
                        >
                          {cityName}
                        </div>
                      ))
                    ) : (
                      <div className="px-4 py-3 text-muted text-center">
                        No cities found
                      </div>
                    )}
                  </div>
                </div>
              )}
              
              {showDropdown && (
                <div 
                  className="position-fixed top-0 start-0 w-100 h-100"
                  style={{ zIndex: 1040 }}
                  onClick={() => setShowDropdown(false)}
                />
              )}
            </div>
            
            <Nav.Link as={Link} to="/admin/login" className="btn btn-outline-light btn-sm">
              Admin Login
            </Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default UserHeader;
