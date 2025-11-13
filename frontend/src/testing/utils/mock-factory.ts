import { SimplifiedTeam } from './../../app/services/team.service';
import { Pitcher } from '../../app/models/pitcher.model';
import { PositionPlayer } from '../../app/models/position-player.model';
import { StadiumSummary } from '../../app/models/stadium-summary.model';
import { TeamInfo } from '../../app/models/team-info.model';
import { Team } from '../../app/models/team.model';
import { User } from '../../app/models/user.model';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { UserRole } from '../../app/models/auth/user-role.model';

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

	static buildSimpliefiedTeamMock = (name: string, abbreviation: string, league: string, division: string) => {
		return {
			name: name,
			abbreviation: abbreviation,
			league: league,
			division: division
		} as SimplifiedTeam;
	};

	static buildStadiumMock = (name: string, year: number) => {
		return {
			name: name,
			openingDate: year,
		} as StadiumSummary;
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
}
