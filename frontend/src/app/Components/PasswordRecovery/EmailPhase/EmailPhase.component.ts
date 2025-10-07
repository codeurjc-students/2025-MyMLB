import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/Auth.service';
import { ForgotPasswordRequest } from '../../../Models/Auth/ForgotPasswordRequest';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-email-phase',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule],
	templateUrl: './EmailPhase.component.html',
})
export class EmailPhaseComponent {
	@Output() emailSent = new EventEmitter<void>();
	public emailForm: FormGroup;
	public errorMessage = '';
	public loading = false;

	constructor(private fb: FormBuilder, private authService: AuthService) {
		this.emailForm = fb.group({
			email: ['', [Validators.required, Validators.email]],
		});
	}

	public getEmail() {
		return this.emailForm.get('email');
	}

	public submitEmail() {
		if (this.emailForm.invalid) {
			return;
		}

		this.errorMessage = '';
		this.loading = true;

		const request: ForgotPasswordRequest = { email: this.emailForm.value.email };
		this.authService.forgotPassword(request).subscribe({
			next: () => {
				this.loading = false;
				this.emailSent.emit();
			},
			error: (err) => {
				this.loading = false;
				this.errorMessage = err.message;
			}
		});
	}
}