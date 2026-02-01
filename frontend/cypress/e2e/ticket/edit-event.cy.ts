/// <reference types="cypress" />

describe('Edit Event E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const EVENT_API_URL = '**/api/v1/events';
	const eventId = 123;

	const mockEventResponse = {
		id: eventId,
		awayTeamName: 'Boston Red Sox',
		homeTeamName: 'New York Yankees',
		stadiumName: 'Yankee Stadium',
		date: '2026-05-20T21:00:00Z',
		sectors: [
			{
				sectorId: 10,
				sectorName: 'North Stand',
				price: 80,
				availability: 50,
				totalCapacity: 100,
			},
			{
				sectorId: 11,
				sectorName: 'South Stand',
				price: 85,
				availability: 20,
				totalCapacity: 100,
			},
		],
	};

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

		cy.intercept('GET', `${EVENT_API_URL}/${eventId}`, {
			statusCode: 200,
			body: mockEventResponse,
		}).as('getEvent');

		cy.visit(`/edit-event?eventId=${eventId}`);
		cy.wait('@getEvent');
	});

	it('should display the event data accordingly', () => {
		cy.contains('Boston Red Sox').should('be.visible');
		cy.contains('New York Yankees').should('be.visible');

		cy.get('h3').contains('North Stand').should('be.visible');
		cy.get('h3').contains('South Stand').should('be.visible');

		cy.get('.bg-emerald-500').first().should('have.attr', 'style').and('include', 'width: 50%');
	});

	it('should edit the event successfully', () => {
		cy.intercept('PUT', EVENT_API_URL, {
			statusCode: 200,
			body: { message: 'Updated' },
		}).as('updateRequest');

		cy.get('input[type="number"]').first().clear().type('95');

		cy.get('input[type="number"]').last().clear().type('110');

		cy.contains('button', 'Save Changes').click();

		cy.wait('@updateRequest').then((interception) => {
			const body = interception.request.body;
			expect(body.eventId).to.equal(eventId);
			expect(body.sectorIds).to.deep.equal([10, 11]);
			expect(body.prices).to.deep.equal([95, 110]);
		});

		cy.get('app-success-modal').should('be.visible');
		cy.contains('Event Successfully Modified').should('be.visible');

		cy.contains('button', 'Continue').click();
		cy.url().should('eq', Cypress.config().baseUrl + '/');
	});
});