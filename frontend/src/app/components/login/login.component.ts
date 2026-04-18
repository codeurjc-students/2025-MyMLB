import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginRequest } from '../../models/auth.model';
import { CommonModule } from '@angular/common';
import { MatTooltip } from '@angular/material/tooltip';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import { switchMap } from 'rxjs';
import { TeamService } from '../../services/team.service';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule, MatTooltip, MatInputModule, MatFormFieldModule],
	templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
	@Output() toggleForm = new EventEmitter<void>();
	private authService = inject(AuthService);
	private teamService = inject(TeamService);
	private fb = inject(FormBuilder);
	private router = inject(Router);

	public loginForm!: FormGroup;
	public errorMessage: string = "";
	public hidePassword = true;

	ngOnInit(): void {
		this.loginForm = this.fb.group({
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

		this.authService.loginUser(loginRequest).pipe(
			switchMap(() => this.authService.getActiveUser())
		).subscribe({
			next: (user) => {
				this.authService.setCurrentUser(user);
				this.teamService.cleanStandingsCache();
				this.router.navigate(['/']);
			},
			error: () => this.errorMessage = 'Invalid Credentials'
		});
	}

	public redirectToForgotPassword() {
		this.router.navigate(['recovery']);
	}

	public toggleToRegisterForm() {
		this.toggleForm.emit();
		this.loginForm.reset();
	}
}