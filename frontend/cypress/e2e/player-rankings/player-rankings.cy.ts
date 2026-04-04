/// <reference types="cypress" />

describe('Player Rankings E2E Tests', () => {
    const RANKINGS_API_URL = '/api/v1/players/rankings/all*';
    const TEAMS_API_URL = '/api/v1/teams/standings';

    const mockRankings = {
        average: [
            { name: 'Aaron Judge', picture: 'https://judge.png', stat: 0.322 },
            { name: 'Juan Soto', picture: 'https://soto.png', stat: 0.288 }
        ],
        homeRuns: [
            { name: 'Shohei Ohtani', picture: 'https://ohtani.png', stat: 54 },
            { name: 'Aaron Judge', picture: 'https://judge.png', stat: 51 }
        ]
    };

    const mockTeams = {
        'AL': { 'EAST': [{ name: 'New York Yankees', abbreviation: 'NYY', league: 'AL', division: 'EAST' }] }
    };

    beforeEach(() => {
        cy.viewport(1440, 900);

        cy.intercept('GET', RANKINGS_API_URL, { body: mockRankings }).as('getRankings');
        cy.intercept('GET', TEAMS_API_URL, { body: mockTeams }).as('getTeams');

        cy.visit('/player-rankings?playerType=position');
        cy.wait(['@getRankings', '@getTeams']);
    });

    it('should change the number of players shown when TOP buttons are clicked', () => {
        cy.contains('button', 'TOP 10').click();
        cy.contains('button', 'TOP 10').should('have.class', 'bg-indigo-600');
   		cy.get('table tbody').first().find('tr').should('have.length.at.most', 10);
    });

    it('should filter by league and division', () => {
        cy.intercept('GET', RANKINGS_API_URL).as('getFilteredRankings');

        cy.get('mat-select').eq(1).click();
        cy.get('mat-option').contains('AL').click();

        cy.wait('@getFilteredRankings');

        cy.get('mat-select').eq(2).click();
        cy.get('mat-option').contains('EAST').click();

        cy.wait('@getFilteredRankings');
        cy.get('h1').should('contain', 'Batting Leaders');
    });
});