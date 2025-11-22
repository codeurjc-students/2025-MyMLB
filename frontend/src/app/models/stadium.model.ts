import { Pictures } from "./pictures.model"

export type StadiumSummary = {
	name: string,
	openingDate: number,
	pictures: Pictures[]
}

export type Stadium = {
	name: string,
	openingDate: number,
	teamName: string,
	pictures: Pictures[]
}