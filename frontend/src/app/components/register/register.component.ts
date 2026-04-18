import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/auth.model';
import { CommonModule } from '@angular/common';
import { MatTooltip } from '@angular/material/tooltip';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import { AnalyticsService } from '../../services/analytics.service';

@Component({
	selector: 'app-register',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule, MatTooltip, MatInputModule, MatFormFieldModule],
	templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
	@Output() toggleForm = new EventEmitter<void>();

	private authService = inject(AuthService);
	private analyticsService = inject(AnalyticsService);
	private fb = inject(FormBuilder);

	public registerForm!: FormGroup;
	public errorMessage = "";
	public successMessage = "";
	public showSuccess = false;
	public hidePassword = true;

	ngOnInit(): void {
		this.registerForm = this.fb.group({
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
					this.analyticsService.updateNewUsers().subscribe();
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