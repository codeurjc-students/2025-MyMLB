import { Pictures } from "./pictures.model";

export type PitcherGlobal = {
	name: string;
	playerNumber: number;
	teamName: string;
	position: string;
	games: number;
	wins: number;
	losses: number;
	era: number;
	inningsPitched: number;
	totalStrikeouts: number;
	walks: number;
	hitsAllowed: number;
	runsAllowed: number;
	whip: number;
	saves: number;
	saveOpportunities: number;
	picture: Pictures;
};

export type Pitcher = Omit<PitcherGlobal, 'teamName'>;

export type CreatePitcherRequest = {
	name: string,
	playerNumber: number,
	teamName: string,
	position: string
}

export type EditPitcherRequest = {
	teamName?: string,
	playerNumber?: number,
	position?: string,
	games?: number,
	wins?: number,
	losses?: number,
	inningsPitched?: number,
	totalStrikeouts?: number,
	walks?: number,
	hitsAllowed?: number,
	runsAllowed?: number,
	saves?: number,
	saveOpportunities?: number,
	isApiDataSource?: boolean
}