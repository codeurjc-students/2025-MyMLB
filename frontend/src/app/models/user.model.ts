import { Pictures } from "./pictures.model"

export interface User {
	email: string,
	username: string
}

export type EditProfileRequest = {
	email?: string,
	password?: string,
	enableNotifications?: boolean
}

export type Profile = {
	email: string,
	picture: Pictures | null,
	enableNotifications: boolean
}