import { CommonModule } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
	Input,
	Output,
} from '@angular/core';
import { Stadium } from '../../../models/stadium.model';
import { TeamSummary } from '../../../models/team.model';
import { BackgroundColorService } from '../../../services/background-color.service';

@Component({
	selector: 'app-select-element-modal',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonModule],
	templateUrl: './select-element-modal.component.html',
})
export class SelectElementModalComponent {
	@Input() teamElements!: boolean;
	@Input() openModal!: boolean;
	@Input() isClose!: boolean;
	@Input() hasMore!: boolean;
	@Input() elements!: (Stadium | TeamSummary)[];

	@Output() select = new EventEmitter<unknown>();
	@Output() loadNextPageFunction = new EventEmitter<void>();
	@Output() closeModal = new EventEmitter<void>();

	constructor(private backgroundService: BackgroundColorService) {}

	public isTeam(obj: any): obj is TeamSummary {
		return 'abbreviation' in obj;
	}

	public selectElement(elem: Stadium | TeamSummary) {
		this.select.emit(elem);
	}

	public loadNextPage() {
		this.loadNextPageFunction.emit();
	}

	public close() {
		this.closeModal.emit();
	}

	public trackByFn(elem: Stadium | TeamSummary) {
		return this.isTeam(elem) ? elem.abbreviation: elem.name;
	}

	public logoBackground(elem: unknown) {
		if (this.isTeam(elem)) {
			return this.backgroundService.getBackgroundColor(elem.abbreviation);
		}
		return '';
	}
}