import { StadiumSummary } from "./stadium.model"
import { Pitcher } from "./pitcher.model"
import { PositionPlayer } from "./position-player.model"

export type Team = {
	name: string,
	abbreviation: string,
	league: string,
	division: string,
	totalGames: number,
	wins: number,
	losses: number,
	pct: string,
	gamesBehind: number,
	lastTen: string
}

export type TeamSummary = Pick<Team, 'name' | 'abbreviation' | 'league' | 'division'>;

export type TeamInfo = {
	teamStats: Team,
	city: string,
	generalInfo: string,
	championships: number[],
	stadium: StadiumSummary,
	positionPlayers: PositionPlayer[],
	pitchers: Pitcher[]
}

export type UpdateTeamRequest = {
	city?: string,
	newChampionship?: number,
	newInfo?: string,
	newStadiumName?: string
}

export type WinsPerRival = {
	rivalTeamName: string,
	gamesPlayed: number,
	wins: number
}

export type RunStats = {
	teamName: string,
	runsScored: number,
	runsAllowed: number
}

export type WinsDistribution = {
	teamName: string,
	homeGames: number,
	homeWins: number,
	roadGames: number,
	roadWins: number,
	homeWinPct: number,
	roadWinPct: number
}

export type HistoricRanking = {
	teamName: string,
	matchDate: Date,
	rank: number,
	wins: number,
	losses: number
}