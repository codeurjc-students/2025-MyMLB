import { Seat } from "./seat.model"

export type PurchaseRequest = {
	eventManagerId: number,
	ticketAmount: number,
	seats: Seat[],
	ownerName: string,
	cardNumber: string,
	cvv: string,
	expirationDate: Date
}