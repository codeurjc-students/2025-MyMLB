/// <reference types="cypress" />

describe('Analytics Dashboard E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';

    beforeEach(() => {
		cy.intercept('GET', `${AUTH_API_URL}/me`, {
			statusCode: 200,
			body: {
				username: 'testUser',
				roles: ['ADMIN'],
			},
		}).as('getActiveUser');

		cy.visit('/');
		cy.wait('@getActiveUser');
        cy.visit('/statistics');
    });

    it('should show the list of analytics options', () => {
        cy.get('button.group').should('have.length.at.least', 1);
        cy.get('button.group h3').first().should('not.be.empty');
        cy.get('button.group p').first().should('not.be.empty');
    });

    it('should navigate to Visibility view when clicking on the visibility card', () => {
        cy.contains('button.group', /Visibility/i).click();
        cy.get('app-visibility').should('be.visible');
    });
});