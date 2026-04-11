/// <reference types="cypress" />

import { TeamInfo } from './../../../src/app/models/team.model';

declare global {
    interface Window {
        __mockTeam__?: any;
        SelectedTeamServiceInit?: (serviceInstance: any) => void;
    }
}

export {};

describe('Team Statistics E2E Tests', () => {
    let teamData: TeamInfo;

	beforeEach(() => {
        cy.intercept('GET', '**/analytics/win-distribution').as('getWinDist');
        cy.intercept('GET', '**/rivals').as('getRivals');

        cy.fixture('team-info').then((mockTeam) => {
            teamData = mockTeam;

            cy.visit(`/team/${encodeURIComponent(teamData.teamStats.name)}`, {
                onBeforeLoad(win) {
                    win.__mockTeam__ = teamData;
                    Object.defineProperty(win, 'SelectedTeamServiceInit', {
                        value: (serviceInstance: any) => {
                            serviceInstance.setSelectedTeam(win.__mockTeam__);
                        },
                        writable: true,
                    });
                },
            });
            cy.get('button').contains('Stats').click();
            cy.wait(['@getWinDist', '@getRivals']);
        });
    });

    it('should filter by league and display its clear button', () => {
        cy.get('mat-select').first().click();
        cy.get('mat-option').contains('AL').click();

        cy.get('button[matTooltip="Clear league filter"]').should('be.visible');

        cy.get('button[matTooltip="Clear league filter"]').click();
        cy.get('button[matTooltip="Clear league filter"]').should('not.exist');
    });

	it('should display all four charts', () => {
        const charts = ['Historic Ranking', 'Win Distribution', 'Wins vs Rivals', 'Runs Analysis'];

        charts.forEach(chartTitle => {
            cy.get('h2').contains(chartTitle).should('be.visible');
        });

        cy.get('canvas').each(($canvas) => {
            cy.wrap($canvas).should('be.visible')
              .and($c => {
                  expect($c.width()).to.be.gt(0);
                  expect($c.height()).to.be.gt(0);
              });
        });
    });
});