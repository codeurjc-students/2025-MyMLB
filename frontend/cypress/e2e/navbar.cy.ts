/// <reference types="cypress" />

type Role = 'GUEST' | 'USER' | 'ADMIN';

const roleConfig: Record<
	Role,
	{
		intercept: { statusCode: number; body: { username: string; roles: string[] } };
		visibleItems: string[];
		absentItems?: string[];
		profileRedirect: string;
	}
> = {
	GUEST: {
		intercept: {
			statusCode: 401,
			body: { username: '', roles: ['GUEST'] },
		},
		visibleItems: ['Home', 'Login', 'Sign Up', 'Teams', 'Standings'],
		absentItems: [
			'My Tickets',
			'Fav Teams',
			'Edit Info',
			'Matches',
			'Create Match',
			'Statistics',
		],
		profileRedirect: '/auth',
	},
	USER: {
		intercept: {
			statusCode: 200,
			body: { username: 'testUser', roles: ['USER'] },
		},
		visibleItems: ['Home', 'My Tickets', 'Fav Teams', 'Teams', 'Standings'],
		absentItems: ['Login', 'Sign Up', 'Edit Info', 'Matches', 'Create Match', 'Statistics'],
		profileRedirect: '/profile',
	},
	ADMIN: {
		intercept: {
			statusCode: 200,
			body: { username: 'testAdmin', roles: ['ADMIN'] },
		},
		visibleItems: ['Home', 'Edit Info', 'Matches', 'Create Match', 'Statistics'],
		absentItems: ['Login', 'Sign Up', 'My Tickets', 'Fav Teams', 'Teams', 'Standings'],
		profileRedirect: '/profile',
	},
};

describe('Navigation Bar E2E Tests', () => {
	const testCommonElements = () => {
		cy.contains('a.nav-elems', 'Home').should('exist').and('be.visible').click();
		cy.url().should('match', /\/$/);

		cy.get('img[alt="MLB Portal Logo"]')
			.should('exist')
			.and('be.visible')
			.and('have.attr', 'src', 'assets/logo.png');

		cy.get('img[alt="Avatar Profile"]')
			.should('exist')
			.and('be.visible')
			.and('have.attr', 'src', 'assets/account-avatar.png');

		cy.get('input[type="checkbox"]')
            .should('exist')
            .and('be.visible');

        cy.contains('span', 'Dark Mode')
            .should('exist')
            .and('be.visible');
	};

	const testRoleNavigation = (role: Role) => {
		describe(`${role} User Navigation`, () => {
			beforeEach(() => {
				cy.viewport('macbook-15');
				cy.intercept(
					'GET',
					'https://localhost:8443/api/auth/me',
					roleConfig[role].intercept).as('getUser');

				cy.visit('/');
				cy.wait('@getUser');
			});

			it('should display the correct navigation items', () => {
				testCommonElements();
				roleConfig[role].visibleItems.forEach((label) => {
					cy.contains('a.nav-elems', label).should('exist').and('be.visible');
				});
				roleConfig[role].absentItems?.forEach((label) => {
					cy.contains('a.nav-elems', label).should('not.exist');
				});
			});

			it('should navigate correctly when clicking on the profile avatar', () => {
				cy.get('a img[alt="Avatar Profile"]').click();
				cy.url().should('include', roleConfig[role].profileRedirect);
			});

			if (role === 'GUEST') {
				it('should navigate to login form when clicking "Login"', () => {
					cy.contains('a.nav-elems', 'Login').click();
					cy.url().should('include', '/auth?form=login');
				});

				it('should navigate to register form when clicking "Sign Up"', () => {
					cy.contains('a.nav-elems', 'Sign Up').click();
					cy.url().should('include', '/auth?form=register');
				});
			}
			else if (role === 'USER') {
				// TODO
			}
			else if (role === 'ADMIN') {
				// TODO
			}
		});
	};

	testRoleNavigation('GUEST');
	testRoleNavigation('USER');
	testRoleNavigation('ADMIN');
});