/// <reference types = "cypress" />

describe('Edit Player Component E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth/me';

	beforeEach(() => {
		cy.intercept('GET', AUTH_API_URL, {
			statusCode: 200,
			body: { username: 'testUser', roles: ['ADMIN'] },
		}).as('getAdmin');

		cy.visit('/');
		cy.wait('@getAdmin');

		cy.contains('Edit Info').click();

		cy.intercept('GET', '/api/v1/searchs/player*', { fixture: 'player.json' }).as('searchPlayer');

		cy.get('select').first().select('player');
		cy.get('#player-type').select('position');
		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type('Aaron Judge');
		cy.contains('SEARCH').click();
		cy.wait('@searchPlayer');

		cy.contains('EDIT').click();
	});

    it('should display player info and picture', () => {
        cy.get('h2').should('contain.text', 'Edit Aaron Judge');
        cy.get('img[alt="Aaron Judge picture"]').should('be.visible');
    });

    it('should allow editing editable fields', () => {
        cy.get('input[type="number"][placeholder="99"]').clear().type('27');
        cy.get('select').select('CF');
        cy.get('button').contains('Yankees').click();
        cy.get('app-select-element-modal').should('be.visible');
    });

    it('should show readonly stats', () => {
        cy.get('.grid .text-lg').first().should('not.be.empty');
    });

    it('should confirm changes successfully', () => {
        cy.get('input[type="number"][placeholder="99"]').clear().type('27');

		cy.intercept('PATCH', '/api/v1/players/position-players/**', {
			statusCode: 200,
			body: { message: 'Changes successfully applied' },
		}).as('updatePlayer');

        cy.get('app-action-buttons button').contains('Confirm').click();
        cy.wait('@updatePlayer');
        cy.get('app-success-modal').should('contain.text', 'Changes applied successfully');
    });

    it('should show delete confirmation modal and delete player', () => {
        cy.get('button').contains('Delete Player').click();
        cy.get('app-remove-confirmation-modal').should('be.visible');
        cy.intercept('DELETE', '/api/v1/players/*', {
			statusCode: 200
		}).as('deletePlayer');

        cy.get('app-remove-confirmation-modal button').contains('Yes, Remove').click();
        cy.wait('@deletePlayer');
    });
});