import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { AuthComponent } from './components/auth/auth.component';
import { LoginComponent } from './components/login/login.component';
import { PasswordComponent } from './components/password-recovery/password.component';
import { AuthGuard } from './guards/auth.guard';
import { ErrorComponent } from './components/error/error.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'auth', component: AuthComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'recovery', component: PasswordComponent },
	//{ path: 'error', component: ErrorComponent },
	{
		path: 'profile',
		canActivate: [AuthGuard],
		loadComponent: () =>
			import('./components/profile/profile.component').then((m) => m.ProfileComponent),
	},
	//{ path: '**', component: ErrorComponent, data: { code: 404, message: 'Page Not Found' } },
];