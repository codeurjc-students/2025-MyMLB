import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PollingService } from '../../../../app/services/utilities/polling.service';
import { SupportService } from '../../../../app/services/support.service';

describe('Polling Service Tests', () => {
	let service: PollingService;
	let supportServiceSpy: jasmine.SpyObj<SupportService>;

	beforeEach(() => {
		const spy = jasmine.createSpyObj('SupportService', ['updateCurrentOpenTickets']);

		TestBed.configureTestingModule({
			providers: [PollingService, { provide: SupportService, useValue: spy }],
		});

		service = TestBed.inject(PollingService);
		supportServiceSpy = TestBed.inject(SupportService) as jasmine.SpyObj<SupportService>;
	});

	afterEach(() => {
		service.stopPolling();
	});

	it('should call updateCurrentOpenTickets every 3 seconds', fakeAsync(() => {
		service.initPolling();

		expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalledTimes(1);

		tick(3000);
		expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalledTimes(2);

		tick(3000);
		expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalledTimes(3);

		service.stopPolling();
	}));

	it('should not call updateCurrentOpenTickets once the polling finished (stopPolling)', fakeAsync(() => {
		service.initPolling();
		expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalledTimes(1);

		service.stopPolling();

		tick(3000);
		expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalledTimes(1);
	}));
});