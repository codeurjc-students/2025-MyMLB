import { Observable } from 'rxjs';
import { EntityFormMapperService } from '../../services/utilities/entity-form-mapper.service';

export abstract class EditEntityComponent<T, R extends object> {
	public request!: R;
	public formInputs: any = {};

	public success = false;
	public error = false;
	public finish = false;
	public loading = false;

	public successMessage = '';
	public errorMessage = '';

	protected constructor(protected mapper: EntityFormMapperService) {}

	protected abstract getFieldMap(): Record<string, string>;
	protected abstract getEntity(): T;
	protected abstract updateEntityService(request: R): Observable<any>;

	public hydrateForm() {
		this.mapper.syncFormInputs(this.formInputs, this.getEntity(), this.getFieldMap());
	}

	public prepareRequest() {
		this.request = this.mapper.buildEditRequest<R>(
			this.formInputs,
			this.getEntity(),
			this.getFieldMap()
		);
	}

	public updateDashboard() {
		this.mapper.updateEntityFromInputs(this.formInputs, this.getEntity(), this.getFieldMap());
	}

	public confirm() {
		this.resetState();
		this.prepareRequest();
		this.updateEntityService(this.request).subscribe({
			next: () => {
				this.finish = true;
				this.updateDashboard();
			},
			error: () => {
				this.error = true;
				this.errorMessage = 'An unexpected error occurr. Please, try again later';
			},
		});
	}

	public resetState() {
		this.error = false;
		this.errorMessage = '';
		this.success = false;
		this.successMessage = '';
		this.loading = false;
	}
}