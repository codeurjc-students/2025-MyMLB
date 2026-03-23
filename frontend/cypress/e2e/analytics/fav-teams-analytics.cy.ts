/// <reference types="cypress" />

describe('Fav Teams Analytics E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
	const ANALYTICS_API_URL = '/api/v1/analytics/fav-teams';

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
            body: {
				'New York Yankees': 15,
				'Boston Red Sox': 10,
				'Los Angeles Dodgers': 5
			}
        }).as('getAnalytics');

		cy.visit('/statistics');
        cy.contains('button', /Favorite/i).click();
        cy.wait('@getAnalytics');
	});

	it('should render the page correctly', () => {
		cy.get('h1').contains('Favorite Teams Analytics').should('be.visible');
		cy.get('h2').contains('Fanbase Distribution').should('be.visible');

		cy.get('canvas').should('be.visible');
        cy.get('span.leading-none').should('not.be.empty');
	});
});