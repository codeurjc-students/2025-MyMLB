import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LoginComponent } from "../login/login.component";
import { RegisterComponent } from '../register/register.component';

@Component({
	selector: 'app-Auth',
	standalone: true,
	imports: [LoginComponent, RegisterComponent],
	templateUrl: './auth.component.html',
})
export class AuthComponent implements OnInit {
	private route = inject(ActivatedRoute);
	public loginForm = true;

	ngOnInit(): void {
		this.route.queryParams.subscribe(params => {
			this.loginForm = params['form'] !== 'register';
		});
	}

	public toggleForm() {
		this.loginForm = !this.loginForm;
	}
}