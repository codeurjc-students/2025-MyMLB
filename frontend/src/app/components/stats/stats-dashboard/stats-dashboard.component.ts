import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { VisibilityComponent } from "../visibility/visibility.component";
import { FavTeamsAnalyticsComponent } from "../fav-teams-analytics/fav-teams-analytics.component";
import { ApiPerformanceComponent } from "../api-performance/api-performance.component";
import { CacheComponent } from "../cache/cache.component";

type StatsView = 'visibility' | 'activity' | 'favTeams' | 'api' | 'cache' | null;

@Component({
	selector: 'app-stats-dashboard',
	imports: [CommonModule, MatIconModule, VisibilityComponent, FavTeamsAnalyticsComponent, ApiPerformanceComponent, CacheComponent],
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './stats-dashboard.component.html'
})
export class StatsDashboardComponent {
	public currentView: StatsView = null;

	public options = [
		{
			label: 'Visibility',
			subtitle: 'Growth rate, views and churn analysis',
			iconName: 'visibility',
			iconStyles: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400',
			arrowStyles: 'group-hover:text-blue-500',
			view: 'visibility'
		},
		{
			label: 'API Perfomance',
			subtitle: 'API response times and server health',
			iconName: 'speed',
			iconStyles: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
			arrowStyles: 'group-hover:text-amber-500',
			view: 'api'
		},
		{
			label: 'Favorite Teams',
			subtitle: 'Analysis of user preferences',
			iconName: 'star',
			iconStyles: 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-600 dark:text-cyan-400',
			arrowStyles: 'group-hover:text-cyan-500',
			view: 'favTeams'
		},
		{
			label: 'Caches',
			subtitle: 'Manually restore the active caches of the application',
			iconName: 'storage',
			iconStyles: 'bg-violet-100 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400',
			arrowStyles: 'group-hover:text-violet-500',
			view: 'cache'
		}
	];

	public switchView(view: string) {
		const processedView = view as StatsView;
		this.currentView = processedView;
	}
}