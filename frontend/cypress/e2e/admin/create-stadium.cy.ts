/// <reference types="cypress" />

describe('Create Stadium Component E2E Tests', () => {
	const API_URL = '/api/v1/stadiums';

	beforeEach(() => {
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

	it('should create stadium successfully', () => {
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

	it('should show error when fields are empty', () => {
		cy.intercept('POST', API_URL, {
			statusCode: 400,
			body: { message: 'All the fields are required' },
		}).as('createStadium');

		cy.get('app-action-buttons button').contains('Confirm').click();

		cy.wait('@createStadium');
		cy.get('app-error-modal').should('contain.text', 'All the fields are required');
	});

	it('should show error when stadium already exists', () => {
		cy.intercept('POST', API_URL, {
			statusCode: 400,
			body: { message: 'A stadium with this name already exists' },
		}).as('createStadium');

		cy.get('input[placeholder="e.g., Shippett Stadium"]').type('Yankee Stadium');
		cy.get('input[placeholder="e.g., 1923"]').type('2009');

		cy.get('app-action-buttons button').contains('Confirm').click();

		cy.wait('@createStadium');
		cy.get('app-error-modal').should('contain.text', 'A stadium with this name already exists');
	});

	it('should navigate back to home when clicking goBack', () => {
		cy.get('app-action-buttons button').contains('Go Back').click();
		cy.url().should('include', '/');
	});
});