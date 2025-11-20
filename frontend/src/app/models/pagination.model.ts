import { PitcherGlobal } from "./pitcher.model";
import { PositionPlayerGlobal } from "./position-player.model";
import { Stadium } from "./stadium.model";
import { Team } from "./team.model";

export type PaginatedSearchs = {
	content: Team[] | Stadium[] | PositionPlayerGlobal[] | PitcherGlobal[];
	page: {
		size: number;
		number: number;
		totalElements: number;
		totalPages: number;
	};
};