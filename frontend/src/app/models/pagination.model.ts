import { PitcherGlobal } from "./pitcher.model";
import { PositionPlayerGlobal } from "./position-player.model";
import { Stadium } from "./stadium.model";
import { TeamInfo, TeamSummary } from "./team.model";

export type PaginatedSearchs = {
	content: (TeamInfo | Stadium | PositionPlayerGlobal | PitcherGlobal) [];
	page: {
		size: number;
		number: number;
		totalElements: number;
		totalPages: number;
	};
};

export type PaginatedStadiums = {
	content: Stadium[];
	page: {
		size: number;
		number: number;
		totalElements: number;
		totalPages: number;
	};
};

export type PaginatedTeamSummary = {
	content: TeamSummary[];
	page: {
		size: number;
		number: number;
		totalElements: number;
		totalPages: number;
	};
};