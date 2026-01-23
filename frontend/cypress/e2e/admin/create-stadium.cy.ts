/// <reference types="cypress" />

describe('Create Stadium Component E2E Tests', () => {
	const API_URL = '/api/v1/stadiums';

	beforeEach(() => {
		cy.viewport(1280, 720);
		cy.intercept('GET', '/api/v1/auth/me', {
			statusCode: 200,
			body: { username: 'testUser', roles: ['ADMIN'] },
		}).as('getAdmin');

		cy.visit('/create-stadium');
		cy.wait('@getAdmin');
	});

	it('should display the creation form', () => {
		cy.contains('Create Stadium').should('be.visible');
		cy.get('input[placeholder="e.g., Shippett Stadium"]').should('exist');
		cy.get('input[placeholder="e.g., 1923"]').should('exist');
	});

	it('should create the stadium successfully', () => {
		cy.intercept('POST', API_URL, {
			statusCode: 200,
			body: { name: 'Fenway Park', openingDate: 1912, pictures: [] },
		}).as('createStadium');

		cy.get('input[placeholder="e.g., Shippett Stadium"]').type('Fenway Park');
		cy.get('input[placeholder="e.g., 1923"]').type('1912');

		cy.get('app-action-buttons button').contains('Confirm').click();

		cy.wait('@createStadium');
		cy.get('app-success-modal').should('contain.text', 'Fenway Park successfully created');
	});

	it('should navigate back to home when clicking goBack', () => {
		cy.get('app-action-buttons button').contains('Go Back').click();
		cy.url().should('include', '/');
	});
});