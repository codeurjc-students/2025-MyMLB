/// <reference types="cypress" />

describe('Support Modal E2E Tests', () => {
    beforeEach(() => {
        cy.visit('/');

        cy.contains('Contact Support').click();

        cy.get('app-support').should('exist').and('be.visible');
    });

    it('should fill the form and send a support ticket successfully', () => {
        cy.intercept('POST', 'https://localhost:8443/api/v1/support', {
            statusCode: 200,
            body: { status: 'SUCCESS', message: 'Created' }
        }).as('createTicket');

        cy.get('input[formcontrolname="subject"]').as('subject');
        cy.get('textarea[formcontrolname="body"]').as('body');

        cy.contains('button', 'Send Message').should('have.class', 'disabled');

        cy.get('@subject').type('Test Subject');
        cy.get('@body').type('This is a test message');

        cy.contains('button', 'Send Message').should('not.have.class', 'disabled').click();

        cy.wait('@createTicket');

        cy.get('app-success-modal').should('exist').and('be.visible').contains('Message sent successfully');
    });

    it('should close the modal when clicking the close button', () => {
        cy.get('button').filter(':contains("svg")').first().click({ force: true });
        cy.get('app-support').should('not.exist');
    });
});