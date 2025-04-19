# BookMyShow - Movie Ticket Booking System

A Spring Boot-based movie ticket booking system that allows users to browse movies, book tickets, and manage bookings.

## Features

- User Authentication and Authorization
- Movie Management
- Theatre and Screen Management
- Show Scheduling
- Seat Selection and Booking
- Payment Processing
- Ticket Generation
- Admin Dashboard

## Tech Stack

- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Database**: PostgreSQL
- **Caching**: Redis
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- PostgreSQL 14 or higher
- Redis 6.0 or higher
- Maven 3.8 or higher

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/bookMyShow.git
cd bookMyShow
```

2. Configure the database:
- Create a PostgreSQL database named `bookMyShow`
- Update the database configuration in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookMyShow
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

3. Configure Redis:
- Update Redis configuration in `application.properties`:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=10000
```

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn spring-boot:run
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/movie/bookMyShow/
│   │       ├── config/         # Configuration classes
│   │       ├── controller/     # REST controllers
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── exception/     # Custom exceptions
│   │       ├── model/         # Entity classes
│   │       ├── repo/          # Repository interfaces
│   │       ├── security/      # Security configuration
│   │       └── service/       # Business logic
│   └── resources/
│       ├── application.properties
│       └── schema.sql
└── test/                      # Test classes
```

## Key Features Implementation

### Booking Flow
1. User selects seats for a show
2. System holds seats temporarily
3. User completes payment
4. System creates booking and generates ticket
5. User receives confirmation

### Security
- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Password encryption
- CSRF protection

### Caching
- Redis used for:
  - Seat availability
  - Show details
  - User sessions

## Testing

Run tests using:
```bash
mvn test
```

The project includes:
- Unit tests for services
- Integration tests for controllers
- Test configuration with H2 database

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring Boot team
- Redis team
- PostgreSQL team
- All contributors to this project 
