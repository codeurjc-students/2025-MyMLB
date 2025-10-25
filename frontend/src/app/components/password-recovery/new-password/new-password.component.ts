import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ResetPasswordRequest } from '../../../models/auth/reset-password-request.model';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
	selector: 'app-password-phase',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule, RouterLink],
	templateUrl: './new-password.component.html',
})
export class PasswordPhaseComponent {
	@Input() code: string = '';
	@Output() passwordReset = new EventEmitter<void>();
	public passwordForm: FormGroup;
	public errorMessage = '';
	public successMessage = '';
	public showSuccess = false;

	constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
		this.passwordForm = fb.group({
			newPassword: ['', [Validators.required]],
		});
	}

	public getNewPassword() {
		return this.passwordForm.get('newPassword');
	}

	public submitPassword() {
		if (this.passwordForm.invalid) {
			return;
		}

		const request: ResetPasswordRequest = {
			code: this.code,
			newPassword: this.passwordForm.value.newPassword,
		};

		this.errorMessage = "";

		this.authService.resetPassword(request).subscribe({
			next: (res) => {
				this.successMessage = res.message;
				this.showSuccess = true;
			},
			error: (err) => this.errorMessage = err.message
		});
	}

	public toggleToLoginForm() {
        this.router.navigate(['login']);
    }
}