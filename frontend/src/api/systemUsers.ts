import { apiClient } from './client';
import type { Page, ResponseTemplate } from '../types/api';
import type {
  UserCreateDTO,
  UserFilter,
  UserResponseDTO,
  UserUpdateDTO,
} from '../types/user';

export const systemUserApi = {
  async searchUsers(filter: UserFilter = {}): Promise<Page<UserResponseDTO>> {
    const response = await apiClient.post<ResponseTemplate<Page<UserResponseDTO>>>(
      '/api/system/users/search',
      filter
    );
    return response.data.data;
  },

  async getUserById(id: string): Promise<UserResponseDTO> {
    const response = await apiClient.get<ResponseTemplate<UserResponseDTO>>(
      `/api/system/users/${id}`
    );
    return response.data.data;
  },

  async createUser(dto: UserCreateDTO): Promise<UserResponseDTO> {
    const response = await apiClient.post<ResponseTemplate<UserResponseDTO>>(
      '/api/system/users',
      dto
    );
    return response.data.data;
  },

  async updateUser(id: string, dto: UserUpdateDTO): Promise<UserResponseDTO> {
    const response = await apiClient.put<ResponseTemplate<UserResponseDTO>>(
      `/api/system/users/${id}`,
      dto
    );
    return response.data.data;
  },

  async deleteUser(id: string): Promise<void> {
    await apiClient.delete<ResponseTemplate<null>>(`/api/system/users/${id}`);
  },
};
