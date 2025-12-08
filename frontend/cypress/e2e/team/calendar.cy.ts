/// <reference types="cypress" />

import { TeamInfo } from './../../../src/app/models/team.model';

declare global {
	interface Window {
		__mockTeam__?: any;
		SelectedTeamServiceInit?: (serviceInstance: any) => void;
	}
}

export {};

describe('Team Calendar E2E Tests', () => {
	let teamData: TeamInfo;

	beforeEach(() => {
		cy.fixture('matches.json').then((matches) => {
			const now = new Date();
			const currentMonth = now.getMonth();
			const currentYear = now.getFullYear();

			const adjustedDate = new Date(currentYear, currentMonth, 15, 18, 0, 0);

			matches[0].date = adjustedDate.toISOString();

			cy.intercept('GET', '**/api/v1/matches/team/**', matches).as('getMatches');
		});

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

		cy.get('button').contains('Show Calendar').click();
		cy.wait('@getMatches');
		cy.get('app-calendar').should('exist').and('be.visible');
	});

	it('should render the current month and year in the header', () => {
		cy.get('app-calendar h2')
			.invoke('text')
			.should('match', /\w+ \d{4}/);
	});

	it('should navigate to previous and next month', () => {
		cy.get('app-calendar h2').then(($header) => {
			const initialMonth = $header.text();

			cy.get('app-calendar button').contains('←').click();
			cy.get('app-calendar h2').should(($newHeader) => {
				expect($newHeader.text()).not.to.eq(initialMonth);
			});

			cy.get('app-calendar button').contains('→').click();
			cy.get('app-calendar h2').should(($newHeader) => {
				expect($newHeader.text()).to.eq(initialMonth);
			});
		});
	});

	it('should open match miniature modal when clicking a match', () => {
		cy.get('#match-cell-container').first().click();
		cy.get('app-match-miniature').should('exist').and('be.visible');
	});

	it('should close the calendar when clicking Close button', () => {
		cy.get('app-calendar button').contains('Close').click();
		cy.get('app-calendar').should('not.exist');
	});
});