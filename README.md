# Enterprise Project Management Tool

A comprehensive Java-based desktop application designed to facilitate efficient project collaboration, task tracking, and team management within organizations. Built with role-based access control and enterprise-grade security features.

## Checkout the master branch for the code.

## 🚀 Key Features

### Multi-Role Access Control
- **Admin Dashboard**: Complete system control with user, department, and client management
- **Manager Dashboard**: Project oversight, team management, and employee time tracking
- **Supervisor Dashboard**: Project creation, milestone management, and task assignment
- **Team Member Dashboard**: Task execution, time logging, and project visibility

### Core Functionality
- **Secure Authentication**: Email/password login with role-based access and password recovery
- **Project Management**: End-to-end project lifecycle management with milestone tracking
- **Task Assignment**: Dynamic task allocation with priority levels and time logging
- **Team Collaboration**: Multi-user workspace with seamless collaboration features
- **Time Tracking**: Comprehensive time logging system for productivity monitoring

### Technical Excellence
- **Database Security**: All operations through stored procedures preventing SQL injection
- **Transactional Integrity**: ACID compliance ensuring data consistency
- **Normalized Schema**: Optimized MySQL database design with proper relationships
- **Responsive UI**: Clean Java Swing interface with consistent user experience

## 🛠 Technology Stack

**Frontend**
- Java Swing GUI with GridBagLayout
- Event-driven architecture
- Custom dialog components
- IntelliJ IDEA development environment

**Backend & Database**
- MySQL 8.0+ with JDBC connectivity
- Stored procedures for business logic
- Parameterized queries for security
- Connection pooling for performance

**Security Features**
- Password hashing and validation
- Input sanitization and validation
- Role-based permissions system
- Database credential separation

## 📊 Database Schema Overview

### Core Entities
- **Employees**: User accounts with role-based permissions
- **Departments**: Organizational units containing teams
- **Teams**: Project groups with assigned members
- **Projects**: Main work units with milestones and tasks
- **Tasks**: Individual work items with priority and time tracking
- **Time Logs**: Employee work hour tracking system

### Key Relationships
- Employee ↔ Department (Many-to-One)
- Department ↔ Teams (One-to-Many)
- Teams ↔ Employees (Many-to-Many)
- Projects ↔ Milestones (One-to-Many)
- Projects ↔ Tasks (One-to-Many)
- Tasks ↔ Time Logs (One-to-Many)

## 👥 Role-Based Permissions

### Administrator
- ✅ User management (CRUD operations)
- ✅ Department management
- ✅ Client management
- ✅ System-wide oversight
- ❌ Direct project/task management

### Manager
- ✅ Team creation and management
- ✅ Employee time log tracking
- ✅ Project visibility (read-only)
- ✅ Performance monitoring
- ❌ Task creation/modification

### Supervisor
- ✅ Project creation and management
- ✅ Milestone setting and tracking
- ✅ Task assignment and monitoring
- ✅ Team member oversight
- ❌ System administration

### Team Member
- ✅ Task viewing and updates
- ✅ Time logging for assigned tasks
- ✅ Project visibility (assigned projects)
- ✅ Personal productivity tracking
- ❌ Administrative functions

## 🔧 Architecture Highlights

### Security Implementation
- All database operations through stored procedures
- Input validation and sanitization
- Role-based access control at UI level
- Password hashing with secure algorithms

### Code Organization
- Modular dashboard architecture
- Reusable UI components
- Centralized error handling
- Clean separation of concerns

**Built with Java ☕ | Powered by MySQL 🐬 | Designed for Enterprise 🏢**
