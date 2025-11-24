/// <reference types="cypress" />

describe('Error Component E2E Tests', () => {
	beforeEach(() => {
		cy.visit('/');
	});

	it('should display the error component when trying to access a ADMIN only page', () => {
		cy.visit('/edit-menu');
		cy.get('h1').contains('404 Not Found').should('exist').and('be.visible');
	});

	it('should display the error page when trying to access a non existant page', () => {
		cy.visit('/any-page');
		cy.get('h1').contains('404 Not Found').should('exist').and('be.visible');
	});

	it('should display the error page correctly with all of its elements', () => {
		cy.visit('/error');
		cy.get('h1').contains('404 Not Found').should('exist').and('be.visible');
		cy.get('p').contains("Whoops! This page doesn't exist.").and('be.visible');
		cy.get('button').contains('Go Back to Home.').and('be.visible');
	});
});