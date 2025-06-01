import React, { useState, useEffect } from 'react';
import { Card, Container, Row, Col, Spinner, Alert } from 'react-bootstrap';
import { FaFilm, FaTicketAlt, FaUsers, FaRupeeSign, FaChartLine } from 'react-icons/fa';
import { Link } from 'react-router-dom';

const AdminDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  // Mock data - in a real app, this would come from an API
  const mockStats = {
    totalMovies: 24,
    totalBookings: 1560,
    totalRevenue: 1250000,
    totalUsers: 845,
    recentBookings: [
      { id: 'BK1001', movie: 'Inception', user: 'user@example.com', amount: 1200, date: '2023-06-15' },
      { id: 'BK1002', movie: 'The Dark Knight', user: 'test@test.com', amount: 800, date: '2023-06-14' },
      { id: 'BK1003', movie: 'Interstellar', user: 'demo@demo.com', amount: 1000, date: '2023-06-14' },
    ],
    revenueData: {
      labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
      data: [650000, 590000, 800000, 810000, 1050000, 1250000],
    },
  };

  useEffect(() => {
    // Simulate API call
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        // In a real app: const data = await adminService.getDashboardStats();
        await new Promise(resolve => setTimeout(resolve, 800));
        setStats(mockStats);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
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
      </Container>
    );
  }


  return (
    <Container fluid className="py-4">
      <h1 className="mb-4">Admin Dashboard</h1>
      
      {/* Stats Cards */}
      <Row className="mb-4">
        <Col xl={3} md={6} className="mb-4">
          <Card className="border-left-primary shadow h-100 py-2">
            <Card.Body>
              <Row noGutters className="align-items-center">
                <div className="col mr-2">
                  <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                    Total Movies
                  </div>
                  <div className="h5 mb-0 font-weight-bold text-gray-800">
                    {stats.totalMovies}
                  </div>
                </div>
                <div className="col-auto">
                  <FaFilm size={40} className="text-gray-300" />
                </div>
              </Row>
            </Card.Body>
            <Card.Footer className="bg-light">
              <Link to="/admin/movies" className="text-decoration-none">
                <span className="text-primary small">View Details</span>
              </Link>
            </Card.Footer>
          </Card>
        </Col>

        <Col xl={3} md={6} className="mb-4">
          <Card className="border-left-success shadow h-100 py-2">
            <Card.Body>
              <Row noGutters className="align-items-center">
                <div className="col mr-2">
                  <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                    Total Bookings
                  </div>
                  <div className="h5 mb-0 font-weight-bold text-gray-800">
                    {stats.totalBookings.toLocaleString()}
                  </div>
                </div>
                <div className="col-auto">
                  <FaTicketAlt size={40} className="text-gray-300" />
                </div>
              </Row>
            </Card.Body>
            <Card.Footer className="bg-light">
              <Link to="/admin/bookings" className="text-decoration-none">
                <span className="text-success small">View Details</span>
              </Link>
            </Card.Footer>
          </Card>
        </Col>

        <Col xl={3} md={6} className="mb-4">
          <Card className="border-left-info shadow h-100 py-2">
            <Card.Body>
              <Row noGutters className="align-items-center">
                <div className="col mr-2">
                  <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                    Total Revenue
                  </div>
                  <div className="h5 mb-0 font-weight-bold text-gray-800">
                    {formatCurrency(stats.totalRevenue)}
                  </div>
                </div>
                <div className="col-auto">
                  <FaRupeeSign size={40} className="text-gray-300" />
                </div>
              </Row>
            </Card.Body>
            <Card.Footer className="bg-light">
              <Link to="/admin/reports" className="text-decoration-none">
                <span className="text-info small">View Reports</span>
              </Link>
            </Card.Footer>
          </Card>
        </Col>

        <Col xl={3} md={6} className="mb-4">
          <Card className="border-left-warning shadow h-100 py-2">
            <Card.Body>
              <Row noGutters className="align-items-center">
                <div className="col mr-2">
                  <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                    Total Users
                  </div>
                  <div className="h5 mb-0 font-weight-bold text-gray-800">
                    {stats.totalUsers}
                  </div>
                </div>
                <div className="col-auto">
                  <FaUsers size={40} className="text-gray-300" />
                </div>
              </Row>
            </Card.Body>
            <Card.Footer className="bg-light">
              <Link to="/admin/users" className="text-decoration-none">
                <span className="text-warning small">View Users</span>
              </Link>
            </Card.Footer>
          </Card>
        </Col>
      </Row>

      {/* Recent Bookings */}
      <Row className="mb-4">
        <Col md={8}>
          <Card className="shadow">
            <Card.Header className="py-3 d-flex justify-content-between align-items-center">
              <h6 className="m-0 font-weight-bold text-primary">Recent Bookings</h6>
              <Link to="/admin/bookings" className="btn btn-sm btn-primary">View All</Link>
            </Card.Header>
            <Card.Body>
              <div className="table-responsive">
                <table className="table table-hover">
                  <thead>
                    <tr>
                      <th>Booking ID</th>
                      <th>Movie</th>
                      <th>User</th>
                      <th>Amount</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats.recentBookings.map((booking) => (
                      <tr key={booking.id}>
                        <td>{booking.id}</td>
                        <td>{booking.movie}</td>
                        <td>{booking.user}</td>
                        <td>₹{booking.amount}</td>
                        <td>{new Date(booking.date).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card.Body>
          </Card>
        </Col>

        {/* Revenue Chart */}
        <Col md={4}>
          <Card className="shadow h-100">
            <Card.Header className="py-3">
              <h6 className="m-0 font-weight-bold text-primary">Revenue Overview</h6>
            </Card.Header>
            <Card.Body>
              <div className="chart-pie pt-4">
                <div className="text-center">
                  <FaChartLine size={40} className="text-primary mb-3" />
                  <p>Revenue growth over the last 6 months</p>
                  <div className="mt-3">
                    {stats.revenueData.labels.map((month, index) => (
                      <div key={month} className="d-flex justify-content-between mb-1">
                        <span>{month}</span>
                        <span>₹{(stats.revenueData.data[index] / 1000).toFixed(0)}K</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Quick Actions */}
      <Row>
        <Col>
          <Card className="shadow mb-4">
            <Card.Header className="py-3">
              <h6 className="m-0 font-weight-bold text-primary">Quick Actions</h6>
            </Card.Header>
            <Card.Body>
              <div className="d-flex flex-wrap gap-3">
                <Link to="/admin/movies/add" className="btn btn-primary">
                  Add New Movie
                </Link>
                <Link to="/admin/shows/add" className="btn btn-success">
                  Add New Show
                </Link>
                <Link to="/admin/theatres" className="btn btn-info text-white">
                  Manage Theatres
                </Link>
                <Link to="/admin/users" className="btn btn-warning">
                  Manage Users
                </Link>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default AdminDashboard;
