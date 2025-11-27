import { Pitcher, PitcherGlobal } from '../../app/models/pitcher.model';
import { PositionPlayer } from '../../app/models/position-player.model';
import { Stadium, StadiumSummary } from '../../app/models/stadium.model';
import { Team, TeamInfo, TeamSummary } from '../../app/models/team.model';
import { User } from '../../app/models/user.model';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { UserRole } from '../../app/models/auth/user-role.model';
import { ShowMatch } from '../../app/services/match.service';
import { Pictures } from '../../app/models/pictures.model';
import { PaginatedSearchs } from '../../app/models/pagination.model';

export class MockFactory {
	static buildUserMocks = (username: string, email: string) => {
		return {
			username: username,
			email: email
		} as User;
	};

	static buildMockResponse = (status: string, message: string) => {
		return {
			status: status,
			message: message
		} as AuthResponse;
	};

	static buildMockUserRole = (username: string, roles: string[]) => {
		return {
			username: username,
			roles: roles
		} as UserRole;
	};

	static buildTeamMocks = (
		name: string,
		abbreviation: string,
		league: string,
		division: string,
		totalGames: number,
		wins: number,
		losses: number,
		pct: number,
		gamesBehind: number,
		lastTen: string
	) => {
		return {
			name: name,
			abbreviation: abbreviation,
			league: league,
			division: division,
			totalGames: totalGames,
			wins: wins,
			losses: losses,
			pct: pct,
			gamesBehind: gamesBehind,
			lastTen: lastTen,
		} as Team;
	};

	static buildTeamSummaryMock = (name: string, abbreviation: string, league: string, division: string) => {
		return {
			name: name,
			abbreviation: abbreviation,
			league: league,
			division: division
		} as TeamSummary;
	};

	static buildStadiumMock = (name: string, year: number, pictures: Pictures[]) => {
		return {
			name: name,
			openingDate: year,
			pictures: pictures
		} as StadiumSummary;
	};

	static buildStadiumCompleteMock = (name: string, year: number, teamName: string, pictures: Pictures[]) => {
		return {
			name: name,
			openingDate: year,
			teamName: teamName,
			pictures: pictures
		} as Stadium;
	};

	static buildPositionPlayerMock = (
		name: string,
		position: string,
		atBats: number,
		walks: number,
		hits: number,
		doubles: number,
		triples: number,
		homeRuns: number,
		rbis: number,
		average: number,
		obp: number,
		ops: number,
		slugg: number
	) => {
		return {
			name: name,
			position: position,
			atBats: atBats,
			walks: walks,
			hits: hits,
			doubles: doubles,
			triples: triples,
			homeRuns: homeRuns,
			rbis: rbis,
			average: average,
			obp: obp,
			ops: ops,
			slugging: slugg,
		} as PositionPlayer;
	};

	static buildPitcherMock = (
		name: string,
		position: string,
		games: number,
		wins: number,
		losses: number,
		era: number,
		innings: number,
		so: number,
		walks: number,
		ha: number,
		ra: number,
		whip: number,
		saves: number,
		savesOp: number
	) => {
		return {
			name: name,
			position: position,
			games: games,
			wins: wins,
			losses: losses,
			era: era,
			inningsPitched: innings,
			totalStrikeouts: so,
			walks: walks,
			hitsAllowed: ha,
			runsAllowed: ra,
			whip: whip,
			saves: saves,
			saveOpportunities: savesOp,
		} as Pitcher;
	};

	static buildPitcherGlobalMock = (
		name: string,
		teamName: string,
		position: string,
		games: number,
		wins: number,
		losses: number,
		era: number,
		innings: number,
		so: number,
		walks: number,
		ha: number,
		ra: number,
		whip: number,
		saves: number,
		savesOp: number
	) => {
		return {
			name: name,
			teamName: teamName,
			position: position,
			games: games,
			wins: wins,
			losses: losses,
			era: era,
			inningsPitched: innings,
			totalStrikeouts: so,
			walks: walks,
			hitsAllowed: ha,
			runsAllowed: ra,
			whip: whip,
			saves: saves,
			saveOpportunities: savesOp,
		} as PitcherGlobal;
	};

	static buildTeamInfoMock = (
		team: Team,
		city: string,
		info: string,
		championships: number[],
		stadium: StadiumSummary,
		player: PositionPlayer[],
		pitcher: Pitcher[]
	) => {
		return {
			teamStats: team,
			city: city,
			generalInfo: info,
			championships: championships,
			stadium: stadium,
			positionPlayers: player,
			pitchers: pitcher,
		} as TeamInfo;
	};

	static buildShowMatchMock = (awayTeam: TeamSummary, homeTeam: TeamSummary, awayScore: number, homeScore: number, date: string, status: string) => {
		return {
			awayTeam: awayTeam,
			homeTeam: homeTeam,
			awayScore: awayScore,
			homeScore: homeScore,
			date: date,
			status: status
		} as ShowMatch;
	};

	static buildPaginatedSearchsForTeam = (team: TeamInfo) => {
		return {
			content: [
				team
			],
			page: {
				size: 10,
				number: 0,
				totalElements: 1,
				totalPages: 1
			}
		} as PaginatedSearchs
	};

	static buildPaginatedSearchsForPlayer = (player: PitcherGlobal) => {
		return {
			content: [
				player
			],
			page: {
				size: 5,
				number: 2,
				totalElements: 1,
				totalPages: 1
			}
		} as PaginatedSearchs
	};
}