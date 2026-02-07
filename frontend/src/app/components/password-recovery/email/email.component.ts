import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ForgotPasswordRequest } from '../../../models/auth/forgot-password.model';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-email-phase',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule],
	templateUrl: './email.component.html',
})
export class EmailPhaseComponent {
	@Output() emailSent = new EventEmitter<void>();
	public emailForm: FormGroup;
	public errorMessage = '';

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

		const request: ForgotPasswordRequest = { email: this.emailForm.value.email };
		this.authService.forgotPassword(request).subscribe({
			next: () => {
				this.emailSent.emit();
			},
			error: (_) => {
				this.errorMessage = 'Resource Not Found';
			}
		});
	}
}