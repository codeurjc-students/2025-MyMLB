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
			case 'ARI':
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
}