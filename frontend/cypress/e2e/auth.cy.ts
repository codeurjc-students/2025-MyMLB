/// <reference types="cypress" />

describe('Auth Forms E2E Tests', () => {
	beforeEach(() => {
		cy.visit('/auth');
	});

	const fillRegisterForm = (email: string, username: string, password: string) => {
		cy.get('app-register input[formcontrolname=email]').type(email, { force: true });
		cy.get('app-register input[formcontrolname=username]').type(username, { force: true });
		cy.get('app-register input[formcontrolname=password]').type(password, { force: true });
	};

	const fillLoginForm = (username: string, password: string) => {
		cy.get('app-login input[formcontrolname=username]').type(username);
		cy.get('app-login input[formcontrolname=password]').type(password);
	};

	describe('Form Switching', () => {
		it('should switch to the register form', () => {
			cy.contains('button', 'SIGN UP').click();
			cy.get('app-register').should('exist');
			cy.get('h2').should('contain.text', 'Sign Up').and('be.visible');
		});

		it('should switch to the login form', () => {
			cy.contains('button', 'SIGN IN').click();
			cy.get('app-login').should('exist');
			cy.get('h2').should('contain.text', 'Sign In').and('be.visible');
		});
	});

	describe('Registration', () => {
		it('should successfully register a new user', () => {
			cy.intercept('POST', '/api/auth/register', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'User registered successfully' },
			}).as('registerRequest');

			cy.get('app-login').contains('button', 'SIGN UP').click();
			fillRegisterForm('test@gmail.com', 'testUser', 'test');
			cy.get('app-register').contains('button', 'SIGN UP').click({ force: true });

			cy.wait('@registerRequest');
			cy.get('app-register').contains('User registered successfully').should('exist');
		});

		it('should show an error on invalid registration', () => {
			cy.intercept('POST', '/api/auth/register', {
				statusCode: 409,
				body: { status: 'FAILURE', message: 'User already exists' },
			}).as('registerRequest');

			cy.contains('button', 'SIGN UP').click();
			fillRegisterForm('testExistingUser@gmail.com', 'testExistingUser', 'test');
			cy.get('app-register button').contains('SIGN UP').click({ force: true });

			cy.wait('@registerRequest');
			cy.get('app-register').contains('User already exists').should('exist');
		});
	});

	describe('Login', () => {
		it('should successfully login a user after registration', () => {
			cy.intercept('POST', '/api/auth/login', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'Login successful' },
			}).as('loginRequest');

			fillLoginForm('testUser', 'test');
			cy.get('app-login button').contains('SIGN IN').click();

			cy.wait('@loginRequest');
			cy.url().should('eq', Cypress.config().baseUrl + '/');
		});

		it('should show error on invalid login', () => {
			cy.intercept('POST', '/api/auth/login', {
				statusCode: 401,
				body: { status: 'FAILURE', message: 'Invalid Credentials' },
			}).as('loginFail');

			fillLoginForm('wrongUser', 'wrongPass');
			cy.get('app-login button').contains('SIGN IN').click();

			cy.wait('@loginFail');
			cy.get('app-login').contains('Invalid Credentials').should('be.visible');
		});
	});

	describe('Redirect to Password Recovery', () => {
		it('should navigate to /recovery and show the Password Recovery Forms', () => {
			cy.get('app-login').contains('a', 'Forgot Your Password?').click();
			cy.url().should('include', '/recovery');
			cy.contains('h2', 'Password Recovery').should('be.visible');
		});
	});
});