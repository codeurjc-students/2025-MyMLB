import { Component } from '@angular/core';
import { Support } from "../support/support.component";

@Component({
	selector: 'app-footer',
	imports: [Support],
	standalone: true,
	templateUrl: './footer.component.html',
})
export class Footer {
	public currentYear = new Date().getFullYear();
	public showModal = false;
}