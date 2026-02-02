/// <reference types="cypress" />

describe('Create Event E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const MATCH_API_URL = '**/api/v1/matches';
	const EVENT_API_URL = '**/api/v1/events';
	const matchId = 55;

	beforeEach(() => {
		cy.intercept('GET', `${AUTH_API_URL}/me`, {
			statusCode: 200,
			body: {
				username: 'testUser',
				roles: ['ADMIN'],
			},
		}).as('getActiveUser');

		cy.visit('/');
		cy.wait('@getActiveUser');

		cy.intercept('GET', `${MATCH_API_URL}/${matchId}`, {
			statusCode: 200,
			body: {
				id: matchId,
				awayTeam: { name: 'Los Angeles Dodgers' },
				homeTeam: { name: 'Los Angeles Angels' },
				stadiumName: 'Dodger Stadium',
				date: '2026-07-15T20:00:00Z',
			},
		}).as('getMatch');

		cy.visit(`/create-event?matchId=${matchId}`);
		cy.wait('@getMatch');
	});

	it('should display match information correctly', () => {
		cy.contains('Dodgers').should('be.visible');
		cy.contains('Angels').should('be.visible');
		cy.contains('Dodger Stadium').should('be.visible');
	});

	it('should create an event successfully', () => {
		cy.get('input[type="number"]').first().clear().type('2').trigger('change');

		const sectorNames = ['Grandstand', 'VIP Lounge'];
		sectorNames.forEach((name) => {
			cy.get('input[placeholder*="Type & Enter"]').type(`${name}{enter}`);
		});

		const prices = ['50', '150'];
		prices.forEach((price) => {
			cy.get('input[placeholder="$ 0.00"]').type(`${price}{enter}`);
		});

		const capacities = ['500', '100'];
		capacities.forEach((cap) => {
			cy.get('input[placeholder="e.g 100"]').type(`${cap}{enter}`);
		});

		cy.contains('Ready to generate event').should('be.visible');
		cy.contains('button', 'Generate Event').should('not.be.disabled');

		cy.intercept('POST', EVENT_API_URL, {
			statusCode: 201,
			body: { id: 100 },
		}).as('createRequest');

		cy.contains('button', 'Generate Event').click();
		cy.wait('@createRequest');

		cy.get('app-success-modal').should('be.visible');
		cy.contains('Event Successfully Created').should('be.visible');

		cy.contains('button', 'Continue').click();
		cy.url().should('eq', Cypress.config().baseUrl + '/');
	});

	it('should allow removing a sector tag', () => {
		cy.get('input[type="number"]').first().type('1').trigger('change');

		cy.get('input[placeholder*="Type & Enter"]').type('Delete Me{enter}');
		cy.contains('span', 'Delete Me').within(() => {
			cy.get('button').click();
		});

		cy.contains('span', 'Delete Me').should('not.exist');
	});
});