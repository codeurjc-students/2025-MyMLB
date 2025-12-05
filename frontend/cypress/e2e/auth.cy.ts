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
	});

	describe('Registration', () => {
		it('should successfully register a new user', () => {
			cy.intercept('POST', '/api/v1/auth/register', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'User registered successfully' },
			}).as('registerRequest');

			cy.get('app-login').contains('button', 'SIGN UP').click();
			fillRegisterForm('test@gmail.com', 'testUser', 'test');
			cy.get('app-register').contains('button', 'SIGN UP').click({ force: true });

			cy.wait('@registerRequest');
			cy.get('app-register').contains('User registered successfully').should('exist');
		});
	});

	describe('Login', () => {
		it('should successfully login a user after registration', () => {
			cy.intercept('POST', '/api/v1/auth/login', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'Login successful' },
			}).as('loginRequest');

			fillLoginForm('testUser', 'test');
			cy.get('app-login button').contains('SIGN IN').click();

			cy.wait('@loginRequest');
			cy.url().should('eq', Cypress.config().baseUrl + '/');
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