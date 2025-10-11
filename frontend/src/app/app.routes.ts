import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { AuthComponent } from './components/auth/auth.component';
import { LoginComponent } from './components/login/login.component';
import { PasswordComponent } from './components/password-recovery/password.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: "auth", component: AuthComponent },
	{ path: "login", component: LoginComponent },
	{ path: "recovery", component: PasswordComponent }
];