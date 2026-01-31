import { Pictures } from "../pictures.model"
import { EventManager } from "./event-manager.model"

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