/// <reference types="cypress" />

describe('User Page E2E with fixtures', () => {
	it('should display a list of users from fixture', () => {
		cy.fixture('user-examples.json').then((users) => {
			cy.intercept('GET', '/api/users', {
				statusCode: 200,
				body: users,
			}).as('getUsers');
		});

		cy.visit('/');
		cy.wait('@getUsers');

		cy.get('li').should('have.length.greaterThan', 0);
		cy.get('li').first().should('contain.text', '@');
	});

	it('should show error message if users fail to load', () => {
		cy.intercept('GET', '/api/users', { statusCode: 500 }).as('getUsersFail');

		cy.visit('/');
		cy.wait('@getUsersFail');

		cy.contains('Error while loading the users').should('be.visible');
	});
});