import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { PaginatedResponse } from '../../models/pagination.model';

@Injectable({ providedIn: 'root' })
export class PaginatedSelectorService<T> {
	public items$ = new BehaviorSubject<T[]>([]);
	private rawItems: T[] = [];

	public currentPage = 0;
	public hasMore = true;
	public loading = false;

	private excludedLabels = new Set<string>();
	private currentSelectionLabel?: string;
	private labelFn: ((item: T) => string) | null = null;

	public setLabelFn(fn: (item: T) => string): void {
		this.labelFn = fn;
		this.refreshView();
	}

	public exclude(label: string): void {
		this.excludedLabels.add(label);
		this.refreshView();
	}

	public loadPage(
		page: number,
		pageSize: number,
		fetchFn: (page: number, size: number) => any
	): void {
		if (this.loading) return;
		this.loading = true;

		fetchFn(page, pageSize)
			.pipe(finalize(() => (this.loading = false)))
			.subscribe({
				next: (response: PaginatedResponse<T>) => {
					this.rawItems = [...this.rawItems, ...response.content];
					this.currentPage = response.page.number;
					this.hasMore = response.page.totalPages > this.currentPage + 1;
					this.refreshView();
				},
				error: () => {
					this.items$.error('Error loading items');
				},
			});
	}

	public loadNextPage(pageSize: number, fetchFn: (page: number, size: number) => any): void {
		if (this.hasMore) {
			this.loadPage(this.currentPage + 1, pageSize, fetchFn);
		}
	}

	public select(item: T, labelFn: (item: T) => string): string {
		const label = labelFn(item);

		if (this.currentSelectionLabel) {
			this.excludedLabels.delete(this.currentSelectionLabel);
		}
		this.excludedLabels.add(label);
		this.currentSelectionLabel = label;

		this.refreshView();
		return label;
	}

	public clearSelection(): void {
		if (this.currentSelectionLabel) {
			this.excludedLabels.delete(this.currentSelectionLabel);
			this.currentSelectionLabel = undefined;
			this.refreshView();
		}
	}

	public reset(): void {
		this.rawItems = [];
		this.items$.next([]);
		this.currentPage = 0;
		this.hasMore = true;
	}

	public clearAll(): void {
		this.rawItems = [];
		this.items$.next([]);
		this.currentSelectionLabel = undefined;
		this.excludedLabels.clear();
		this.currentPage = 0;
		this.hasMore = true;
	}

	private refreshView(): void {
		if (!this.labelFn) {
			this.items$.next(this.rawItems.slice());
			return;
		}
		const filtered = this.rawItems.filter(
			(item) => !this.excludedLabels.has(this.labelFn!(item))
		);
		this.items$.next(filtered);
	}
}