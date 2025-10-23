/// <reference types="cypress" />

describe('Landing Page E2E Tests', () => {
	const baseUrl = '/';

	beforeEach(() => {
		cy.visit(baseUrl);
	});

	it('should render hero section with title, description and CTA', () => {
		cy.get('.landing-hero-container').should('be.visible');
		cy.contains('Welcome to MLB Portal!').should('be.visible');
		cy.contains('Track team standings, stats, and streaks across all divisions').should(
			'be.visible'
		);
		cy.contains('Explore Features').should('have.attr', 'href', '#features');
	});

	it('should scroll down to features section when clicking Explore Features', () => {
		cy.contains('Explore Features').click();
		cy.get('#features').should('be.visible');
	});

	it('should render all feature blocks with titles and images', () => {
		const featureTitles = [
			'Discover team rankings and performance insights',
			'Discover teams, track your favorites',
			'See you at the ballpark',
			'Play Ball!',
		];

		featureTitles.forEach((title) => {
			cy.contains(title).should('be.visible');
		});

		const imageAlts = ['MLB Leagues', 'MLB Teams', 'Baseball Fans', 'Baseball Match'];
		imageAlts.forEach((alt) => {
			cy.get(`img[alt="${alt}"]`).should('be.visible');
		});
	});

	it('should render matches-of-the-day component', () => {
		cy.get('app-matches-of-the-day').should('exist');
	});

	it('should render standings widget component', () => {
		cy.get('app-standings').should('exist');
	});
});