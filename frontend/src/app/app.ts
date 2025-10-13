import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { initFlowbite } from "flowbite";
import { NavbarComponent } from './components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
	title = "web-app";
	public hideNavbar = false;

	constructor(private router: Router) {
		this.router.events.subscribe(() => {
			const url = this.router.url;
			this.hideNavbar = url.startsWith('/auth') || url.startsWith('/recovery');
		});
	}

	ngOnInit(): void {
		initFlowbite();
	}
}