import { AbstractFilter } from './api';

export type UserRole = 'USER' | 'ADMIN' | 'MANAGER' | 'STAFF' | string;

export interface UserRoleResponseDTO {
  id: string;
  user_id: string;
  role: string;
  created_on?: string | null;
  modified_on?: string | null;
  active: boolean;
  deleted: boolean;
}

export interface UserResponseDTO {
  id: string;
  name: string;
  email: string;
  phone?: string | null;
  role: string;
  roles?: UserRoleResponseDTO[] | null;
  created_on?: string | null;
  modified_on?: string | null;
  created_by?: string | null;
  modified_by?: string | null;
  updated_on?: string | null;
  updated_by?: string | null;
  active: boolean;
  deleted: boolean;
  remark?: string | null;
  version?: number | null;
}

export interface UserCreateDTO {
  name: string;
  email: string;
  password?: string;
  phone?: string;
  role?: string;
}

export interface UserUpdateDTO {
  name?: string;
  email?: string;
  password?: string;
  phone?: string;
  role?: string;
  active?: boolean;
}

export interface UserFilter extends AbstractFilter {
  role?: string;
  email?: string;
}
