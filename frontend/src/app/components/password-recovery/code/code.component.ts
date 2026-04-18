import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-code-phase',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule],
	templateUrl: './code.component.html'
})
export class CodePhaseComponent implements OnInit {
	@Output() codeConfirmed = new EventEmitter<string>();

	private fb = inject(FormBuilder);

	public codeForm!: FormGroup;
	public errorMessage = '';

	ngOnInit(): void {
		this.codeForm = this.fb.group({
			firstDigit: ['', [Validators.required, Validators.pattern(/^\d$/)]],
			secondDigit: ['', [Validators.required, Validators.pattern(/^\d$/)]],
			thirdDigit: ['', [Validators.required, Validators.pattern(/^\d$/)]],
			fourthDigit: ['', [Validators.required, Validators.pattern(/^\d$/)]],
		});
	}

	public getCode(): string {
		const { firstDigit, secondDigit, thirdDigit, fourthDigit } = this.codeForm.value;
		return `${firstDigit}${secondDigit}${thirdDigit}${fourthDigit}`;
	}

	public submitCode() {
		if (this.codeForm.invalid) {
			this.errorMessage = 'You must enter all 4 digits and in a valid format';
			return;
		}
		this.errorMessage = '';
		this.codeConfirmed.emit(this.getCode());
	}

	public onDigitInput(event: any, nextInput: HTMLInputElement | null) {
		if (event.target.value.length === 1) {
			this.codeForm.markAllAsTouched();
			if (nextInput) nextInput.focus();
		}
	}
}