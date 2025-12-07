/// <reference types="cypress" />

import { TeamInfo } from './../../../src/app/models/team.model';

declare global {
	interface Window {
		__mockTeam__?: any;
		SelectedTeamServiceInit?: (serviceInstance: any) => void;
	}
}

export {};

describe('Team Page E2E Tests', () => {
	let teamData: TeamInfo;

	beforeEach(() => {
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
		});
	});

	it('should display all the elements of the team page', () => {
		cy.get('h1', { timeout: 10000 }).contains(teamData.teamStats.name).should('be.visible');
		cy.get('img[alt="Team Logo"]').should('be.visible');

		cy.get('button').contains('Show Calendar').click();
		cy.get('app-calendar').should('exist').and('be.visible');
		cy.get('app-calendar button').contains('Close').click();

		cy.get('h2').contains('About the Team').should('be.visible');
		cy.get('p').contains(teamData.generalInfo).should('be.visible');

		cy.get('h2').contains('Championships').should('be.visible');
		cy.get('img[alt="WS Trophy"]').should('have.length.at.least', 1);

		cy.get('h2').contains('Stadium').should('be.visible');
		cy.get('p').contains(`Opening Date: ${teamData.stadium.openingDate}`).should('be.visible');
		cy.get('img[alt="Stadium image"]').should('be.visible');

		cy.get('h2').contains('Position Players').should('be.visible');
		cy.get('#position-player-card').should('be.visible').click();
		cy.get('app-stats-panel').should('be.visible');
	});
});