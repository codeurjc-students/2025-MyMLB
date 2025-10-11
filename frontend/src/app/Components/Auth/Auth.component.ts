import { Component } from '@angular/core';
import { LoginComponent } from "../login/login.component";
import { RegisterComponent } from '../register/register.component';

@Component({
	selector: 'app-Auth',
	standalone: true,
	imports: [LoginComponent, RegisterComponent],
	templateUrl: './auth.component.html',
})
export class AuthComponent {
	public loginForm = true;

	constructor() {}

	public toggleForm() {
		this.loginForm = !this.loginForm;
	}
}