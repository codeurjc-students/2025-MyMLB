import { StadiumSummary } from "./stadium-summary.model"
import { Pitcher } from "./pitcher.model"
import { PositionPlayer } from "./position-player.model"
import { Team } from "./team.model"

export type TeamInfo = {
	info: Team,
	city: string,
	informationText: string,
	chamiponships: number[],
	stadium: StadiumSummary,
	positionPlayers: PositionPlayer[],
	pitchers: Pitcher[]
}