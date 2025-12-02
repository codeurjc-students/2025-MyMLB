import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EntityFormMapperService {
	buildEditRequest<T extends object>(
		inputObj: any,
		entityObj: any,
		fieldMap: Record<string, string>
	): T {
		const req: any = {};
		Object.entries(fieldMap).forEach(([inputKey, entityPath]) => {
			const newValue = inputObj[inputKey];
			const oldValue = this.getNested(entityObj, entityPath);
			if (newValue !== undefined && newValue !== oldValue) {
				this.setNested(req, entityPath, newValue);
			}
		});
		return req as T;
	}

	syncFormInputs(formInputs: any, entity: any, fieldMap: Record<string, string>) {
		Object.entries(fieldMap).forEach(([inputKey, entityPath]) => {
			formInputs[inputKey] = this.getNested(entity, entityPath);
		});
	}

	updateEntityFromInputs(formInputs: any, entity: any, fieldMap: Record<string, string>) {
		Object.entries(fieldMap).forEach(([inputKey, entityPath]) => {
			const newValue = formInputs[inputKey];
			const oldValue = this.getNested(entity, entityPath);
			if (newValue !== undefined && newValue !== oldValue) {
				this.setNested(entity, entityPath, newValue);
			}
		});
	}

	private getNested(obj: any, path: string): any {
		return path.split('.').reduce((acc, key) => (acc != null ? acc[key] : undefined), obj);
	}

	private setNested(obj: any, path: string, value: any): void {
		const keys = path.split('.');
		const last = keys.pop()!;
		const target = keys.reduce((acc, key) => {
			if (acc[key] === undefined || acc[key] === null) acc[key] = {};
			return acc[key];
		}, obj);
		target[last] = value;
	}
}