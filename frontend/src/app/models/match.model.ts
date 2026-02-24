import { TeamSummary } from "./team.model";

export type MatchStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED';

export type ShowMatch = {
	id: number,
	homeTeam: TeamSummary;
	awayTeam: TeamSummary;
	homeScore: number;
	awayScore: number;
	date: string;
	status: MatchStatus;
	stadiumName: string;
};