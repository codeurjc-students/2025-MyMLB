import { Pictures } from "./pictures.model"

export type PositionPlayerGlobal = {
	name: string,
	playerNumber: number,
	teamName: string,
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
	slugging: number,
	picture: Pictures
}

export type PositionPlayer = Omit<PositionPlayerGlobal, 'teamName'>;

export type CreatePlayerRequest = {
	name: string,
	playerNumber: number,
	teamName: string,
	position: string
}

export type EditPositionPlayerRequest = {
	teamName?: string,
	playerNumber?: number,
	position?: string,
	atBats?: number,
	walks?: number,
	hits?: number,
	doubles?: number,
	triples?: number,
	rbis?: number,
	homeRuns?: number
}