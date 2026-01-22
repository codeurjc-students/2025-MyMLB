/// <reference types="cypress" />

describe('Support Ticket Modal E2E Tests', () => {
    const apiBase = 'https://localhost:8443/api/v1/admin/support/tickets';

    beforeEach(() => {
        cy.visit('/admin/support/inbox');

        cy.intercept('GET', `${apiBase}`, [
            {
                id: 't1',
                subject: 'Login Issue',
                status: 'OPEN',
                creationDate: new Date().toISOString()
            }
        ]).as('getTickets');

        cy.reload();
        cy.wait('@getTickets');

        cy.get('app-support-ticket-item').first().click();

        cy.intercept('GET', `${apiBase}/t1/conversation`, [
            {
                id: 'm1',
                senderEmail: 'user@test.com',
                body: 'Hello, I need help',
                creationDate: new Date().toISOString()
            }
        ]).as('getConversation');

        cy.wait('@getConversation');

        cy.get('app-support-ticket-modal').should('exist').and('be.visible');
    });

    it('should render the modal UI correctly', () => {
        cy.contains('Support Ticket').should('be.visible');

        cy.contains('From: user@test.com').should('be.visible');
        cy.contains('Hello, I need help').should('be.visible');

        cy.get('textarea[placeholder="Write your reply..."]').should('exist');

        cy.contains('button', 'Send Reply').should('exist').and('be.visible');

        cy.contains('button', 'Close Ticket').should('exist').and('be.visible');

        cy.get('app-support-ticket-modal').find('button').filter(':has(svg)').should('exist').and('be.visible');
    });
});