// Package declaration for the User entity model class
// This class represents a user in the authentication system
package com.subscription.customer.auth.model;

// Import statements for JPA (Java Persistence API) annotations
// JPA allows mapping Java objects to database tables
import jakarta.persistence.*;

// Import statements for Lombok annotations
// Lombok generates boilerplate code automatically (getters, setters, constructors, etc.)
import lombok.*;

// Import for Instant class (Java 8+ date/time API)
// Used for timestamp fields with nanosecond precision
import java.time.Instant;

// JPA Entity annotation marks this class as a database entity
// This tells JPA that this class should be mapped to a database table
@Entity

// Table annotation specifies the database table name
// Without this, JPA would use the class name "User" as table name
@Table(name = "users")

// Lombok annotations for automatic code generation:

// @Getter - Generates getter methods for all fields
// Example: public String getEmail() { return email; }
@Getter

// @Setter - Generates setter methods for all fields
// Example: public void setEmail(String email) { this.email = email; }
@Setter

// @NoArgsConstructor - Generates a no-argument constructor
// Required by JPA for entity instantiation
@NoArgsConstructor

// @AllArgsConstructor - Generates a constructor with all fields as parameters
// Useful for testing and object creation
@AllArgsConstructor

// @Builder - Generates a builder pattern for object construction
// Allows fluent API: User.builder().email("test").password("pass").build()
@Builder

public class User {

    // Primary key field with auto-generation strategy
    // IDENTITY strategy uses database auto-increment feature
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email field with database constraints
    // nullable = false means this field cannot be null in database
    // unique = true ensures no duplicate emails in the table
    @Column(nullable = false, unique = true)
    private String email;

    // User's display name
    @Column(nullable = false)
    private String name;

    // Password field - stored as hashed value (never plain text)
    // nullable = false ensures password is always required
    @Column(nullable = false)
    private String password;

    // Roles field for user authorization
    // Stores comma-separated role names (e.g., "ROLE_USER,ROLE_ADMIN")
    // Used by Spring Security for access control
    @Column(nullable = false)
    private String roles; // comma-separated roles, e.g. ROLE_USER,ROLE_ADMIN

    // User account status (ACTIVE, INACTIVE, SUSPENDED, etc.)
    // Default value is "ACTIVE" for new users
    @Column(nullable = false)
    private String status = "ACTIVE";

    // Account creation timestamp
    // Instant.now() sets current timestamp when object is created
    // No @Column annotation needed - uses default mapping
    private Instant createdAt = Instant.now();
}
