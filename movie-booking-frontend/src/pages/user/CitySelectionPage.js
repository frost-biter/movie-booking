import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { FaMapMarkerAlt } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

const CitySelectionPage = ({ onSelectCity }) => {
  const navigate = useNavigate();
  const cities = ['Pune', 'Nagpur', 'Gurugram'].sort();

  const handleCitySelect = (city) => {
    onSelectCity(city);
    navigate('/');
  };

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col md={8} lg={6}>
            <Card className="shadow-sm">
              <Card.Body className="p-4">
                <div className="text-center mb-4">
                  <div className="bg-primary bg-opacity-10 d-inline-flex p-3 rounded-circle mb-3">
                    <FaMapMarkerAlt size={32} className="text-primary" />
                  </div>
                  <h2>Select Your City</h2>
                  <p className="text-muted">Choose your city to see movies and shows near you</p>
                </div>
                
                <div className="d-grid gap-3">
                  {cities.map((city) => (
                    <Button
                      key={city}
                      variant="outline-primary"
                      size="lg"
                      className="text-start py-3 d-flex align-items-center"
                      onClick={() => handleCitySelect(city)}
                    >
                      <FaMapMarkerAlt className="me-3" />
                      {city}
                    </Button>
                  ))}
                </div>
                
                <div className="text-center mt-4">
                  <p className="text-muted mb-0">
                    <small>Admin? <a href="/admin/login" className="text-decoration-none">Login here</a></small>
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default CitySelectionPage;
