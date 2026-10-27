import { apiClient } from './client';
import type { ResponseTemplate } from '../types/api';
import type { LoginDTO, LoginResponseDTO } from '../types/auth';
import type { UserResponseDTO } from '../types/user';

export const authApi = {
  async loginSystem(dto: LoginDTO): Promise<LoginResponseDTO> {
    const res = await apiClient.post<ResponseTemplate<LoginResponseDTO>>(
      '/api/system/auth/login',
      dto
    );
    return res.data.data;
  },

  async loginClient(dto: LoginDTO): Promise<LoginResponseDTO> {
    const res = await apiClient.post<ResponseTemplate<LoginResponseDTO>>(
      '/api/client/auth/login',
      dto
    );
    return res.data.data;
  },

  async login(dto: LoginDTO): Promise<LoginResponseDTO> {
    try {
      return await authApi.loginSystem(dto);
    } catch (err: any) {
      const msg = (err.message || '').toLowerCase();
      if (msg.includes('access denied') || msg.includes('administrative privileges')) {
        return await authApi.loginClient(dto);
      }
      throw err;
    }
  },

  async logoutSystem(): Promise<void> {
    try {
      await apiClient.post<ResponseTemplate<any>>('/api/system/auth/logout');
    } catch {
      await apiClient.post<ResponseTemplate<any>>('/api/client/auth/logout');
    }
  },

  async getMe(): Promise<UserResponseDTO> {
    try {
      const res = await apiClient.get<ResponseTemplate<UserResponseDTO>>(
        '/api/system/auth/me'
      );
      return res.data.data;
    } catch {
      const res = await apiClient.get<ResponseTemplate<UserResponseDTO>>(
        '/api/client/auth/me'
      );
      return res.data.data;
    }
  },
};
