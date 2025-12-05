import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EditTeamComponent } from '../../../../app/components/admin/edit-team/edit-team.component';
import { TeamService } from '../../../../app/services/team.service';
import { StadiumService } from '../../../../app/services/stadium.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { TeamInfo } from '../../../../app/models/team.model';
import { Stadium } from '../../../../app/models/stadium.model';
import { AuthResponse } from '../../../../app/models/auth/auth-response.model';
import { MockFactory } from '../../../utils/mock-factory';
import { PaginatedSelectorService } from '../../../../app/services/utilities/paginated-selector.service';
import { EntityFormMapperService } from '../../../../app/services/utilities/entity-form-mapper.service';

describe('Edit Team Component Tests', () => {
	let component: EditTeamComponent;
	let fixture: ComponentFixture<EditTeamComponent>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let stadiumServiceSpy: jasmine.SpyObj<StadiumService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;

	const teamStats = MockFactory.buildTeamMocks(
		'Yankees',
		'NYY',
		'American',
		'East',
		162,
		100,
		62,
		0.617,
		0,
		'7-3'
	);

	const stadium = MockFactory.buildStadiumMock('Yankee Stadium', 2009, [
		{ url: '', publicId: '' },
	]);

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		teamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		stadium,
		[],
		[]
	);

	beforeEach(() => {
		teamServiceSpy = jasmine.createSpyObj('TeamService', ['updateTeam']);
		stadiumServiceSpy = jasmine.createSpyObj('StadiumService', ['getAvailableStadiums']);
		backgroundServiceSpy = jasmine.createSpyObj('BackgroundColorService', [
			'getBackgroundColor',
		]);

		TestBed.configureTestingModule({
			imports: [EditTeamComponent],
			providers: [
				EntityFormMapperService,
				PaginatedSelectorService,
				{ provide: TeamService, useValue: teamServiceSpy },
				{ provide: StadiumService, useValue: stadiumServiceSpy },
				{ provide: BackgroundColorService, useValue: backgroundServiceSpy },
			],
		}).compileComponents();

		fixture = TestBed.createComponent(EditTeamComponent);
		component = fixture.componentInstance;
		component.team = { ...mockTeamInfo };
		fixture.detectChanges();
	});

	it('should initialize formInputs with team data', () => {
		expect(component.formInputs.city).toBe('New York');
		expect(component.formInputs.stadiumName).toBe('Yankee Stadium');
	});

	it('should select a stadium and show success message', () => {
		const stadium: Stadium = { name: 'Yankee Stadium' } as Stadium;
		spyOn(component.selector, 'select').and.returnValue(stadium.name);

		component.selectStadium(stadium);

		expect(component.formInputs.stadiumName).toBe('Yankee Stadium');
		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Stadium Selected');
	});

	it('should build request correctly with modified inputs', () => {
		component.formInputs.city = 'Boston';

		(component as any).prepareRequest();

		expect(component.request.city).toBe('Boston');
	});

	it('should confirm and update dashboard on success', () => {
		const response: AuthResponse = {
			status: 'SUCCESS',
			message: 'Team successfully updated',
		};

		teamServiceSpy.updateTeam.and.returnValue(of(response));

		component.formInputs.city = 'Boston';
		component.confirm();

		expect(component.finish).toBeTrue();
		expect(component.team.city).toBe('Boston');
	});

	it('should handle error on confirmation', () => {
		teamServiceSpy.updateTeam.and.returnValue(throwError(() => new Error('error')));

		component.confirm();

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toContain('An unexpected error occurr. Please, try again later');
	});

	it('should emit backToMenu when returning to menu', () => {
		spyOn(component.backToMenu, 'emit');

		component.goToEditMenu();

		expect(component.finish).toBeFalse();
		expect(component.backToMenu.emit).toHaveBeenCalled();
	});

	it('should close stadium modal on Escape key', fakeAsync(() => {
		component.selectStadiumButtonClicked = true;
		const event = new KeyboardEvent('keydown', { key: 'Escape' });

		component.handleEscape(event);
		tick(300);

		expect(component.selectStadiumButtonClicked).toBeFalse();
		expect(component.isClose).toBeFalse();
	}));
});