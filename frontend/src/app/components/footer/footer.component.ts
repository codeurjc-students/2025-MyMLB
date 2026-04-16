import { Component } from '@angular/core';
import { Support } from "../support/support.component";
import { MatIconModule } from '@angular/material/icon';

@Component({
	selector: 'app-footer',
	imports: [Support, MatIconModule],
	standalone: true,
	templateUrl: './footer.component.html',
})
export class Footer {
	public currentYear = new Date().getFullYear();
	public showModal = false;
}