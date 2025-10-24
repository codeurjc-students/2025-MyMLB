/// <reference types="cypress" />

describe('Matches Of The Day Component E2E Tests', () => {
	const baseUrl = '/';

	const interceptMatches = (body: any, statusCode = 200, alias = 'getMatches') => {
		cy.intercept('GET', '/api/matches/today*', { statusCode, body }).as(alias);
	};

	const generateMatch = (
		i: number,
		status: 'SCHEDULED' | 'FINISHED' | 'IN_PROGRESS' = 'SCHEDULED',
		homeAbbr: string = `T${i}`,
		awayAbbr: string = `O${i}`,
		homeScore: number = i,
		awayScore: number = i + 1,
		date: string = `2025-10-22T19:${String(i).padStart(2, '0')}:00`
	) => ({
		homeTeam: { name: `Team ${i}`, abbreviation: homeAbbr, league: 'AL', division: 'East' },
		awayTeam: { name: `Opponent ${i}`, abbreviation: awayAbbr, league: 'AL', division: 'East' },
		homeScore,
		awayScore,
		date,
		status,
	});

	it('should display the main title', () => {
		interceptMatches({
			content: [],
			page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
		});
		cy.visit(baseUrl);
		cy.contains("📅 Today's Schedule").should('be.visible');
	});

	it('should show error message when errorMessage is set', () => {
		interceptMatches({}, 500);
		cy.visit(baseUrl);
		cy.wait('@getMatches');
		cy.contains('Error trying to show the matches').should('be.visible');
	});

	it('should show no matches message when matches is empty', () => {
		interceptMatches({
			content: [],
			page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
		});
		cy.visit(baseUrl);
		cy.wait('@getMatches');
		cy.contains('No matches scheduled for today').should('be.visible');
	});

	it('should render matches correctly', () => {
		const mockMatch = {
			content: [generateMatch(0, 'FINISHED', 'NYY', 'BOS', 5, 3, '2025-10-22T19:05:00')],
			page: { size: 10, number: 0, totalElements: 1, totalPages: 1 },
		};

		interceptMatches(mockMatch);
		cy.visit(baseUrl);
		cy.wait('@getMatches');

		cy.contains('NYY').should('be.visible');
		cy.contains('BOS').should('be.visible');
		cy.contains('3 - 5').should('be.visible');
		cy.contains('Final').should('be.visible');
	});

	it('should show Load More Games button when hasMore is true', () => {
		const mockMatch = {
			content: Array.from({ length: 10 }, (_, i) =>
				generateMatch(i, 'SCHEDULED', `T${i}`, `O${i}`, i, i + 1)
			),
			page: { size: 10, number: 0, totalElements: 15, totalPages: 2 },
		};

		interceptMatches(mockMatch);
		cy.visit(baseUrl);
		cy.wait('@getMatches');
		cy.contains('⚾ Load More Games').should('be.visible');
	});

	it('should load more matches when Load More button is clicked', () => {
		const page0 = {
			content: Array.from({ length: 10 }, (_, i) =>
				generateMatch(i, 'SCHEDULED', `T${i}`, `O${i}`, i, i + 1)
			),
			page: { size: 10, number: 0, totalElements: 15, totalPages: 2 },
		};

		const page1 = {
			content: Array.from({ length: 5 }, (_, i) =>
				generateMatch(i + 10, 'SCHEDULED', `T${i + 10}`, `O${i + 10}`, i + 10, i + 11)
			),
			page: { size: 10, number: 1, totalElements: 15, totalPages: 2 },
		};

		cy.intercept('GET', /\/api\/matches\/today\?page=\d+.*/, (req) => {
			const pageParam = new URLSearchParams(req.url.split('?')[1]).get('page');
			if (pageParam === '0') {
				req.reply({ statusCode: 200, body: page0 });
			} else if (pageParam === '1') {
				req.reply({ statusCode: 200, body: page1 });
			}
		}).as('getMatchesPage');

		cy.visit(baseUrl);
		cy.wait('@getMatchesPage');

		cy.contains('⚾ Load More Games').click();
		cy.wait('@getMatchesPage');

		cy.contains('T14').should('be.visible');
		cy.contains('O14').should('be.visible');
	});
});