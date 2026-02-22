import { Pictures } from "./pictures.model"

export type Stadium = {
	name: string,
	openingDate: number,
	teamName: string,
	pictures: Pictures[]
}

export type StadiumSummary = Omit<Stadium, 'teamName'>;

export type CreateStadiumRequest = {
	name: string,
	openingDate: number
}