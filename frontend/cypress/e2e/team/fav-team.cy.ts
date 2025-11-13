/// <reference types="cypress" />

describe('Favorite Team Component E2E Tests', () => {
	const AUTH_API_URL = '/api/auth/me';

	beforeEach(() => {
		cy.intercept('GET', AUTH_API_URL, {
			statusCode: 200,

			body: {
				username: 'testUser',

				roles: ['USER'],
			},
		}).as('getUser');

		cy.intercept('GET', '/api/users/favorites/teams', {
			fixture: 'fav-teams.json',
		}).as('getFavTeams');

		cy.intercept('GET', '/api/teams/standings', {
			fixture: 'all-teams.json',
		}).as('getTeams');

		cy.intercept('POST', '/api/users/favorites/teams/*', {
			statusCode: 200,
			body: { status: 'SUCCESS', message: 'Team successfully added' },
		}).as('addFavTeam');

		cy.intercept('DELETE', '/api/users/favorites/teams/New York Yankees', {
			statusCode: 200,
			body: { status: 'SUCCESS', message: 'Team successfully removed' },
		}).as('removeFavTeam');

		cy.visit('/');

		cy.wait('@getUser');

		cy.visit('/favorite-teams');
	});

	describe('General Elements', () => {
		it('should display the title of the page', () => {
			cy.get('h1').should('contain.text', "testUser's Favorite Teams");
		});

		it('should display the "Add Favorite Team" button', () => {
			cy.get('button').contains('Add Favorite Team').should('exist').and('be.visible');
		});

		it('should display the modal with all teams after clicking on the "Add Favorite Team" button', () => {
			cy.get('button').contains('Add Favorite Team').click();

			cy.wait('@getTeams');

			cy.wait('@getTeams');

			cy.get('h2').should('contain.text', 'Select a Team to Add');

			cy.get('ul li button').should('have.length.greaterThan', 0);
		});
	});

	describe('Add Favorite Team', () => {
		it('should add the team to the favorite list and display the confirmation modal', () => {
			cy.get('button').contains('Add Favorite Team').click();

			cy.wait('@getTeams');

			cy.get('ul li button').first().click();

			cy.wait('@addFavTeam');

			cy.wait('@getFavTeams');

			cy.wait('@getTeams');

			cy.get('app-success-modal').should('exist');

			cy.get('app-success-modal').contains('Team successfully added');

			cy.get('app-success-modal').get('button').contains('Continue').click();

			cy.get('button').contains('Close').click();

			cy.get('span').contains('Boston Red Sox').should('exist').and('be.visible');
		});
	});

	describe('Remove Favorite Team', () => {
		it('should remove the team from the fav list and display the confirmation modal', () => {
			cy.get('button[title="Remove team"]').first().click();

			cy.get('app-remove-confirmation-modal').should('exist').and('be.visible');

			cy.get('app-remove-confirmation-modal').contains(
				'Are you sure you want to remove this team as favorite?'
			);

			cy.get('app-remove-confirmation-modal').get('button').contains('Yes, Remove').click();

			cy.wait('@removeFavTeam');

			cy.get('app-success-modal').get('button').contains('Continue').click();

			cy.get('span').contains('Boston Red Sox').should('not.exist');
		});
	});
});