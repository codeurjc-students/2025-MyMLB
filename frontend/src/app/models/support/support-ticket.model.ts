export type SupportTicket = {
	id: number,
	subject: string,
	status: 'OPEN' | 'ANSWERED' | 'CLOSED',
	creationDate: Date
}