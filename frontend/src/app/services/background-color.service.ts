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

	public navBarItems(abbreviation: string | undefined, prefix: string): string {
		let color = '';

		switch (abbreviation) {
			case 'TOR':
				color = 'sky-700';
				break;

			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				color = 'red-700';
				break;

			case 'TB':
			case 'KC':
			case 'MIA':
				color = 'sky-600';
				break;

			case 'HOU':
			case 'DET':
				color = 'blue-800';
				break;

			case 'BAL':
			case 'SD':
				color = 'black';
				break;

			case 'CHC':
				color = 'blue-500';
				break;

			case 'ATH':
				color = 'green-500';
				break;

			case 'SF':
				color = 'orange-600';
				break;

			case 'PIT':
				color = 'yellow-500'
				break;

			case 'CWS':
				color = 'gray-800';
				break;

			case 'LAD':
				color = 'blue-400';
				break;

			case 'COL':
				color = 'purple-500';
				break;

			default:
				color = 'blue-700';
		}

		if (prefix === 'active-nav') {
			return `active-nav bg-${color}`;
		}
		return `${prefix}bg-${color}`;
	}

	public toggleButton(abbreviation: string | undefined) {
		switch (abbreviation) {
			case 'TOR':
				return 'peer-focus:ring-sky-600 peer-checked:bg-sky-700';

			case 'LAA':
			case 'CIN':
			case 'AZ':
			case 'PHI':
			case 'WSH':
				return 'peer-focus:ring-red-600 peer-checked:bg-red-700';

				case 'TB':
			case 'KC':
			case 'MIA':
				return 'peer-focus:ring-sky-500 peer-checked:bg-sky-600';

			case 'HOU':
			case 'DET':
				return 'peer-focus:ring-blue-700 peer-checked:bg-blue-800';

			case 'BAL':
			case 'SD':
				return 'peer-focus:ring-gray-800 peer-checked:bg-black'

			case 'CHC':
				return 'peer-focus:ring-blue-400 peer-checked:bg-blue-500';

			case 'ATH':
				return 'peer-focus:ring-green-400 peer-checked:bg-green-500';

			case 'PIT':
				return 'peer-focus:ring-yellow-400 peer-checked:bg-yellow-500';

			case 'SF':
				return 'peer-focus:ring-orange-500 peer-checked:bg-orange-600';

			case 'CWS':
				return 'peer-focus:ring-gray-700 peer-checked:bg-gray-800';

			case 'LAD':
				return 'peer-focus:ring-blue-300 peer-checked:bg-blue-400';

			case 'COL':
				return 'peer-focus:ring-purple-400 peer-checked:bg-purple-500';

			default:
				return 'peer-focus:ring-blue-600 peer-checked:bg-blue-700';
		}
	}
}