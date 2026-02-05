import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { UserService } from '../../services/user.service';
import { SupportService } from '../../services/support.service';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CreateTicketRequest } from '../../models/support/create-ticket-request.model';
import { EscapeCloseDirective } from "../../directives/escape-close.directive";
import { SuccessModalComponent } from "../success-modal/success-modal.component";
import { ErrorModalComponent } from "../modal/error-modal/error-modal.component";

@Component({
	selector: 'app-support',
	imports: [ReactiveFormsModule, CommonModule, EscapeCloseDirective, SuccessModalComponent, ErrorModalComponent],
	standalone: true,
	templateUrl: './support.component.html'
})
export class Support implements OnInit {
	@Input() showModal!: boolean;
	@Output() closeModal = new EventEmitter<void>();

	public isClosing = false;

	private userService = inject(UserService);
	private supportService = inject(SupportService);
	private fb = inject(FormBuilder);

	public supportForm: FormGroup;

	public userEmail: string | null = null;

	public success = false;
	public error = false;

	public successMessage = '';
	public errorMessage = '';

	constructor() {
		this.handleCancel = this.handleCancel.bind(this);

		this.supportForm = this.fb.group({
			email: ['', [Validators.required, Validators.email]],
			subject: ['', [Validators.required]],
			body: ['', [Validators.required]]
		});
	}

	ngOnInit(): void {
		this.userService.getUserProfile().subscribe({
			next: (response) => {
				this.userEmail = response.email;
				this.supportForm.get('email')?.setValue(this.userEmail);
			}
		});
	}

	public handleCancel() {
		this.isClosing = true;
		setTimeout(() => {
			this.closeModal.emit();
			this.isClosing = false;
		}, 300);
	}

	public get emailControl(): FormControl {
		return this.supportForm.get('email') as FormControl;
	}

	public getEmailFromForm() {
		return this.supportForm.get('email');
	}

	public getSubjectFromForm() {
		return this.supportForm.get('subject');
	}

	public getBodyFromForm() {
		return this.supportForm.get('body');
	}

	private buildRequest() {
		const data = this.supportForm.value;
		const request: CreateTicketRequest = {
			email: data.email,
			subject: data.subject,
			body: data.body
		};
		return request;
	}

	public createTicket() {
		if (this.supportForm.invalid) {
			return;
		}
		const request = this.buildRequest();

		this.supportService.createTicket(request).subscribe({
			next: (_) => {
				this.success = true;
				this.successMessage = 'Message sent successfully';
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Something wrong happened. Please, try again'
			}
		});
	}
}