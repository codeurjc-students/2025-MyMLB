/// <reference types="cypress" />

describe('Cache Management E2E Tests', () => {
    const AUTH_API_URL = '/api/v1/auth';
    const CACHE_API_URL = '/api/v1/cache';

    beforeEach(() => {
        cy.intercept('GET', `${AUTH_API_URL}/me`, {
            statusCode: 200,
            body: {
                username: 'adminUser',
                roles: ['ADMIN'],
            },
        }).as('getActiveUser');

        cy.visit('/');
        cy.wait('@getActiveUser');

        cy.intercept('GET', CACHE_API_URL, {
            body: ['get-users', 'get-teams', 'get-players']
        }).as('getCaches');

        cy.visit('/analytics');
        cy.contains('button', /Caches/i).click();
        cy.wait('@getCaches');
    });

    it('should render the cache management page correctly', () => {
        cy.get('h1').contains('Cache Management').should('be.visible');
        cy.get('h2').contains('System Caches').should('be.visible');

        cy.get('tbody tr').should('have.length', 3);
        cy.get('tbody').contains('get-users').should('be.visible');
    });

    it('should clear a single cache and show success modal', () => {
        cy.intercept('DELETE', `${CACHE_API_URL}/get-users`, {
            statusCode: 200,
            body: { status: 'SUCCESS', message: 'OK' }
        }).as('clearSingle');

        cy.get('tbody tr').first().find('button').click();
        cy.wait('@clearSingle');

        cy.get('app-success-modal').should('be.visible');
        cy.get('app-success-modal').contains('Cache get-users successfully restored');
    });
});