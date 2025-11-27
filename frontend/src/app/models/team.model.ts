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
	pct: number,
	gamesBehind: number,
	lastTen: string
}

export type TeamSummary = {
	name: string;
	abbreviation: string;
	league: string;
	division: string;
};

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