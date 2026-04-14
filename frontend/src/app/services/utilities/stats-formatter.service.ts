import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class StatsFormatterService {
	public formatter(value: number, stat: string) {
		if (!value && value !== 0) {
			return '-';
		}
		const decimalStats = ['AVG', 'OBP', 'SLG', 'OPS', 'ERA', 'WHIP'];
		if (decimalStats.includes(stat)) {
			const formatted = value.toFixed(3);
			if (['AVG', 'OBP', 'SLG', 'OPS'].includes(stat)) {
				return formatted.startsWith('0') ? formatted.substring(1) : formatted;
			}
			return value.toFixed(2);
		}
		return value.toString();
	}
}