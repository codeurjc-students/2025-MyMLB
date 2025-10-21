import { Component } from '@angular/core';
import { StandingsComponent } from "../standings/standings.component";
import { CommonModule } from '@angular/common';
import { MatchesOfTheDayComponent } from "../match/matches-of-the-day/matches-of-the-day.component";

@Component({
	selector: 'app-home',
	standalone: true,
	templateUrl: './home.component.html',
 	imports: [StandingsComponent, CommonModule, MatchesOfTheDayComponent]
})
export class HomeComponent {
	constructor() {}
}