import { Seat } from "./event.model"

export type Ticket = {
	id: number,
	awayTeamName: string,
	homeTeamName: string,
	stadiumName: string,
	price: number,
	matchDate: Date,
	sectorName: string,
	seatName: string
}

export type PurchaseRequest = {
	eventManagerId: number,
	ticketAmount: number,
	seats: Seat[],
	ownerName: string,
	cardNumber: string,
	cvv: string,
	expirationDate: string
}