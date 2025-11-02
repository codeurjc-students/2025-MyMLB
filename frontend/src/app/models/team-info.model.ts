import { StadiumSummary } from "./stadium-summary.model"
import { Pitcher } from "./pitcher.model"
import { PositionPlayer } from "./position-player.model"
import { Team } from "./team.model"

export type TeamInfo = {
	teamStats: Team,
	city: string,
	generalInfo: string,
	championships: number[],
	stadium: StadiumSummary,
	positionPlayers: PositionPlayer[],
	pitchers: Pitcher[]
}