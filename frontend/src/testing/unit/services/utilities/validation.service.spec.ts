import { TestBed } from '@angular/core/testing';
import { ValidationService } from '../../../../app/services/utilities/validation.service';
import { MockFactory } from '../../../utils/mock-factory';
import { TeamInfo } from '../../../../app/models/team.model';

describe('Validation Service Tests', () => {
	let service: ValidationService;

	const mockTeamSummary = MockFactory.buildTeamSummaryMock('New York Mets', 'NYM', 'NL', 'EAST');

	const mockStadium = MockFactory.buildStadiumCompleteMock(
		'City Field',
		1999,
		'New York Mets',
		[]
	);

	const mockPositionPlayer = MockFactory.buildPositionPlayerMock(
		'Aaron Judge',
		'RF',
		500,
		80,
		150,
		30,
		2,
		45,
		110,
		0.3,
		0.4,
		1.0,
		0.6,
		{ url: '', publicId: '' }
	);

	const mockPitcher = MockFactory.buildPitcherMock(
		'Gerrit Cole',
		'SP',
		30,
		15,
		5,
		2.5,
		200,
		220,
		40,
		150,
		60,
		1.05,
		0,
		0,
		{ url: '', publicId: '' }
	);

	const mockTeamStats = MockFactory.buildTeamMocks(
		'Yankees',
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

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		mockTeamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		mockStadium,
		[mockPositionPlayer],
		[mockPitcher]
	);

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [ValidationService],
		});
		service = TestBed.inject(ValidationService);
	});

	describe('isTeamSummary', () => {
		it('should return true when object has abbreviation', () => {
			expect(service.isTeamSummary(mockTeamSummary)).toBeTrue();
		});

		it('should return false when object does not have abbreviation', () => {
			expect(service.isTeamSummary(mockStadium)).toBeFalse();
		});
	});

	describe('isTeamInfo', () => {
		it('should return true when object has teamStats', () => {
			expect(service.isTeamInfo(mockTeamInfo)).toBeTrue();
		});

		it('should return false when object does not have teamStats', () => {
			expect(service.isTeamInfo(mockStadium)).toBeFalse();
		});
	});

	describe('isStadium', () => {
		it('should return true when object has openingDate', () => {
			expect(service.isStadium(mockStadium)).toBeTrue();
		});

		it('should return false when object does not have openingDate', () => {
			expect(service.isStadium(mockTeamInfo)).toBeFalse();
		});
	});

	describe('isPitcher', () => {
		it('should return true when object has era', () => {
			expect(service.isPitcher(mockPitcher)).toBeTrue();
		});

		it('should return false when object does not have era', () => {
			expect(service.isPitcher(mockStadium)).toBeFalse();
		});
	});

	describe('isPositionPlayer', () => {
		it('should return true when object has ops', () => {
			expect(service.isPositionPlayer(mockPositionPlayer)).toBeTrue();
		});

		it('should return false when object does not have ops', () => {
			expect(service.isPositionPlayer(mockStadium)).toBeFalse();
		});
	});
});