import { TestBed } from '@angular/core/testing';
import { CodePhaseComponent } from '../../../app/components/password-recovery/code/code.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('Code Phase Component Tests', () => {
	let component: CodePhaseComponent;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [CodePhaseComponent, ReactiveFormsModule, CommonModule],
		});

		const fixture = TestBed.createComponent(CodePhaseComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create form with 4 digit inputs', () => {
		const controls = ['firstDigit', 'secondDigit', 'thirdDigit', 'fourthDigit'];
		controls.forEach((control) => {
			expect(component.codeForm.contains(control)).toBeTrue();
		});
	});

	it('should not emit code if form is invalid', () => {
		const emitSpy = spyOn(component.codeConfirmed, 'emit');
		component.codeForm.setValue({
			firstDigit: '',
			secondDigit: '',
			thirdDigit: '',
			fourthDigit: '',
		});

		component.submitCode();

		expect(emitSpy).not.toHaveBeenCalled();
		expect(component.errorMessage).toBe('You must enter all 4 digits and in a valid format');
	});

	it('should emit code if form is valid', () => {
		const emitSpy = spyOn(component.codeConfirmed, 'emit');
		component.codeForm.setValue({
			firstDigit: '1',
			secondDigit: '2',
			thirdDigit: '3',
			fourthDigit: '4',
		});

		component.submitCode();

		expect(emitSpy).toHaveBeenCalledWith('1234');
		expect(component.errorMessage).toBe('');
	});

	it('should focus next input on digit entry', () => {
		const mockEvent = { target: { value: '5' } };
		const nextInput = document.createElement('input');
		const focusSpy = spyOn(nextInput, 'focus');

		component.onDigitInput(mockEvent, nextInput);

		expect(focusSpy).toHaveBeenCalled();
	});
});