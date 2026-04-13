import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CacheService } from '../../../services/cache.service';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { SuccessModalComponent } from "../../modal/success-modal/success-modal.component";
import { BackToDashboardButtonComponent } from "../back-to-dashboard-button/back-to-dashboard-button.component";
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { MatIconModule } from "@angular/material/icon";

@Component({
	selector: 'app-cache',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		FormsModule,
		ErrorModalComponent,
		SuccessModalComponent,
		BackToDashboardButtonComponent,
		LoadingModalComponent,
		MatIconModule
	],
	templateUrl: './cache.component.html'
})
export class CacheComponent implements OnInit {
	private cacheService = inject(CacheService);
	@Output() backToStatsDashboard = new EventEmitter<void>();

	public caches: string[] = [];

	public loading = false;
	public error = false;
	public success = false;
	public errorMessage = '';
	public successMessage = '';

	ngOnInit() {
		this.loadCaches();
	}

	private loadCaches() {
		this.loading = true;
		this.cacheService.getCaches().subscribe({
			next: (response) => {
				this.caches = response;
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur fetching the caches: ${err.error.message}`;
			}
		});
	}

	public clearCache(name: string) {
		this.loading = true;
		this.cacheService.clearCache(name).subscribe({
			next: (_) => {
				this.loading = false;
				this.success = true;
				this.successMessage = `Cache ${name} successfully restored`;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur while clearing the cache: ${err.error.message}`;
			}
		});
	}

	public clearAllCaches() {
		this.loading = true;
		this.cacheService.clearAllCaches().subscribe({
			next: (_) => {
				this.loading = false;
				this.success = true;
				this.successMessage = 'Caches successfully restored';
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur while clearing the caches: ${err.error.message}`;
			}
		});
	}

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}