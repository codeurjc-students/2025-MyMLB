import { ChangeDetectionStrategy, Component } from '@angular/core';
import { StandingsWidgetComponent } from "../standings/standings-widget/standings-widget.component";
import { CommonModule } from '@angular/common';
import { MatchesOfTheDayComponent } from "../match/matches-of-the-day/matches-of-the-day.component";
import { PlayerRankingsWidgetComponent } from '../player-rankings/player-rankings-widget/player-rankings-widget.component';

@Component({
	selector: 'app-home',
	standalone: true,
	templateUrl: './home.component.html',
	changeDetection: ChangeDetectionStrategy.Default,
 	imports: [StandingsWidgetComponent, PlayerRankingsWidgetComponent, CommonModule, MatchesOfTheDayComponent]
})
export class HomeComponent {}