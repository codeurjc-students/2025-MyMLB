/// <reference types="cypress" />

import { TeamInfo } from './../../../src/app/models/team-info.model';

declare global {
	interface Window {
		__mockTeam__?: any;
		SelectedTeamServiceInit?: (serviceInstance: any) => void;
	}
}

export {};

describe('Stats Panel E2E Tests', () => {
	let teamData: TeamInfo;

	beforeEach(() => {
		cy.fixture('team-info').then((mockTeam) => {
			teamData = mockTeam;

			cy.fixture('standings').then((mockStandings) => {
				cy.intercept('GET', '**/api/teams/standings', {
					statusCode: 200,
					body: mockStandings,
				}).as('mockStandings');
			});

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

			cy.wait('@mockStandings');
		});
	});

	it('should display the team stats section with correct fields', () => {
		cy.get('#team-stats')
			.should('exist')
			.and('be.visible')
			.within(() => {
				cy.contains('Season Statistics').should('be.visible');
				cy.contains('Wins').should('be.visible');
				cy.contains(teamData.teamStats.wins).should('be.visible');

				cy.contains('PCT').should('be.visible');
				cy.contains(teamData.teamStats.pct).should('be.visible');

				cy.get('#rank').contains('1');
				cy.contains('Rank').should('be.visible');
			});
	});

	it('should open player stats panel when clicking a position player card', () => {
		cy.get('#position-player-card').should('be.visible').click();

		cy.get('#player-stats')
			.should('exist')
			.and('be.visible')
			.within(() => {
				cy.contains('Position').should('be.visible');
				cy.get('img').should('have.attr', 'alt').and('not.be.empty');
			});
	});

	it('should open pitcher stats panel when clicking a pitcher card', () => {
		cy.get('#pitcher-card').first().click();

		cy.get('#player-stats')
			.should('exist')
			.and('be.visible')
			.within(() => {
				cy.contains('ERA').should('be.visible');
				cy.contains('Strikeouts').should('be.visible');
			});
	});

	it('should close the stats panel when pressing Escape key', () => {
		cy.get('#position-player-card').click();
		cy.get('#player-stats').should('be.visible');

		cy.get('body').type('{esc}');
		cy.get('#player-stats').should('not.exist');
	});

	it('should close the stats panel when pressing the "x" button', () => {
		cy.get('#position-player-card').click();
		cy.get('#player-stats').should('be.visible');

		cy.get('#close-button').click();
		cy.get('#player-stats').should('not.exist');
	});
});