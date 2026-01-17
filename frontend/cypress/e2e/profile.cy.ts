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

	describe('Profile Information & Picture', () => {
        const USERS_API_URL = '/api/v1/users';

        beforeEach(() => {
            cy.intercept('GET', `${USERS_API_URL}/profile`, {
                statusCode: 200,
                body: {
                    email: 'test@example.com',
                    picture: null
                }
            }).as('getProfile');

            cy.visit('/profile');
            cy.wait('@getProfile');
        });

        it('should update profile email successfully', () => {
            const newEmail = 'updated@example.com';

            cy.intercept('PATCH', USERS_API_URL, {
                statusCode: 200,
                body: { username: 'testUser', email: newEmail }
            }).as('patchProfile');

            cy.get('input[type="email"]').type(newEmail);
            cy.contains('button', 'Save Changes').click();

            cy.wait('@patchProfile');
            cy.contains('app-success-modal', 'Profile Updated!').should('be.visible');
            cy.get('app-success-modal button').click();
            cy.get('input[type="email"]').should('have.attr', 'placeholder', newEmail);
        });

        it('should show error modal when profile update fails', () => {
            cy.intercept('PATCH', USERS_API_URL, {
                statusCode: 400
            }).as('patchError');

            cy.get('input[type="email"]').type('invalid-email');
            cy.contains('button', 'Save Changes').click();

            cy.wait('@patchError');
            cy.contains('app-error-modal', 'Invalid format for some of the fields entered').should('be.visible');
        });
    });

	describe('Logout User', () => {
		it('should appear the confirmation modal after clicking on the button', () => {
			cy.contains('button', 'Logout').click();
			cy.get('app-remove-confirmation-modal').should('exist').and('be.visible');
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

	describe('Delete Account', () => {
		it('should successfully delete the user account and redirect him to the home page', () => {
			cy.intercept('DELETE', AUTH_API_URL, {
				statusCode: 200,
				body: {
					status: 'SUCCESS',
					message: 'Account Successfully Deleted',
				},
			}).as('delete-account');

			cy.contains('button', 'Delete Account').click();
			cy.contains('button', 'Yes, Delete').click();

			cy.wait('@delete-account');
			cy.url().should('eq', Cypress.config().baseUrl + '/');
		});
	});
});