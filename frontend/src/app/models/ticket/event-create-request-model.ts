import { SectorCreateRequest } from "./sector-create-request.model"

export type EventCreateRequest = {
	matchId: number,
	prices: number[],
	sectors: SectorCreateRequest[]
}