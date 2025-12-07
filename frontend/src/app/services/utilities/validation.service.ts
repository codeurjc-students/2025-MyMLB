import { PositionPlayer, PositionPlayerGlobal } from '../../models/position-player.model';
import { Stadium } from '../../models/stadium.model';
import { TeamInfo, TeamSummary } from '../../models/team.model';
import { Pitcher, PitcherGlobal } from './../../models/pitcher.model';
import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ValidationService {
	constructor() {}

	public isTeamSummary(elem: any): elem is TeamSummary {
		return 'abbreviation' in elem;
	}

	public isTeamInfo(elem: any): elem is TeamInfo {
		return 'teamStats' in elem;
	}

	public isStadium(elem: any): elem is Stadium {
		return 'openingDate' in elem;
	}

	public isPitcher(player: any): player is (PitcherGlobal | Pitcher) {
		return 'era' in player;
	}

	public isPositionPlayer(player: any): player is (PositionPlayer | PositionPlayerGlobal) {
		return 'ops' in player;
	}
}