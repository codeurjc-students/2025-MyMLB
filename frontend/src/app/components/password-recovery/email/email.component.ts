import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ForgotPasswordRequest } from '../../../models/auth.model';
import { CommonModule } from '@angular/common';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';

@Component({
	selector: 'app-email-phase',
	standalone: true,
	imports: [ReactiveFormsModule, CommonModule, MatInputModule, MatFormFieldModule],
	templateUrl: './email.component.html',
})
export class EmailPhaseComponent implements OnInit {
	@Output() emailSent = new EventEmitter<void>();

	private fb = inject(FormBuilder);
	private authService = inject(AuthService);

	public emailForm!: FormGroup;
	public errorMessage = '';

	ngOnInit(): void {
		this.emailForm = this.fb.group({
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