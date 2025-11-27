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
			cy.intercept('GET', '**/api/v1/matches/home/**', matches).as('getHomeMatches');
			cy.intercept('GET', '**/api/v1/matches/away/**', matches).as('getAwayMatches');
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
		cy.wait(['@getHomeMatches', '@getAwayMatches']);
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

	it('should render 7 columns for days of the week', () => {
		cy.get('app-calendar .grid.grid-cols-7').first().children().should('have.length', 7);
		cy.get('app-calendar .grid.grid-cols-7')
			.first()
			.children()
			.eq(0)
			.should('contain.text', 'Mon');
	});

	it('should render day cells for the full month grid', () => {
		cy.get('app-calendar .grid.grid-cols-7')
			.last()
			.children()
			.should('have.length.greaterThan', 28);
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