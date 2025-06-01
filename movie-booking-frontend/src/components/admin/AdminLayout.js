import React, { useState, useEffect } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { 
  Container, 
  Navbar, 
  Nav, 
  Dropdown, 
  Badge,
  Button,
  Offcanvas,
  Image
} from 'react-bootstrap';
import { 
  FaBars, 
  FaHome, 
  FaFilm, 
  FaTicketAlt, 
  FaUsers, 
  FaBuilding, 
  FaChartLine, 
  FaCog, 
  FaSignOutAlt,
  FaUserCircle,
  FaBell,
  FaTimes
} from 'react-icons/fa';
import { useAuth } from '../../context/AuthContext';
// Using a placeholder logo instead of the local file
const logo = 'https://via.placeholder.com/150x50?text=Movie+Booking';

const AdminLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showSidebar, setShowSidebar] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);

  // Toggle sidebar on mobile
  const toggleSidebar = () => setShowSidebar(!showSidebar);
  
  // Close sidebar when route changes
  useEffect(() => {
    setShowSidebar(false);
  }, [location.pathname]);

  // Mock notifications
  useEffect(() => {
    // In a real app, fetch notifications from API
    const mockNotifications = [
      { id: 1, message: 'New booking received for Inception', time: '2 minutes ago', read: false },
      { id: 2, message: '5 new users registered today', time: '1 hour ago', read: true },
      { id: 3, message: 'Scheduled maintenance tonight at 2 AM', time: '3 hours ago', read: true },
    ];
    setNotifications(mockNotifications);
  }, []);

  const unreadCount = notifications.filter(n => !n.read).length;

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  // Check if current route is active
  const isActive = (path) => {
    return location.pathname.startsWith(path);
  };

  // Nav items for sidebar
  const navItems = [
    { to: '/admin', icon: <FaHome className="me-2" />, label: 'Dashboard', exact: true },
    { 
      to: '/admin/movies', 
      icon: <FaFilm className="me-2" />, 
      label: 'Movies',
      children: [
        { to: '/admin/movies', label: 'All Movies' },
        { to: '/admin/movies/new', label: 'Add New Movie' }
      ]
    },
    { to: '/admin/shows', icon: <FaTicketAlt className="me-2" />, label: 'Shows' },
    { to: '/admin/theatres', icon: <FaBuilding className="me-2" />, label: 'Theatres' },
    { to: '/admin/bookings', icon: <FaTicketAlt className="me-2" />, label: 'Bookings' },
    { to: '/admin/users', icon: <FaUsers className="me-2" />, label: 'Users' },
    { to: '/admin/reports', icon: <FaChartLine className="me-2" />, label: 'Reports' }
  ];

  // Check if a nav item or any of its children is active
  const isNavItemActive = (item) => {
    if (item.exact) {
      return location.pathname === item.to;
    }
    
    if (item.children) {
      return item.children.some(child => 
        location.pathname.startsWith(child.to)
      );
    }
    
    return location.pathname.startsWith(item.to);
  };
  
  // Check if a child nav item is active
  const isChildActive = (child) => {
    return location.pathname === child.to;
  };

  return (
    <div className="d-flex" style={{ minHeight: '100vh' }}>
      {/* Desktop Sidebar */}
      <div className="d-none d-lg-block bg-dark text-white" style={{ width: '250px' }}>
        <div className="p-3 border-bottom border-secondary">
          <Link to="/admin" className="text-decoration-none text-white d-flex align-items-center">
            <Image src={logo} alt="Logo" height="40" className="me-2" />
            <span className="h5 mb-0">Admin Panel</span>
          </Link>
        </div>
        
        <div className="p-3">
          <div className="text-muted small mb-2">MAIN NAVIGATION</div>
          <Nav className="flex-column">
            {navItems.map((item) => (
              <React.Fragment key={item.to}>
                <Nav.Link 
                  as={Link} 
                  to={item.to} 
                  className={`d-flex align-items-center justify-content-between py-2 px-3 mb-1 rounded ${isNavItemActive(item) ? 'bg-primary text-white' : 'text-white-50 hover-bg-dark'}`}
                >
                  <div className="d-flex align-items-center">
                    {item.icon}
                    <span className="ms-2">{item.label}</span>
                  </div>
                  {item.children && (
                    <span className="ms-2">
                      {isNavItemActive(item) ? '▼' : '▶'}
                    </span>
                  )}
                </Nav.Link>
                
                {item.children && isNavItemActive(item) && (
                  <div className="ms-4 mb-2">
                    {item.children.map(child => (
                      <Nav.Link
                        key={child.to}
                        as={Link}
                        to={child.to}
                        className={`d-block py-1 px-2 small rounded ${isChildActive(child) ? 'text-white' : 'text-white-50 hover-bg-dark'}`}
                      >
                        {child.label}
                      </Nav.Link>
                    ))}
                  </div>
                )}
              </React.Fragment>
            ))}
          </Nav>
          
          <div className="mt-4 pt-3 border-top border-secondary">
            <Button 
              variant="outline-light" 
              size="sm" 
              className="w-100 d-flex align-items-center justify-content-center"
              onClick={handleLogout}
            >
              <FaSignOutAlt className="me-2" />
              Logout
            </Button>
          </div>
        </div>
      </div>

      {/* Mobile Offcanvas Sidebar */}
      <Offcanvas show={showSidebar} onHide={toggleSidebar} className="bg-dark text-white" style={{ width: '250px' }}>
        <Offcanvas.Header closeButton closeVariant="white" className="border-bottom border-secondary">
          <Offcanvas.Title>
            <Link to="/admin" className="text-decoration-none text-white d-flex align-items-center">
              <Image src={logo} alt="Logo" height="30" className="me-2" />
              <span className="h5 mb-0">Admin Panel</span>
            </Link>
          </Offcanvas.Title>
        </Offcanvas.Header>
        <Offcanvas.Body className="p-0">
          <div className="p-3">
            <Nav className="flex-column">
              {navItems.map((item) => (
                <Nav.Link 
                  key={item.to} 
                  as={Link} 
                  to={item.to}
                  className={`text-white mb-1 rounded ${isActive(item.to) ? 'bg-primary' : 'hover-bg-dark'}`}
                  onClick={toggleSidebar}
                >
                  {item.icon}
                  {item.label}
                </Nav.Link>
              ))}
            </Nav>
            
            <div className="mt-4 pt-3 border-top border-secondary">
              <Button 
                variant="outline-light" 
                size="sm" 
                className="w-100 d-flex align-items-center justify-content-center"
                onClick={handleLogout}
              >
                <FaSignOutAlt className="me-2" />
                Logout
              </Button>
            </div>
          </div>
        </Offcanvas.Body>
      </Offcanvas>

      {/* Main Content */}
      <div className="flex-grow-1 d-flex flex-column" style={{ backgroundColor: '#f8f9fa' }}>
        {/* Top Navigation */}
        <Navbar bg="white" className="shadow-sm" expand="lg">
          <Container fluid>
            <Button 
              variant="link" 
              className="d-lg-none p-0 me-3 text-dark"
              onClick={toggleSidebar}
            >
              <FaBars size={20} />
            </Button>
            
            <div className="d-flex align-items-center ms-auto">
              {/* Notifications */}
              <div className="position-relative me-3">
                <Button 
                  variant="link" 
                  className="position-relative p-0 text-dark"
                  onClick={() => setShowNotifications(!showNotifications)}
                >
                  <FaBell size={20} />
                  {unreadCount > 0 && (
                    <Badge 
                      pill 
                      bg="danger" 
                      className="position-absolute top-0 start-100 translate-middle"
                      style={{ fontSize: '0.6rem' }}
                    >
                      {unreadCount}
                    </Badge>
                  )}
                </Button>
                
                {/* Notifications Dropdown */}
                {showNotifications && (
                  <div 
                    className="position-absolute end-0 mt-2 bg-white rounded shadow-lg" 
                    style={{ width: '320px', zIndex: 1050 }}
                  >
                    <div className="d-flex justify-content-between align-items-center p-3 border-bottom">
                      <h6 className="mb-0">Notifications</h6>
                      <Button 
                        variant="link" 
                        className="p-0 text-dark"
                        onClick={() => setShowNotifications(false)}
                      >
                        <FaTimes />
                      </Button>
                    </div>
                    <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                      {notifications.length > 0 ? (
                        notifications.map(notification => (
                          <div 
                            key={notification.id} 
                            className={`p-3 border-bottom ${!notification.read ? 'bg-light' : ''}`}
                          >
                            <div className="d-flex justify-content-between">
                              <p className="mb-1">{notification.message}</p>
                              {!notification.read && (
                                <span className="badge bg-primary">New</span>
                              )}
                            </div>
                            <small className="text-muted">{notification.time}</small>
                          </div>
                        ))
                      ) : (
                        <div className="p-4 text-center text-muted">
                          No new notifications
                        </div>
                      )}
                    </div>
                    <div className="p-2 text-center border-top">
                      <Button variant="link" size="sm" className="text-primary">
                        View All Notifications
                      </Button>
                    </div>
                  </div>
                )}
              </div>
              
              {/* User Dropdown */}
              <Dropdown align="end">
                <Dropdown.Toggle 
                  variant="link" 
                  id="user-dropdown" 
                  className="d-flex align-items-center text-decoration-none p-0"
                >
                  <div className="d-flex align-items-center">
                    <div className="me-2 text-end d-none d-sm-block">
                      <div className="fw-medium">{user?.name || 'Admin User'}</div>
                      <small className="text-muted">Administrator</small>
                    </div>
                    <div className="rounded-circle bg-secondary d-flex align-items-center justify-content-center" style={{ width: '40px', height: '40px' }}>
                      <FaUserCircle size={32} className="text-white" />
                    </div>
                  </div>
                </Dropdown.Toggle>

                <Dropdown.Menu className="dropdown-menu-end">
                  <Dropdown.Item as={Link} to="/admin/profile">
                    <FaUserCircle className="me-2" /> Profile
                  </Dropdown.Item>
                  <Dropdown.Item as={Link} to="/admin/settings">
                    <FaCog className="me-2" /> Settings
                  </Dropdown.Item>
                  <Dropdown.Divider />
                  <Dropdown.Item onClick={handleLogout}>
                    <FaSignOutAlt className="me-2" /> Logout
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            </div>
          </Container>
        </Navbar>

        {/* Page Content */}
        <main className="flex-grow-1 p-4">
          <Outlet />
        </main>

        {/* Footer */}
        <footer className="bg-white py-3 border-top">
          <Container>
            <div className="text-center text-muted">
              <small>
                &copy; {new Date().getFullYear()} Movie Booking Admin Panel. All rights reserved.
              </small>
            </div>
          </Container>
        </footer>
      </div>
    </div>
  );
};

export default AdminLayout;
