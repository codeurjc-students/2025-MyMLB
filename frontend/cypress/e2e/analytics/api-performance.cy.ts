/// <reference types="cypress" />

describe('API Performance Analytics E2E Tests', () => {
	const AUTH_API_URL = '/api/v1/auth';
    const ANALYTICS_API_URL = '/api/v1/analytics/api-performance';

    const mockData = [
        {
            timeStamp: '2026-03-23T10:00:00',
            totalRequests: 100,
            totalErrors: 5,
            totalSuccesses: 95,
            averageResponseTime: 200,
            mostDemandedEndpoints: [{ uri: '/api/v1/auth', count: 50 }]
        }
    ];

    beforeEach(() => {
		cy.viewport(1280, 720);
        cy.intercept('GET', `${AUTH_API_URL}/me`, {
            statusCode: 200,
            body: { username: 'adminUser', roles: ['ADMIN'] },
        }).as('getActiveUser');

		cy.visit('/');
		cy.wait('@getActiveUser');

        cy.intercept('GET', `${ANALYTICS_API_URL}?dateRange=1h`, {
            body: mockData
        }).as('getAnalytics');

        cy.visit('/analytics');
        cy.contains('button', /API/i).click();

        cy.wait('@getAnalytics');
    });

    it('should render all sections and charts correctly', () => {
        cy.get('h1').contains('API Analytics').should('be.visible');

        cy.contains('h2', /Health Ratio/i).should('be.visible');
        cy.contains('h2', /Performance History/i).should('be.visible');
        cy.contains('h2', /Endpoint Traffic Usage/i).should('be.visible');

        cy.get('canvas').should('have.length', 3);
    });

    it('should toggle all export checkboxes when "Select All" is clicked', () => {
        cy.get('.download-checkbox').contains('Select All').click();

        cy.get('mat-checkbox').each(($el) => {
            cy.wrap($el).should('have.class', 'mat-mdc-checkbox-checked');
        });
    });
});