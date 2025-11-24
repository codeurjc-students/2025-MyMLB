import { TestBed } from '@angular/core/testing';
import { ErrorService } from '../../../app/services/error.service';

describe('ErrorService', () => {
	let service: ErrorService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [ErrorService],
		});

		service = TestBed.inject(ErrorService);
	});

	it('should store an error when setError is called', () => {
		service.setError(404, 'Not Found');
		const result = service.getError();

		expect(result).toEqual({ code: 404, message: 'Not Found' });
	});

	it('should clear the stored error after getError is called', () => {
		service.setError(500, 'Server Error');
		service.getError();
		const result = service.getError();

		expect(result).toBeNull();
	});

	it('should return null if getError is called without previous setError', () => {
		const result = service.getError();
		expect(result).toBeNull();
	});
});