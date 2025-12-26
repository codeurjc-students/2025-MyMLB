import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class BackgroundColorService {
	constructor() {}

	public getBackgroundColor(abbreviation: string | undefined) {
		switch (abbreviation) {
			case 'TOR':
				return 'bg-sky-500';
			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				return 'bg-red-500';
			case 'TB':
			case 'KC':
				return 'bg-sky-300';
			case 'BAL':
			case 'HOU':
			case 'DET':
				return 'bg-orange-500';

			case 'CLE':
			case 'SEA':
			case 'TEX':
			case 'NYM':
			case 'ATL':
			case 'MIL':
			case 'MIN':
			case 'NYY':
			case 'BOS':
				return 'bg-blue-900';

			case 'MIA':
				return 'bg-sky-400';

			case 'CHC':
				return 'bg-blue-700';

			case 'ATH':
				return 'bg-green-900';

			case 'PIT':
			case 'CWS':
			case 'SF':
				return 'bg-black';

			case 'LAD':
				return 'bg-blue-600';

			case 'SD':
				return 'bg-yellow-500';

			case 'COL':
				return 'bg-purple-700';

			default:
				return 'bg-blue-100';
		}
	}

	public navBarBackground(abbreviation: string | undefined) {
		switch (abbreviation) {
			case 'TOR':
				return 'bg-sky-500';
			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				return 'bg-red-500';
			case 'TB':
			case 'KC':
				return 'bg-sky-300';
			case 'BAL':
			case 'HOU':
			case 'DET':
				return 'bg-orange-500';

			case 'CLE':
			case 'SEA':
			case 'TEX':
			case 'NYM':
			case 'ATL':
			case 'MIL':
			case 'MIN':
			case 'NYY':
			case 'BOS':
				return 'bg-blue-900';

			case 'MIA':
				return 'bg-sky-400';

			case 'CHC':
				return 'bg-blue-700';

			case 'ATH':
				return 'bg-green-900';

			case 'PIT':
			case 'CWS':
			case 'SF':
				return 'bg-black';

			case 'LAD':
				return 'bg-blue-600';

			case 'SD':
				return 'bg-yellow-500';

			case 'COL':
				return 'bg-purple-700';

			default:
				return 'bg-gradient-to-b from-blue-800 via-blue-900 to-gray-900 border-blue-700 dark:from-blue-950 dark:via-indigo-950 dark:to-slate-950';
		}
	}

	public navBarItemsHover(abbreviation: string | undefined) {
		switch (abbreviation) {
			case 'TOR':
				return 'hover:bg-sky-500';
			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				return 'hover:bg-red-500';
			case 'TB':
			case 'KC':
				return 'hover:bg-sky-300';
			case 'BAL':
			case 'HOU':
			case 'DET':
				return 'hover:bg-orange-500';

			case 'CLE':
			case 'SEA':
			case 'TEX':
			case 'NYM':
			case 'ATL':
			case 'MIL':
			case 'MIN':
			case 'NYY':
			case 'BOS':
				return 'hover:bg-blue-900';

			case 'MIA':
				return 'hover:bg-sky-400';

			case 'CHC':
				return 'hover:bg-blue-700';

			case 'ATH':
				return 'hover:bg-green-500';

			case 'PIT':
			case 'CWS':
			case 'SF':
				return 'hover:bg-black';

			case 'LAD':
				return 'hover:bg-blue-600';

			case 'SD':
				return 'hover:bg-yellow-500';

			case 'COL':
				return 'hover:bg-purple-700';

			default:
				return 'hover:bg-blue-700';
		}
	}

	public navBarActiveItem(abbreviation: string | undefined) {
		switch (abbreviation) {
			case 'TOR':
				return 'active-nav bg-sky-500';
			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				return 'active-nav bg-red-500';
			case 'TB':
			case 'KC':
				return 'active-nav bg-sky-300';
			case 'BAL':
			case 'HOU':
			case 'DET':
				return 'active-nav bg-orange-500';

			case 'CLE':
			case 'SEA':
			case 'TEX':
			case 'NYM':
			case 'ATL':
			case 'MIL':
			case 'MIN':
			case 'NYY':
			case 'BOS':
				return 'active-nav bg-blue-900';

			case 'MIA':
				return 'active-nav bg-sky-400';

			case 'CHC':
				return 'active-nav bg-blue-700';

			case 'ATH':
				return 'active-nav bg-green-500';

			case 'PIT':
			case 'CWS':
			case 'SF':
				return 'active-nav bg-black';

			case 'LAD':
				return 'active-nav bg-blue-600';

			case 'SD':
				return 'active-nav bg-yellow-500';

			case 'COL':
				return 'active-nav bg-purple-700';

			default:
				return 'active-nav bg-blue-700';
		}
	}
}