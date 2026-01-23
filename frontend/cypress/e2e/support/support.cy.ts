/// <reference types="cypress" />

describe('Support Modal E2E Tests', () => {
    beforeEach(() => {
        cy.intercept('GET', '**/api/v1/auth/me', {
            statusCode: 200,
            body: { username: 'testUser', roles: ['USER'] },
        }).as('me');

        cy.visit('/');
        cy.contains('button', 'Contact Support').click();

        cy.get('app-support').should('be.visible');
    });

	it('should fill the form and send a support ticket successfully', () => {
		cy.intercept('POST', '**/api/v1/support', {
			statusCode: 200,
			body: { status: 'SUCCESS', message: 'Ticket Created' }
		}).as('createTicket');

		cy.get('app-support').within(() => {
			const email = 'test@example.com';
			const subject = 'Test Subject';
			const message = 'This is a test message';

			cy.get('input[type="email"]').type(email);
			cy.get('input[formControlName="subject"]').type(subject);
			cy.get('textarea[formControlName="body"]').type(message);

			cy.contains('button', 'Send Message').click();
		});

		cy.wait('@createTicket');

		cy.get('app-success-modal').should('be.visible');
	});

    it('should close the modal when clicking the close button', () => {
        cy.get('#close-button').click();

        cy.get('app-support').should('not.exist');
    });
});