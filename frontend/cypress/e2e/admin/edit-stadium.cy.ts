/// <reference types="cypress" />

describe('Edit Stadium E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth/me';

	beforeEach(() => {
        cy.viewport(1280, 720);

        cy.intercept('GET', AUTH_API_URL, {
            statusCode: 200,
            body: { username: 'testUser', roles: ['ADMIN'] },
        }).as('getAdmin');

        cy.visit('/');
        cy.wait('@getAdmin');

        cy.contains('Edit Menu').should('be.visible').click();
        cy.url().should('include', '/edit-menu');
        cy.intercept('GET', '/api/v1/searchs/stadium*', { fixture: 'stadium.json' }).as('searchStadium');

        cy.get('mat-select').first().should('be.visible').click({ force: true });
        cy.get('mat-option', { timeout: 10000 }).contains('Stadium').should('be.visible').click();
        cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').should('be.visible').type('Yankee Stadium');

        cy.wait('@searchStadium');

        cy.contains('button', 'EDIT').should('be.visible').click();
    });

	it('should render stadium info', () => {
		cy.contains('Edit Yankee Stadium').should('be.visible');

		cy.get('.team-section-container').within(() => {
			cy.contains('Name').prev('p').should('contain.text', 'Yankee Stadium');
			cy.contains('Opening Date').prev('p').should('contain.text', '2009');
			cy.contains('Team').prev('p').should('contain.text', 'New York Yankees');
		});
	});

	it('should render pictures', () => {
		cy.get('img[alt="Stadium picture"]').should('have.length.at.least', 2);
	});

	it('should go back to menu when clicking Go Back', () => {
		cy.contains('Go Back').click();
		cy.get('app-edit-menu').should('be.visible');
	});
});