import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-dark text-light py-4 mt-4">
      <Container>
        <Row>
          <Col md={4}>
            <h5>About Us</h5>
            <p>Your one-stop destination for movie tickets, events, and more.</p>
          </Col>
          <Col md={4}>
            <h5>Quick Links</h5>
            <ul className="list-unstyled">
              <li><Link to="/" className="text-light">Home</Link></li>
              <li><Link to="/movies" className="text-light">Movies</Link></li>
              <li><Link to="/contact" className="text-light">Contact Us</Link></li>
            </ul>
          </Col>
          <Col md={4}>
            <h5>Contact</h5>
            <address>
              Email: support@bookmyshow.com<br />
              Phone: +1 234 567 8900
            </address>
          </Col>
        </Row>
        <hr className="bg-light" />
        <div className="text-center">
          <p className="mb-0">&copy; {currentYear} BookMyShow. All rights reserved.</p>
        </div>
      </Container>
    </footer>
  );
};

export default Footer;
