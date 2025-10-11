import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { User } from '../../Models/user.model';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-user',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './user.component.html'
})
export class UserComponent implements OnInit {
	public allUsers : User[] = [];
	public success: boolean = true;

	constructor(private userService: UserService) {}

	ngOnInit(): void {
		this.userService.getAllUsers().subscribe({
			next: (users) => {
				this.allUsers = users;
			},
			error: (err) => {
				console.error("An error ocurred while obtaining the users ", err);
				this.success = false;
			}
		})
	}
}