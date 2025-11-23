import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { AuthComponent } from './components/auth/auth.component';
import { LoginComponent } from './components/login/login.component';
import { PasswordComponent } from './components/password-recovery/password.component';
import { AuthGuard } from './guards/auth.guard';
import { ErrorComponent } from './components/error/error.component';
import { TeamComponent } from './components/team/team.component';
import { StandingsComponent } from './components/standings/standings.component';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'auth', component: AuthComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'recovery', component: PasswordComponent },
	{ path: 'team/:name', component: TeamComponent },
	{ path: 'standings', component: StandingsComponent },
	{ path: 'error', component: ErrorComponent },
	{
		path: 'profile',
		canActivate: [AuthGuard],
		loadComponent: () =>
			import('./components/profile/profile.component').then((m) => m.ProfileComponent),
	},
	{
		path: 'favorite-teams',
		canActivate: [AuthGuard],
		loadComponent: () =>
			import('./components/team/fav-team/fav-team.component').then((m) => m.FavTeamComponent)
	},
	{
		path: 'edit-menu',
		canActivate: [AdminGuard],
		loadComponent: () =>
			import('./components/admin/edit-menu/edit-menu.component').then((m) => m.EditMenuComponent)
	},
	{ path: '**', redirectTo: 'error', component: ErrorComponent, data: { code: 404, message: 'Page Not Found' } },
];