import { Component, EventEmitter, Output } from '@angular/core';
import { AuthService } from '../../Services/Auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginRequest } from '../../Models/Auth/LoginRequest';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule],
	templateUrl: './Login.component.html',
})
export class LoginComponent {

	@Output() toggleForm = new EventEmitter<void>();
	public loginForm: FormGroup;
	public errorMessage: string = "";

	constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) {
		this.loginForm = fb.group({
			username: ['', [Validators.required]],
			password: ['', [Validators.required]]
		});
	}

	public getUsername() {
		return this.loginForm.get('username');
	}

	public getPassword() {
		return this.loginForm.get('password');
	}

	private getLoginRequest() {
		const data = this.loginForm.value;
		const loginRequest: LoginRequest = {
			username: data.username,
			password: data.password
		};
		return loginRequest;
	}

	public login() {
		if (this.loginForm.invalid) {
			return;
		}
		const loginRequest = this.getLoginRequest();

		this.authService.loginUser(loginRequest).subscribe({
			next: (_) => this.router.navigate(['/']),
			error: (err) => this.errorMessage = err.message
		});
	}

	public redirectToForgotPassword() {
		this.router.navigate(['recovery']);
	}

	public toggleToRegisterForm() {
		this.toggleForm.emit();
	}
}