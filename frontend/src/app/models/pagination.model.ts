import { PitcherGlobal } from "./pitcher.model";
import { PositionPlayerGlobal } from "./position-player.model";
import { Stadium } from "./stadium.model";
import { TeamInfo } from "./team.model";

export type PaginatedSearchs = {
	content: TeamInfo[] | Stadium[] | PositionPlayerGlobal[] | PitcherGlobal[];
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