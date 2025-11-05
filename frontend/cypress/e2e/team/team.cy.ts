/// <reference types="cypress" />

import { TeamInfo } from './../../../src/app/models/team-info.model';

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

	it('should display the team name and logo', () => {
		cy.get('h1', { timeout: 10000 }).contains(teamData.teamStats.name).should('be.visible');

		cy.get('img[alt="Team Logo"]').should('be.visible');
	});

	it('should display the team’s information text', () => {
		cy.get('h2').contains('About the Team').should('be.visible');
		cy.get('p').contains(teamData.generalInfo).should('be.visible');
	});

	it('should display the championships section', () => {
		cy.get('h2').contains('Championships').should('be.visible');
		cy.get('img[alt="WS Trophy"]').should('have.length.at.least', 1);
	});

	it('should display the stadium section', () => {
		cy.get('h2').contains('Stadium').should('be.visible');
		cy.get('p').contains(`Opening Date: ${teamData.stadium.openingDate}`).should('be.visible');
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