/// <reference types="cypress" />

describe('Create Player Component E2E Tests', () => {
	const API_URL = '/api/v1/stadiums';

	beforeEach(() => {
		cy.viewport(1280, 720);
		cy.intercept('GET', '/api/v1/auth/me', {
			statusCode: 200,
			body: { username: 'testUser', roles: ['ADMIN'] },
		}).as('getAdmin');

		cy.visit('/create-player');
		cy.wait('@getAdmin');
	});

	it('should display the creation form', () => {
		cy.contains('Create Player').should('be.visible');
		cy.get('input[placeholder="e.g., Aaron Judge"]').should('exist').and('be.visible');
		cy.get('input[placeholder="99"]').should('exist').and('be.visible');
		cy.get('input[placeholder="e.g., New York Yankees"]').should('exist').and('be.visible');
		cy.get('button').contains('Select Team').should('exist').and('be.visible');
	});

	it('should create the player successfully', () => {
        cy.intercept('GET', '/api/v1/teams/available*', {
            statusCode: 200,
            body: {
                content: [{ name: 'Detroit Tigers' }],
                page: { size: 10, number: 0, totalElements: 1, totalPages: 1 }
            },
        }).as('getAvailableTeams');

        cy.intercept('POST', '/api/v1/players/position-players', {
            statusCode: 200,
            body: {
                name: 'Gleyber Torres',
                playerNumber: 25,
                teamName: 'Detroit Tigers',
                position: '2B'
            },
        }).as('createPlayer');

        cy.get('input[placeholder="e.g., Aaron Judge"]').clear().type('Gleyber Torres');
        cy.get('input[placeholder="99"]').clear().type('25');
        cy.get('select').select('2B');

        cy.get('button').contains('Select Team').click();
        cy.wait('@getAvailableTeams');
        cy.get('app-select-element-modal ul li button').first().click();

        cy.get('app-success-modal').contains('Continue').click();

		cy.get('button').contains('Confirm').click();
        cy.wait('@createPlayer');

        cy.get('app-success-modal p').invoke('text').should('match', /Gleyber Torres\s+successfully created/);

    });

	it('should navigate back to home when clicking goBack', () => {
		cy.get('app-action-buttons button').contains('Go Back').click();
		cy.url().should('include', '/');
	});
});