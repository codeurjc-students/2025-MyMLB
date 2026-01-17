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

        it('should upload a .webp profile picture successfully', () => {
            cy.intercept('POST', `${USERS_API_URL}/picture`, {
                statusCode: 200,
                body: { url: 'https://cdn.test.com/new-avatar.webp', publicId: 'abc123' }
            }).as('uploadPicture');

            cy.get('input[type="file"]').selectFile({
                contents: Cypress.Buffer.from('fake-image-content'),
                fileName: 'avatar.webp',
                lastModified: Date.now(),
            }, { force: true });

            cy.get('app-loading-modal').should('be.visible');
            cy.wait('@uploadPicture');

            cy.contains('app-success-modal', 'Picture uploaded successfully').should('be.visible');
            cy.get('img[alt="Profile picture"]').should('have.attr', 'src', 'https://cdn.test.com/new-avatar.webp');
        });

        it('should show error when trying to upload a non-webp file', () => {
            cy.get('input[type="file"]').selectFile({
                contents: Cypress.Buffer.from('fake-image-content'),
                fileName: 'avatar.png',
                lastModified: Date.now(),
            }, { force: true });

            cy.contains('app-error-modal', 'Only .webp images are allowed').should('be.visible');
        });

        it('should delete the profile picture successfully', () => {
			cy.intercept('GET', `${USERS_API_URL}/profile`, {
				statusCode: 200,
				body: {
					email: 'test@example.com',
					picture: { url: 'https://cdn.test.com/existing.webp', publicId: '123' }
				}
			}).as('getProfileWithPic');

			cy.visit('/profile');
			cy.wait('@getProfileWithPic');

			cy.intercept('DELETE', `${USERS_API_URL}/picture`, {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'Deleted' }
			}).as('deletePicture');

			cy.get('div.relative.group').within(() => {
				cy.get('button.bg-red-600').click();
			});

			cy.wait('@deletePicture');

			cy.get('app-success-modal').should('be.visible').and('contain', 'Profile Picture Successfully Deleted');
			cy.get('app-success-modal').contains('button', 'Continue').click();
			cy.get('img[alt="Profile picture"]').should('have.attr', 'src', 'assets/account-avatar.png');
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