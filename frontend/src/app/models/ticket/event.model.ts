import { Pictures } from "../pictures.model"

export type EventResponse = {
	id: number,
	awayTeamName: string,
	homeTeamName: string,
	homeTeamAbbreviation: string,
	stadiumName: string,
	date: Date,
	pictureMap: Pictures,
	sectors: EventManager[]
}

export type EventManager = {
	id: number,
	sectorId: number,
	sectorName: string,
	price: number,
	availability: number,
	totalCapacity: number
}

export type Seat = {
	id: number,
	name: string
}

export type SectorCreateRequest = {
	name: string,
	totalCapacity: number
}

export type EventCreateRequest = {
	matchId: number,
	prices: number[],
	sectors: SectorCreateRequest[]
}

export type EventEditRequest = {
	eventId: number,
	sectorIds: number[],
	prices: number[]
}