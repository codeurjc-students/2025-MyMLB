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
import { ValidationService } from '../../../services/utilities/validation.service';

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

	constructor(public backgroundService: BackgroundColorService, public validationService: ValidationService) {}

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
		return this.validationService.isTeamSummary(elem) ? elem.abbreviation: elem.name;
	}
}