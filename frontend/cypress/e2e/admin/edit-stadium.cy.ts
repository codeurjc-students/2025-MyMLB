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

		cy.contains('Edit Menu').click();

		cy.intercept('GET', '/api/v1/searchs/stadium*', { fixture: 'stadium.json' }).as('searchStadium');

		cy.get('select').first().select('stadium');
		cy.get('input[placeholder="Search a Team, a Player or a Stadium to edit..."]').type('Yankee Stadium');
		cy.wait('@searchStadium');

		cy.contains('EDIT').click();
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

	it('should show loading modal when uploading', () => {
		cy.intercept('POST', '/api/v1/stadiums/*/pictures', (req) => {
			req.reply({
				url: 'http://test.com/new.webp',
				publicId: 'newPic',
			});
		}).as('uploadPicture');

		cy.get('input[type="file"]').selectFile('cypress/fixtures/test.webp', { force: true });
		cy.get('app-loading-modal').should('be.visible');
		cy.wait('@uploadPicture');
		cy.get('app-success-modal').should('be.visible');
	});

	it('should go back to menu when clicking Go Back', () => {
		cy.contains('Go Back').click();
		cy.get('app-edit-menu').should('be.visible');
	});

	it('should show success modal when confirming changes', () => {
		cy.contains('Confirm Changes').click();
		cy.get('app-success-modal').should('be.visible');
		cy.contains('Changes applied successfully').should('be.visible');
	});
});