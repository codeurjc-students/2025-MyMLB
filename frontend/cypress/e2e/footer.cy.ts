/// <reference types="cypress" />

describe('Footer E2E Tests', () => {
    beforeEach(() => {
        cy.visit('/');
    });

    it('should display footer branding and open support modal', () => {
        cy.contains('Diamond Insights').should('be.visible');
        cy.contains('Insights for the Modern Fan').should('be.visible');

        cy.contains('button', 'Contact Support').should('be.visible');

        cy.contains('button', 'Contact Support').click();

        cy.get('app-support').should('exist').and('be.visible');

        cy.get('#close-button').click();

        cy.get('app-support').should('not.exist');
    });
});