import { Component } from '@angular/core';
import { LoginComponent } from "../Login/Login.component";
import { RegisterComponent } from '../Register/Register.component';

@Component({
	selector: 'app-Auth',
	standalone: true,
	imports: [LoginComponent, RegisterComponent],
	templateUrl: './Auth.component.html',
})
export class AuthComponent {
	public loginForm = true;

	constructor() {}

	public toggleForm() {
		this.loginForm = !this.loginForm;
	}
}