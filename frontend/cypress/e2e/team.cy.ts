/// <reference types="cypress"/>

describe('Team Page E2E Tests', () => {
    beforeEach(() => {
        cy.fixture('team-info').then((mockTeam) => {
            cy.intercept('GET', /\/api\/teams\/.*$/, {
                statusCode: 200,
                body: mockTeam,
            }).as('getTeam');

            cy.visit(`/team/${encodeURIComponent(mockTeam.teamStats.name)}`);
            cy.wait('@getTeam');

            cy.wrap(mockTeam).as('teamData'); // ← esto permite usar this.teamData
        });
    });

    it('should display the team name and logo', function () {
        cy.get('h1').contains(this.teamData.teamStats.name).should('be.visible');
        cy.get('img[alt="Team Logo"]').should('be.visible');
    });

    it('should display the team`s information text', function () {
        cy.get('h2').contains('About the Team').should('be.visible');
        cy.get('p').contains(this.teamData.generalInfo).should('be.visible');
    });

    it('should display the championships section', () => {
        cy.get('h2').contains('Championships').should('be.visible');
        cy.get('img[alt="WS Trophy"]').should('have.length.at.least', 1);
    });

    it('should display the stadium section', function () {
        cy.get('h2').contains('Stadium').should('be.visible');
        cy.get('p')
            .contains(`Opening Date: ${this.teamData.stadium.openingDate}`)
            .should('be.visible');
        cy.get('img[alt="Stadium image"]').should('be.visible');
    });

    it('should display the position players section', () => {
        cy.get('h2').contains('Position Players').should('be.visible');
        cy.get('#position-player-card').should('be.visible').click();
        cy.get('app-stats-panel').should('be.visible');
    });

    it('should display the pitchers section', () => {
        cy.get('h2').contains('Pitchers').should('be.visible');
        cy.get('#pitcher-card').should('be.visible').click();
        cy.get('app-stats-panel').should('be.visible');
    });
});