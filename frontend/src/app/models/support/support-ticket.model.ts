export type SupportTicket = {
	id: string,
	subject: string,
	status: 'OPEN' | 'ANSWERED' | 'CLOSED',
	creationDate: Date
}