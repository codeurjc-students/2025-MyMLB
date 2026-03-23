/// <reference types="cypress" />

describe('Visibility Analytics E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const ANALYTICS_API_URL = '/api/v1/analytics/visibility*';

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

        cy.intercept('GET', ANALYTICS_API_URL, {
            body: [
                { date: '2026-03-01', visualizations: 150, newUsers: 15, deletedUsers: 2 },
                { date: '2026-03-02', visualizations: 250, newUsers: 25, deletedUsers: 5 }
            ]
        }).as('getAnalytics');

		cy.visit('/statistics');
        cy.contains('button', /Visibility/i).click();
        cy.wait('@getAnalytics');
    });

    it('should display statistics cards', () => {
        cy.contains('p', 'Total Views').parent().find('h3').should('contain', '400');
        cy.contains('p', 'New Users').parent().find('h3').should('contain', '40');
        cy.contains('p', 'Churn Users').parent().find('h3').should('contain', '7');
    });

    it('should render the chart', () => {
        cy.get('canvas').should('be.visible');
        cy.get('span.leading-none').should('not.be.empty');
        cy.contains('h2', 'Visibility Overview').should('be.visible');
    });
});