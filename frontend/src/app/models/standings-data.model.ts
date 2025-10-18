import { Team } from "./team-model";

export interface StandingsData {
	league: string,
	division: string,
	teams: Team[]
}