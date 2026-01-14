export interface User {
	email: string,
	username: string
}

export type EditProfileRequest = {
	email: string,
	password: string
}