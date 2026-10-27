import type { UserResponseDTO } from './user';

export interface LoginDTO {
  email: string;
  password: string;
}

export interface LoginResponseDTO extends UserResponseDTO {
  access_token?: string;
  refresh_token?: string;
  token_type?: string;
}
