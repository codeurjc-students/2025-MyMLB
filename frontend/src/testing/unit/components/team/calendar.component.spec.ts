import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarComponent } from '../../../../app/components/team/calendar/calendar.component';
import { MatchService, ShowMatch } from './../../../../app/services/match.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { of, throwError } from 'rxjs';
import { MockFactory } from '../../../utils/mock-factory';
import { TeamInfo, TeamSummary } from '../../../../app/models/team.model';
import { addMonths, subMonths } from 'date-fns';

describe('Calendar Component Tests', () => {
	let component: CalendarComponent;
	let fixture: ComponentFixture<CalendarComponent>;
	let matchServiceSpy: jasmine.SpyObj<MatchService>;
	let bgColorServiceSpy: jasmine.SpyObj<BackgroundColorService>;

	const homeTeam: TeamSummary = MockFactory.buildTeamSummaryMock('New York Yankees', 'NYY', 'AL', 'EAST');

	const awayTeam: TeamSummary = MockFactory.buildTeamSummaryMock('Team2', 'T2', 'AL', 'EAST');

	const mockMatch: ShowMatch = MockFactory.buildShowMatchMock(
		1,
		awayTeam,
		homeTeam,
		2,
		3,
		'2025-11-22',
		'SCHEDULED'
	);

	const mockTeamStats = MockFactory.buildTeamMocks(
		'New York Yankees',
		'NYY',
		'AL',
		'EAST',
		162,
		100,
		62,
		0.617,
		0,
		'7-3'
	);

	const mockStadium = MockFactory.buildStadiumMock('Yankee Stadium', 2009,  [{ url: '', publicId: '' }]);

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		mockTeamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		mockStadium,
		[],
		[]
	);

	beforeEach(() => {
		matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchesOfTeamByMonth']);
		bgColorServiceSpy = jasmine.createSpyObj('BackgroundColorService', ['getBackgroundColor']);

		TestBed.configureTestingModule({
			providers: [
				{ provide: MatchService, useValue: matchServiceSpy },
				{ provide: BackgroundColorService, useValue: bgColorServiceSpy },
			],
		});

		fixture = TestBed.createComponent(CalendarComponent);
		component = fixture.componentInstance;
		component.team = mockTeamInfo;
		matchServiceSpy = TestBed.inject(MatchService) as jasmine.SpyObj<MatchService>;
		matchServiceSpy.getMatchesOfTeamByMonth.and.returnValue(of([]));
		bgColorServiceSpy = TestBed.inject(BackgroundColorService) as jasmine.SpyObj<BackgroundColorService>;
	});

	it('ngOnInit should index matches from service', () => {
		matchServiceSpy.getMatchesOfTeamByMonth.and.returnValue(of([mockMatch]));

		component.ngOnInit();

		const matches = component.getMatchesOfDay(new Date(mockMatch.date));
		expect(matches.length).toBe(1);
		expect(matches[0].homeTeam.name).toBe('New York Yankees');
	});

	it('ngOnInit should set errorMessage on service error', () => {
		matchServiceSpy.getMatchesOfTeamByMonth.and.returnValue(throwError(() => new Error('fail')));

		component.ngOnInit();

		expect(component.errorMessage).toContain('Error trying to load matches');
	});

	it('closeCalendar should emit close event', () => {
		spyOn(component.close, 'emit');
		component.closeCalendar();
		expect(component.close.emit).toHaveBeenCalled();
	});


	it('getCellClass should return team color if home match exists', () => {
		matchServiceSpy.getMatchesOfTeamByMonth.and.returnValue(of([mockMatch]));
		bgColorServiceSpy.getBackgroundColor.and.returnValue('bg-blue-900');

		component.ngOnInit();
		const cssClass = component.getCellClass(new Date(mockMatch.date));

		expect(cssClass).toBe('bg-blue-900');
	});

	it('getCellClass should return default if no match', () => {
		const cssClass = component.getCellClass(new Date());
		expect(cssClass).toBe('bg-white dark:bg-gray-400');
	});

	/**
	 * Helper function to validate that an action applied to the component
	 * correctly updates the `viewDate` to the expected month and year.
	 *
	 * This is used to avoid duplicating test code for `previousMonth()` and
	 * `nextMonth()`, or any other method that modifies the calendar's active date.
	 *
	 * @param action - A callback that performs the date-changing operation on the component.
	 * @param initial - The initial Date value to assign to `component.viewDate`
	 *                  before executing the action.
	 * @param expected - The expected Date value after the action has been executed.
	 *                   Only the month and full year are compared.
	 */
	function expectMonthChange(action: () => void, initial: Date, expected: Date) {
		component.viewDate = initial;
		action();
		expect(component.viewDate.getMonth()).toBe(expected.getMonth());
		expect(component.viewDate.getFullYear()).toBe(expected.getFullYear());
	}

	it('previousMonth should decrement viewDate', () => {
		expectMonthChange(
			() => component.previousMonth(),
			new Date(2024, 11, 1),
			subMonths(new Date(2024, 11, 1), 1)
		);
	});

	it('nextMonth should increment viewDate', () => {
		expectMonthChange(
			() => component.nextMonth(),
			new Date(2024, 11, 1),
			addMonths(new Date(2024, 11, 1), 1)
		);
	});

	it('nextMonth should wrap from December to January', () => {
		component.viewDate = new Date(2024, 11, 1);
		component.nextMonth();
		expect(component.viewDate.getMonth()).toBe(0);
	});

	it('isOutsideCurrentMonth should detect days outside', () => {
		const day = new Date(
			component.viewDate.getFullYear(),
			component.viewDate.getMonth() - 1,
			1
		);
		expect(component.isOutsideCurrentMonth(day)).toBeTrue();
	});

	it('openMatchInfoModal should set selectedMatch and isHomeMatch', () => {
		component.openMatchInfoModal(mockMatch);
		expect(component.selectedMatch).toBe(mockMatch);
		expect(component.showMatchInfo).toBeTrue();
		expect(component.isHomeMatch).toBeTrue();
	});

	it('getLogoPath should return correct path', () => {
		const path = component.getLogoPath(mockMatch);
		expect(path).toContain('assets/team-logos-new/AL/EAST/T2.png');
	});

	it('getAbbreviation should return away team abbreviation', () => {
		const abbr = component.getAbbreviation(mockMatch);
		expect(abbr).toBe('T2');
	});

	it('getLogoBackground should return bg-white for dark logos', () => {
		const darkMatch: ShowMatch = {
			...mockMatch,
			awayTeam: {
				name: 'Detroit Tigers',
				abbreviation: 'DET',
				league: 'AL',
				division: 'Central',
			},
		};
		const bg = component.getLogoBackground(darkMatch);
		expect(bg).toBe('bg-white');
	});
});