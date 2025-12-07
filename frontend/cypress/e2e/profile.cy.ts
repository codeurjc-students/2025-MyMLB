/// <reference types="cypress" />

describe('Profile Component E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';

	beforeEach(() => {
		cy.intercept('GET', `${AUTH_API_URL}/me`, {
			statusCode: 200,
			body: {
				username: 'testUser',
				roles: ['GUEST', 'USER'],
			},
		}).as('getActiveUser');

		cy.visit('/');
		cy.wait('@getActiveUser');

		cy.get('img[alt="Avatar Profile"]').should('be.visible').parent('a').click();
		cy.url().should('include', '/profile');
	});

	describe('Logout User', () => {
		it('should appear the confirmation modal after clicking on the button', () => {
			cy.contains('button', 'Logout').click();
			cy.contains('h2', 'Are you sure you want to log out?').should('be.visible');
			cy.contains(
				'p',
				'You will be signed out of your account and will need to log in again to continue.'
			).should('be.visible');
			cy.contains('button', 'Cancel').should('be.visible');
			cy.contains('button', 'Yes, Logout').should('be.visible');
		});

		it('should close the modal when clicking on the cancel button', () => {
			cy.contains('button', 'Logout').click();
			cy.contains('button', 'Cancel').click();
			cy.contains(
				'p',
				'You will be signed out of your account and will need to log in again to continue.'
			).should('not.exist');
			cy.contains('button', 'Cancel').should('not.exist');
			cy.contains('button', 'Yes, Logout').should('not.exist');
			cy.contains('button', 'Logout').should('be.visible');
		});

		it('should successfully logout the user and redirect him to the home page', () => {
			cy.intercept('POST', `${AUTH_API_URL}/logout`, {
				statusCode: 200,
				body: {
					status: 'SUCCESS',
					message: 'Logout Successful',
				},
			}).as('logout');

			cy.contains('button', 'Logout').click();
			cy.contains('button', 'Yes, Logout').click();

			cy.wait('@logout');
			cy.url().should('eq', Cypress.config().baseUrl + '/');
		});
	});
});