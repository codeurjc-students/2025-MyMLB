/// <reference types="cypress" />

describe('Footer E2E Tests', () => {
    beforeEach(() => {
        cy.visit('/');
    });

    it('should display footer branding and open support modal', () => {
        cy.contains('MLB Portal').should('be.visible');
        cy.contains('Your baseball universe, beautifully crafted.').should('be.visible');

        cy.contains('button', 'Contact Support').should('be.visible');

        cy.contains('button', 'Contact Support').click();

        cy.get('app-support').should('exist').and('be.visible');

        cy.get('app-support')
            .find('button')
            .contains(/close/i)
            .click({ force: true });

        cy.get('app-support').should('not.exist');
    });
});