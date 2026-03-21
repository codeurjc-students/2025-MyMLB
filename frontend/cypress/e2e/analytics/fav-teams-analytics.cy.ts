/// <reference types="cypress" />

describe('Fav Teams Analytics E2E Tests', () => {
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

        cy.intercept('GET', '**/api/v1/analytics/fav-teams', {
            body: {
				'New York Yankees': 15,
				'Boston Red Sox': 10,
				'Los Angeles Dodgers': 5
			}
        }).as('getStats');

		cy.visit('/statistics');
        cy.contains('button', /Favorite/i).click();
        cy.wait('@getStats');
	});

	it('should render the page correctly', () => {
		cy.get('h1').contains('Favorite Teams Analytics').should('be.visible');
		cy.get('h2').contains('Fanbase Distribution').should('be.visible');

		cy.get('canvas').should('be.visible');
        cy.get('span.leading-none').should('not.be.empty');
	});
});