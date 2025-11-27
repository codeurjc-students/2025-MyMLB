/// <reference types="cypress" />

describe('Navbar Component E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth/me';

	beforeEach(() => {
		cy.intercept('GET', AUTH_API_URL, {
			statusCode: 200,
			body: {
				username: 'testuser',
				roles: ['GUEST', 'USER'],
			},
		}).as('getUser');

		cy.visit('/');
		cy.wait('@getUser');
	});

	it('renders logo and title correctly', () => {
		cy.get('header nav').should('exist');
		cy.get('img[alt="MLB Portal Logo"]').should('be.visible');
		cy.contains('MLB Portal').should('be.visible');
	});

	it('shows Home link and navigates correctly', () => {
		cy.contains('Home').should('have.attr', 'href').and('include', '/');
	});

	it('shows user-specific links when role is USER', () => {
		cy.contains('My Tickets').should('exist');
		cy.contains('Fav Teams').should('exist');
		cy.contains('Teams').should('exist');
		cy.contains('Standings').should('exist');
		cy.contains('Edit Info').should('not.exist');
	});

	it('should display the dropdown menu when hover over "Teams"', () => {
		cy.get('li.group')
			.contains(/^Teams$/)
			.trigger('mouseover');
		cy.get('app-dropdown-menu').should('exist');
	});

	it('does not show login/signup when authenticated', () => {
		cy.contains('Login').should('not.exist');
		cy.contains('Sign Up').should('not.exist');
	});

	it('toggles dark mode switch', () => {
		cy.get('input[type="checkbox"]').should('exist').check({ force: true });
		cy.get('input[type="checkbox"]').should('be.checked');
	});

	describe('NavbarComponent as GUEST', () => {
		beforeEach(() => {
			cy.intercept('GET', AUTH_API_URL, {
				statusCode: 200,
				body: {
					username: '',
					roles: ['GUEST'],
				},
			}).as('getGuest');

			cy.visit('/');
			cy.wait('@getGuest');
		});

		it('shows login and signup links for GUEST', () => {
			cy.contains('Login').should('exist');
			cy.contains('Sign Up').should('exist');
			cy.contains('My Tickets').should('not.exist');
			cy.contains('Fav Teams').should('not.exist');
			cy.contains('Edit Info').should('not.exist');
		});

		it('shows avatar and navigates to auth forms', () => {
			cy.get('img[alt="Avatar Profile"]').should('be.visible').parent('a').click();
			cy.url().should('include', '/auth');
		});
	});

	describe('NavbarComponent as ADMIN', () => {
		beforeEach(() => {
			cy.intercept('GET', AUTH_API_URL, {
				statusCode: 200,
				body: {
					username: 'adminuser',
					roles: ['ADMIN'],
				},
			}).as('getAdmin');

			cy.visit('/');
			cy.wait('@getAdmin');
		});

		it('shows admin-specific links', () => {
			cy.contains('Edit Info').should('exist');
			cy.contains('Matches').should('exist');
			cy.contains('Create Stadium').should('exist');
			cy.contains('Statistics').should('exist');
			cy.contains('Teams').should('not.exist');
			cy.contains('Standings').should('not.exist');
		});
	});
});