/// <reference types="cypress" />

describe('Ticket Selection E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const matchId = 1;
	const eventId = 10;

	beforeEach(() => {
		cy.intercept('GET', `${AUTH_API_URL}/me`, {
			statusCode: 200,
			body: {
				username: 'testUser',
				roles: ['GUEST', 'USER'],
			},
		}).as('getActiveUser');

		cy.visit('/');
		cy.wait('@getActiveUser');

		cy.intercept('GET', `**/api/v1/events/match/${matchId}`, {
			statusCode: 200,
			body: {
				id: eventId,
				awayTeamName: 'Boston Red Sox',
				homeTeamName: 'New York Yankees',
				stadiumName: 'Yankee Stadium',
				date: new Date(),
				pictureMap: { url: 'https://example.com/map.jpg' },
			},
		}).as('getEvent');

		cy.intercept('GET', `**/api/v1/events/${eventId}/sectors`, {
			statusCode: 200,
			body: {
				content: [
					{ id: 1, sectorName: 'North Stand', price: 100 },
					{ id: 2, sectorName: 'South Stand', price: 150 },
				],
			},
		}).as('getSectors');

		cy.intercept('GET', `**/api/v1/events/${eventId}/sector/1`, {
			statusCode: 200,
			body: {
				content: [
					{ id: 101, name: 'A-1' },
					{ id: 102, name: 'A-2' },
					{ id: 103, name: 'A-3' },
				],
			},
		}).as('getSeats');

		cy.visit(`/tickets?matchId=${matchId}`);
		cy.wait(['@getEvent', '@getSectors']);
	});

	it('should display event information correctly', () => {
		cy.contains('Boston Red Sox').should('be.visible');
		cy.contains('New York Yankees').should('be.visible');
		cy.contains('Yankee Stadium').should('be.visible');
	});

	it('should complete the ticket selection process', () => {
        cy.get('input[type="number"]').clear().type('2');

        cy.get('select').first().select('1').trigger('change');

        cy.wait('@getSeats');

        cy.get('label').contains('Select seats').should('be.visible');

        cy.get('select').last().select('Seat A-1');
        cy.get('select').last().select('Seat A-2');

        cy.contains('Seat A-1').should('be.visible');
        cy.contains('Seat A-2').should('be.visible');

        cy.contains('button', 'Proceed to Payment').should('not.be.disabled').click();
    });

	it('should open the stadium map picture', () => {
		cy.get('lib-ngx-image-zoom').click({ force: true });

		cy.contains('span', 'Stadium Map').should('be.visible');

		cy.get('div.bg-black\\/70').should('be.visible').within(() => {
			cy.get('img').should('have.attr', 'src', 'https://example.com/map.jpg');
		});
		cy.get('button.absolute.top-6.right-6').click({ force: true });
	});
});