export type UserRole = {
	username: string,
	roles: Array<string>
}

export type AuthResponse = {
	status: "SUCCESS" | "FAILURE";
	message: string
}

export type LoginRequest = {
	username: string,
	password: string
}

export type RegisterRequest = {
	email: string,
	username: string,
	password: string
}

export type ForgotPasswordRequest = {
	email: string;
}

export type ResetPasswordRequest = {
	code: string,
	newPassword: string
}