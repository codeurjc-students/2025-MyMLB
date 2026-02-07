/// <reference types="cypress" />

describe('Support Ticket Modal E2E Tests', () => {
    const AUTH_API_URL = '/api/v1/auth';
    const TICKETS_API_URL = '**/api/v1/admin/support/tickets';

    beforeEach(() => {
        cy.intercept('GET', `${AUTH_API_URL}/me`, {
            statusCode: 200,
            body: { username: 'testUser', roles: ['ADMIN'] },
        }).as('getActiveUser');

        cy.intercept('GET', TICKETS_API_URL, [
            {
                id: 1,
                subject: 'Login Issue',
                status: 'OPEN',
                creationDate: new Date().toISOString()
            }
        ]).as('getTickets');

        cy.intercept('GET', `${TICKETS_API_URL}/1/conversation`, [
            {
                id: 1,
                senderEmail: 'user@test.com',
                body: 'Hello, I need help',
                creationDate: new Date().toISOString()
            }
        ]).as('getConversation');

        cy.visit('/inbox');
        cy.wait('@getActiveUser');
        cy.wait('@getTickets');

        cy.get('app-support-ticket-item').first().click();
        cy.wait('@getConversation');
    });

    it('should render the modal UI correctly', () => {
        cy.get('app-support-ticket-modal').should('be.visible').within(() => {
            cy.contains('Support Ticket').should('be.visible');
            cy.contains('From: user@test.com').should('be.visible');
            cy.contains('Hello, I need help').should('be.visible');

            cy.get('textarea[placeholder="Write your reply..."]').should('exist');
            cy.contains('button', 'Send Reply').should('be.visible');
            cy.contains('button', 'Close Ticket').should('be.visible');

            cy.get('button').filter(':has(svg)').should('be.visible');
        });
    });
});