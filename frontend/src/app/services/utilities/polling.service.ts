import { inject, Injectable } from '@angular/core';
import { interval, startWith, Subscription } from 'rxjs';
import { SupportService } from '../support.service';

@Injectable({
  providedIn: 'root'
})
export class PollingService {
	private polling?: Subscription;
	private supportService = inject(SupportService);

	public initPolling() {
		if (this.polling) {
			return;
		}
		this.polling = interval(3000).pipe(startWith(0)).subscribe(() => {
			this.supportService.updateCurrentOpenTickets();
		});
	}

	public stopPolling() {
		this.polling?.unsubscribe();
		this.polling = undefined;
	}
}