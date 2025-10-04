import { Routes } from '@angular/router';
import { HomeComponent } from './Components/Home/Home.component';
import { AuthComponent } from './Components/Auth/Auth.component';
import { LoginComponent } from './Components/Login/Login.component';
import { RegisterComponent } from './Components/Register/Register.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: "auth", component: AuthComponent },
	{ path: "login", component: LoginComponent }
];