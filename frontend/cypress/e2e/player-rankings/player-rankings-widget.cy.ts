/// <reference types="cypress" />

describe('Player Rankings Widget E2E Tests', () => {
    const RANKINGS_API_URL = '/api/v1/players/rankings*';

    const mockPositionRankings = {
        content: [
            { name: 'Aaron Judge', picture: 'https://judge.png', stat: 0.322 },
            { name: 'Juan Soto', picture: 'https://soto.png', stat: 0.288 }
        ]
    };

    beforeEach(() => {
        cy.viewport(1280, 720);

        cy.intercept('GET', RANKINGS_API_URL, {
            body: mockPositionRankings
        }).as('getRankings');

        cy.visit('/');
        cy.wait('@getRankings');
    });

	it('should render the hitting leaders ranking table correctly', () => {
        cy.get('app-player-rankings-widget').first().within(() => {
            cy.get('h2').should('contain', 'Hitting Leaders');
            cy.get('table thead tr').within(() => {
                cy.get('th').contains('#').should('be.visible');
                cy.get('th').contains('Player').should('be.visible');
                cy.get('th').contains('AVG').should('be.visible');
            });

            cy.get('tbody tr').should('have.length', 2);

            cy.get('tbody tr').first().within(() => {
                cy.get('td').contains('1');
                cy.get('img').should('have.attr', 'src', 'https://judge.png');
                cy.get('span').contains('Aaron Judge');
                cy.get('td').contains('.322');
            });
        });
    });

    it('should navigate to player rankings page when "See All Rankings" is clicked', () => {
        cy.get('button').contains('See All Rankings').click();

        cy.url().should('include', '/player-rankings');
        cy.url().should('include', 'playerType=position');
    });
});