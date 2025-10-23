/// <reference types="cypress" />

describe('Standings Widget E2E Tests', () => {
	const baseUrl = '/';

	beforeEach(() => {
		cy.fixture('standings').then((mockStandings) => {
			cy.intercept('GET', '/api/teams/standings', {
				statusCode: 200,
				body: mockStandings,
			}).as('getStandings');
		});
		cy.visit(baseUrl);
		cy.wait('@getStandings');
	});

	it('should display league logo and division title', () => {
		cy.get('img[alt="AL Logo"]').should('be.visible');
		cy.contains('AL').should('be.visible');
		cy.contains('East').should('be.visible');
	});

	it('should render standings table with team rows', () => {
		cy.contains('NYY').should('be.visible');
		cy.contains('BOS').should('be.visible');
		cy.contains('95').should('be.visible');
		cy.contains('72').should('be.visible');
		cy.contains('0.586').should('be.visible');
		cy.contains('–').should('be.visible');
		cy.contains('5').should('be.visible');
		cy.contains('7-3').should('be.visible');
	});

	it('should navigate to next division when clicking →', () => {
		cy.contains('→').click();
		cy.contains('NL').should('be.visible');
		cy.contains('Central').should('be.visible');
		cy.contains('CHC').should('be.visible');
		cy.contains('0.543').should('be.visible');
	});

	it('should navigate back to previous division when clicking ←', () => {
		cy.contains('→').click();
		cy.contains('←').click();
		cy.contains('AL').should('be.visible');
		cy.contains('East').should('be.visible');
		cy.contains('NYY').should('be.visible');
	});
});