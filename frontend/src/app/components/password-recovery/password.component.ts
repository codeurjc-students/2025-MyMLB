import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { EmailPhaseComponent } from './email/email.component';
import { CodePhaseComponent } from './code/code.component';
import { PasswordPhaseComponent } from './new-password/new-password.component';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-password',
	standalone: true,
	imports: [CommonModule, EmailPhaseComponent, CodePhaseComponent, PasswordPhaseComponent],
	templateUrl: './password.component.html'
})
export class PasswordComponent {
	public currentStep: 'email' | 'code' | 'password' = 'email';
	public recoveryCode: string = '';

	constructor(private router: Router) {}

	public handleEmailSent() {
		this.currentStep = 'code';
	}

	public handleCodeConfirmed(code: string) {
		this.recoveryCode = code;
		this.currentStep = 'password';
	}
}