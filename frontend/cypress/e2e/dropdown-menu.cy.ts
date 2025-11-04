/// <reference types="cypress" />

describe('Dropdown Menu E2E Tests', () => {
	beforeEach(() => {
		cy.fixture('standings').then((mockStandings) => {
			cy.intercept('GET', '/api/teams/standings', {
				statusCode: 200,
				body: mockStandings,
			}).as('getStandings');
		});

		cy.visit('/');
		cy.wait('@getStandings');
		cy.get('li.group').contains(/^Teams$/).trigger('mouseover');
	});

	it('should render all teams in the dropdown', () => {
        cy.get('app-dropdown-menu').contains('New York Yankees').should('exist').and('be.visible');
        cy.get('app-dropdown-menu').contains('Boston Red Sox').should('exist').and('be.visible');
    });

	it('should display team logos', () => {
        cy.get('app-dropdown-menu')
            .find('img[alt="New York Yankees logo"]')
            .should('have.attr', 'src')
            .and('include', 'NYY.png');

        cy.get('app-dropdown-menu')
            .find('img[alt="Boston Red Sox logo"]')
            .should('have.attr', 'src')
            .and('include', 'BOS.png');
    });

	it('should navigate when clicking on a team', () => {
        cy.fixture('team-info').then((mockTeamInfo) => {
			cy.intercept('GET', '/api/teams/New York Yankees', {
				statusCode: 200,
				body: mockTeamInfo,
			}).as('getTeamInfo');
		});

        cy.get('app-dropdown-menu').contains('New York Yankees').click();
        cy.wait('@getTeamInfo');
        cy.url().should('include', '/team/New York Yankees');
    });
});