/// <reference types="cypress" />

describe('Edit Team Component E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth/me';

	beforeEach(() => {
		cy.viewport(1280, 720);
		cy.intercept('GET', AUTH_API_URL, {
			statusCode: 200,
			body: { username: 'testUser', roles: ['ADMIN'] },
		}).as('getAdmin');

		cy.visit('/');
		cy.wait('@getAdmin');

		cy.contains('Edit Info').click();

		cy.intercept('GET', '/api/v1/searchs/team*', { fixture: 'search-team.json' }).as('searchTeam');

		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type('New York Yankees');
		cy.contains('SEARCH').click();
		cy.wait('@searchTeam');

		cy.contains('EDIT').click();
	});

	it('should display team information', () => {
		cy.get('.team-section-container').should('exist');
		cy.contains('Edit New York Yankees').should('be.visible');
		cy.contains('Abbreviation').siblings('p').should('contain.text', 'NYY');
		cy.contains('League').siblings('p').should('contain.text', 'American');
		cy.contains('Division').siblings('p').should('contain.text', 'East');
		cy.contains('Games').siblings('p').should('contain.text', '162');
		cy.contains('Wins').siblings('p').should('contain.text', '100');
		cy.contains('Losses').siblings('p').should('contain.text', '62');
		cy.contains('Win %').siblings('p').should('contain.text', '0.617');
		cy.contains('Last Ten').siblings('p').should('contain.text', '7-3');
	});

	it('should allow editing city and general info', () => {
		cy.get('input[placeholder="New York"]').clear().type('Boston');
		cy.get('textarea[placeholder="Founded in 1901"]').clear().type('Updated info about team');
		cy.get('input[placeholder="Add year..."]').type('2025');
	});

	it('should open stadium modal when clicking stadium button', () => {
		cy.contains('Yankee Stadium').click();
		cy.get('app-select-element-modal').should('be.visible');
	});

	it('should confirm changes and show success modal', () => {
		cy.get('input[placeholder="New York"]').clear().type('Boston');
		cy.get('textarea[placeholder="Founded in 1901"]').clear().type('Updated info');

		cy.intercept('PATCH', '/api/v1/teams/*', {
			statusCode: 200,
			body: { message: 'Changes successfully applied' },
		}).as('updateTeam');

		cy.get('app-action-buttons button').contains('Confirm').click();
		cy.wait('@updateTeam');
		cy.get('app-success-modal').should('contain.text', 'Changes applied successfully');
	});

	it('should go back to menu when clicking goBack', () => {
		cy.get('app-action-buttons button').contains('Go Back').click();
		cy.url().should('include', '/edit-menu');
	});
});