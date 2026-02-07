import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { RegisterRequest } from '../../models/auth/register-request.model';
import { CommonModule } from '@angular/common';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
	selector: 'app-register',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule, MatTooltip],
	templateUrl: './register.component.html'
})
export class RegisterComponent {
	@Output() toggleForm = new EventEmitter<void>();
	public registerForm: FormGroup;
	public errorMessage = "";
	public successMessage = "";
	public showSuccess = false;
	public hidePassword = true;

	constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) {
		this.registerForm = fb.group({
			email: ['', [Validators.required, Validators.email]],
			username: ['', [Validators.required]],
			password: ['', [Validators.required]]
		});
	}

	public getEmail() {
		return this.registerForm.get('email');
	}

	public getUsername() {
		return this.registerForm.get('username');
	}

	public getPassword() {
		return this.registerForm.get('password');
	}

	private getRegisterRequest() {
		const data = this.registerForm.value;
		const registerRequest : RegisterRequest = {
			email: data.email,
			username: data.username,
			password: data.password
		};
		return registerRequest;
	}

	public register() {
		if (this.registerForm.invalid) {
			return;
		}
		this.showSuccess = false;
		this.successMessage = "";
		this.errorMessage = "";

		const registerRequest = this.getRegisterRequest();

		this.authService.registerUser(registerRequest).subscribe({
			next: (response) => {
				if (response.status ===  "SUCCESS") {
					this.showSuccess = true;
					this.successMessage = response.message;
				}
				else {
					this.errorMessage = response.message;
				}
			},
			error: (_) => {
				this.errorMessage = 'User already exists';
			}
		});
	}

	public toggleToLoginForm() {
		this.toggleForm.emit();
		this.registerForm.reset();
	}
}