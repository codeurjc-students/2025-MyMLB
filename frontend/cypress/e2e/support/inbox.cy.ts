/// <reference types="cypress" />

describe('Support Inbox E2E Tests', () => {
    const AUTH_API_URL = '/api/v1/auth';
    const TICKETS_API_URL = '/api/v1/admin/support/tickets';

    beforeEach(() => {
        cy.intercept('GET', `${AUTH_API_URL}/me`, {
            statusCode: 200,
            body: { username: 'testUser', roles: ['ADMIN'] },
        }).as('getActiveUser');
    });

    it('should show empty state when no tickets exist', () => {
        cy.intercept('GET', TICKETS_API_URL, []).as('getTickets');

        cy.visit('/inbox');

        cy.wait('@getTickets');

        cy.contains('No Tickets at the moment').should('be.visible');
        cy.get('img[alt="No Tickets Picture"]').should('be.visible');
    });

    it('should list tickets when there are open ones', () => {
		const mockTickets = [
			{
				id: 1,
				subject: 'Login Issue',
				status: 'OPEN',
				creationDate: new Date().toISOString()
			},
			{
				id: 2,
				subject: 'Payment Error',
				status: 'OPEN',
				creationDate: new Date().toISOString()
			}
		];

		cy.intercept('GET', '**/api/v1/admin/support/tickets', mockTickets).as('getTickets');

		cy.visit('/inbox');

		cy.wait('@getTickets', { timeout: 10000 });

		cy.get('app-support-ticket-item', { timeout: 10000 }).should('have.length', 2);

		cy.contains('Login Issue').should('be.visible');
		cy.contains('Payment Error').should('be.visible');
	});
});