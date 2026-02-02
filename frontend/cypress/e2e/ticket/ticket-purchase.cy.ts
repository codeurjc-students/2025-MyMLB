/// <reference types="cypress" />

describe('Ticket Purchase E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const TICKETS_API_URL = '/api/v1/tickets';
	const matchId = 1;
	const eventId = 10;

	beforeEach(() => {
		cy.intercept('GET', `${AUTH_API_URL}/me`, {
			statusCode: 200,
			body: { username: 'testUser', roles: ['GUEST', 'USER'] },
		}).as('getActiveUser');

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
				content: [{ id: 1, sectorName: 'North Stand', price: 100 }],
			},
		}).as('getSectors');

		cy.intercept('GET', `**/api/v1/events/${eventId}/sector/1`, {
			statusCode: 200,
			body: {
				content: [
					{ id: 101, name: 'A-1' },
					{ id: 102, name: 'A-2' },
				],
			},
		}).as('getSeats');

		cy.intercept('POST', TICKETS_API_URL, {
			statusCode: 200,
			body: { status: 'SUCCESS' },
		}).as('purchaseRequest');

		cy.visit(`/tickets?matchId=${matchId}`);
		cy.wait(['@getActiveUser', '@getEvent', '@getSectors']);
	});

	it('should purchase the ticket successfully', () => {
		cy.get('input[type="number"]').clear().type('2');

		cy.get('select').first().select('1').trigger('change');
		cy.wait('@getSeats');

		cy.get('select').last().select('Seat A-1');
		cy.get('select').last().select('Seat A-2');

		cy.contains('Seat A-1').should('be.visible');
		cy.contains('Seat A-2').should('be.visible');
		cy.contains('$200.00').should('be.visible');

		cy.contains('button', 'Proceed to Payment').click();

		cy.get('app-ticket-purchase').within(() => {
			cy.contains('Boston Red Sox').should('be.visible');
			cy.contains('#A-1').should('be.visible');
			cy.contains('#A-2').should('be.visible');
			cy.contains('$200.00').should('be.visible');
		});

		cy.get('input[formControlName="ownerName"]').type('Test User');
		cy.get('input[formControlName="cardNumber"]').type('4539148912345674');
		cy.get('input[formControlName="expirationDate"]').type('2028-12-01', { force: true  });
		cy.get('input[formControlName="cvv"]').type('123');

		cy.contains('button', 'Confirm Payment').click();

		cy.wait('@purchaseRequest');

		cy.get('app-success-modal').should('be.visible');
		cy.contains('Thank you for your purchase').should('be.visible');

		cy.contains('button', 'Continue').click();
		cy.get('app-successfull-purchase').should('be.visible');
	});
});