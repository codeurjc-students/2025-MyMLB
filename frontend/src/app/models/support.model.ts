export type SupportTicket = {
	id: number,
	subject: string,
	status: 'OPEN' | 'ANSWERED' | 'CLOSED',
	creationDate: Date
}

export type SupportMessage = {
	id: number,
	senderEmail: string,
	body: string,
	fromUser: string,
	creationDate: Date
}

export type CreateTicketRequest = {
	email: string,
	subject: string,
	body: string
}

export type ReplyRequest = {
	adminEmail: string,
	body: string
}