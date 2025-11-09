# Contributing to TaskSphere

Thank you for your interest in contributing to TaskSphere! We welcome contributions from the community and are pleased to have you join us.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/yourusername/tasksphere.git`
3. Create a new branch: `git checkout -b feature/your-feature-name`
4. Follow the [installation instructions](README.md#installation) to set up your development environment

## How to Contribute

### Types of Contributions

We welcome several types of contributions:

- **Bug fixes**
- **New features**
- **Documentation improvements**
- **Test coverage improvements**
- **UI/UX enhancements**
- **Performance optimizations**

### Before You Start

1. Check existing [issues](https://github.com/yourusername/tasksphere/issues) and [pull requests](https://github.com/yourusername/tasksphere/pulls)
2. For major changes, please open an issue first to discuss what you would like to change
3. Make sure your contribution aligns with the project's goals and architecture

## Development Workflow

1. **Create a branch** from `main` with a descriptive name:
   ```bash
   git checkout -b feature/add-task-filtering
   git checkout -b fix/login-validation-bug
   git checkout -b docs/api-documentation-update
   ```

2. **Make your changes** following our coding standards

3. **Test your changes** thoroughly:
   ```bash
   # Backend tests
   cd backend && mvn test
   
   # Frontend tests
   cd frontend && npm test
   ```

4. **Commit your changes** with a clear commit message:
   ```bash
   git commit -m "feat: add advanced task filtering options"
   git commit -m "fix: resolve login validation issue"
   git commit -m "docs: update API documentation for new endpoints"
   ```

5. **Push to your fork** and create a pull request

## Coding Standards

### Backend (Java/Spring Boot)

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Follow Spring Boot conventions and best practices
- Use `@Service`, `@Repository`, `@Controller` annotations appropriately
- Add Swagger annotations for all API endpoints

### Frontend (React/TypeScript)

- Use TypeScript strict mode
- Follow React functional component patterns
- Use meaningful component and variable names
- Add proper type definitions
- Follow consistent file naming conventions
- Use Tailwind CSS for styling
- Add proper error handling and loading states

### Database

- Use snake_case for table and column names
- Create Flyway migration files for schema changes
- Include rollback scripts when possible
- Add proper indexes for performance
- Follow normalization principles

## Testing Guidelines

### Backend Testing

- Write unit tests for all service methods
- Add integration tests for API endpoints
- Use `@SpringBootTest` for integration tests
- Mock external dependencies
- Aim for >80% code coverage

### Frontend Testing

- Write unit tests for utility functions
- Add component tests using React Testing Library
- Test user interactions and edge cases
- Mock API calls in tests

## Documentation

- Update README.md for new features
- Add/update Swagger annotations for API changes
- Include code comments for complex logic
- Update configuration documentation
- Add screenshots for UI changes

## Reporting Issues

When reporting issues, please include:

1. **Clear title** and description
2. **Steps to reproduce** the issue
3. **Expected vs actual behavior**
4. **Environment details** (OS, Java version, browser, etc.)
5. **Screenshots** (for UI issues)
6. **Error logs** (if applicable)

### Issue Templates

Use our issue templates:
- Bug Report
- Feature Request
- Documentation Update
- Question/Support

## Pull Request Guidelines

### Before Submitting

- [ ] Tests pass locally
- [ ] Code follows our style guidelines
- [ ] Documentation is updated
- [ ] Commit messages are clear
- [ ] Branch is up to date with main

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
```

## Development Setup

### Required Tools

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+
- Git
- Your favorite IDE (IntelliJ IDEA, VS Code)

### Recommended IDE Extensions

**VS Code:**
- Extension Pack for Java
- React/TypeScript extensions
- Tailwind CSS IntelliSense
- REST Client (for API testing)

**IntelliJ IDEA:**
- Spring Boot plugin
- Lombok plugin
- Database Navigator

## Release Process

1. Version bump in `pom.xml` and `package.json`
2. Update CHANGELOG.md
3. Create release branch
4. Final testing
5. Merge to main
6. Create GitHub release
7. Deploy to production

## Getting Help

- [GitHub Discussions](https://github.com/yourusername/tasksphere/discussions)
- Email: dev@tasksphere.com
- Discord: [Join our server](#)

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Hall of Fame (coming soon)

Thank you for contributing to TaskSphere!