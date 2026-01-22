/// <reference types="cypress" />

describe('Support Inbox E2E Tests', () => {
    beforeEach(() => {
        cy.visit('/');
    });

    it('should show empty state when no tickets exist', () => {
        cy.intercept('GET', 'https://localhost:8443/api/v1/admin/support/tickets', [])
            .as('getTickets');

        cy.visit('/admin/support/inbox');

        cy.wait('@getTickets');

        cy.contains('No Tickets at the moment').should('be.visible');
        cy.contains("You're all caught up! 🎉").should('be.visible');
        cy.get('img[alt="No Tickets Picture"]').should('be.visible');
    });

    it('should list tickets when API returns data', () => {
        const mockTickets = [
            {
                id: 't1',
                subject: 'Login Issue',
                status: 'OPEN',
                creationDate: new Date().toISOString()
            },
            {
                id: 't2',
                subject: 'Payment Error',
                status: 'OPEN',
                creationDate: new Date().toISOString()
            }
        ];

        cy.intercept('GET', 'https://localhost:8443/api/v1/admin/support/tickets', mockTickets)
            .as('getTickets');

        cy.visit('/admin/support/inbox');

        cy.wait('@getTickets');

        cy.get('app-support-ticket-item').should('have.length', 2);
        cy.contains('Login Issue').should('exist');
        cy.contains('Payment Error').should('exist');
    });

    it('should open ticket modal when clicking a ticket', () => {
        const mockTickets = [
            {
                id: 't1',
                subject: 'Login Issue',
                status: 'OPEN',
                creationDate: new Date().toISOString()
            }
        ];

        cy.intercept('GET', 'https://localhost:8443/api/v1/admin/support/tickets', mockTickets).as('getTickets');

        cy.visit('/admin/support/inbox');

        cy.wait('@getTickets');

        cy.get('app-support-ticket-item').first().click();

        cy.get('app-support-ticket-modal').should('exist').and('be.visible');
    });
});