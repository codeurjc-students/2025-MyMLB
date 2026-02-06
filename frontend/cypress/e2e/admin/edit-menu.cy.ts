/// <reference types="cypress" />

describe('Edit Menu E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth/me';

	beforeEach(() => {
		cy.viewport(1280, 720);
		cy.intercept('GET', AUTH_API_URL, {
			statusCode: 200,
			body: {
				username: 'testUser',
				roles: ['ADMIN'],
			},
		}).as('getAdmin');

		cy.visit('/');
		cy.wait('@getAdmin');

		cy.contains('Edit Menu').click();
	});

	it('should perform search and show team results', () => {
		cy.intercept('GET', '/api/v1/searchs/team*', { fixture: 'team-paginated.json' }).as('searchTeam');

		cy.contains('Edit Menu').should('be.visible');
		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type('Yankees');

		cy.contains('SEARCH').click();

		cy.wait('@searchTeam');
		cy.contains('New York Yankees').should('be.visible');
	});

	it('should show no results message when search returns empty', () => {
		cy.intercept('GET', '/api/v1/searchs/team*', { fixture: 'empty.json' }).as('searchEmpty');

		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type(
			'Unknown'
		);
		cy.contains('SEARCH').click();

		cy.wait('@searchEmpty');
		cy.contains('No results were found').should('be.visible');
	});

	it('should show Load More button when hasMore is true', () => {
		cy.intercept('GET', '/api/v1/searchs/team*', { fixture: 'team-paginated.json' }).as(
			'searchPaginated'
		);

		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type(
			'Yankees'
		);
		cy.contains('SEARCH').click();

		cy.wait('@searchPaginated');
		cy.contains('LOAD MORE').should('be.visible');
	});

	it('should show error modal when backend fails', () => {
		cy.intercept('GET', '/api/v1/searchs/team*', { statusCode: 500 }).as('searchError');

		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type(
			'Yankees'
		);
		cy.contains('SEARCH').click();

		cy.wait('@searchError');
		cy.get('app-error-modal').should('be.visible');
	});
});