/// <reference types="cypress" />

describe('Standings Component E2E Tests', () => {
	beforeEach(() => {
		cy.fixture('standings.json').then((standings) => {
			cy.intercept('GET', 'https://localhost:8443/api/v1/teams/standings', {
				statusCode: 200,
				body: standings,
			}).as('getStandings');
		});

		cy.visit('/standings');
		cy.wait('@getStandings');
	});

	it('should display the title of the page and the leagues and divisions', () => {
		cy.contains('MLB Standings 2025').should('be.visible');
		cy.contains('AL - East').should('be.visible');
		cy.contains('NL - Central').should('be.visible');
	});

	it('should display the teams in their respective table', () => {
		cy.get('table tbody').contains('New York Yankees').should('be.visible');
		cy.get('table tbody').contains('Boston Red Sox').should('be.visible');
		cy.get('table tbody').contains('Chicago Cubs').should('be.visible');
		cy.get('table tbody tr').should('have.length', 3);
	});


	it('should display the crown icon to the division leader', () => {
		cy.get('img[alt="Division Leader Trophy"]').should('exist').and('be.visible');
	});

	it('should navigate to the team page after clicking on one', () => {
		cy.fixture('team-info.json').then((team) => {
			cy.intercept('GET', 'https://localhost:8443/api/v1/teams/*', {
				statusCode: 200,
				body: team,
			}).as('getTeam');
		});

		cy.get('table tbody tr').should('have.length.at.least', 1);
		cy.get('table tbody tr img[alt="NYY"]').first().click();
		cy.wait('@getTeam');
		cy.url().should('include', '/team/New%20York%20Yankees');
	});
});