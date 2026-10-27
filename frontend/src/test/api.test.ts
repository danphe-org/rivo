import { describe, it, expect, vi, beforeEach } from 'vitest';
import { systemUserApi } from '../api/systemUsers';
import { authApi } from '../api/auth';
import { apiClient } from '../api/client';

describe('System User & Auth API Service', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('searches users with pagination and criteria envelope', async () => {
    const mockResponse = {
      data: {
        code: '0',
        message: 'Success',
        data: {
          totalRecords: 2,
          limit: 10,
          currentPage: 1,
          data: [
            {
              id: 'u-1',
              name: 'Jane Doe',
              email: 'jane@example.com',
              role: 'USER',
              active: true,
              deleted: false,
            },
            {
              id: 'u-2',
              name: 'John Admin',
              email: 'admin@example.com',
              role: 'ADMIN',
              active: true,
              deleted: false,
            },
          ],
        },
        time: '2026-10-27T12:00:00Z',
      },
    };

    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue(mockResponse as any);

    const result = await systemUserApi.searchUsers({ searchParameter: 'Jane' });
    expect(postSpy).toHaveBeenCalledWith('/api/system/users/search', { searchParameter: 'Jane' });
    expect(result.totalRecords).toBe(2);
    expect(result.data.length).toBe(2);
    expect(result.data[0].name).toBe('Jane Doe');
  });

  it('creates a new system user', async () => {
    const mockResponse = {
      data: {
        code: '0',
        message: 'User created successfully',
        data: {
          id: 'u-created-1',
          name: 'Jane Doe',
          email: 'jane.doe@example.com',
          role: 'USER',
          active: true,
          deleted: false,
        },
        time: '2026-10-27T12:00:00Z',
      },
    };

    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue(mockResponse as any);

    const result = await systemUserApi.createUser({
      name: 'Jane Doe',
      email: 'jane.doe@example.com',
      password: 'SecurePassword123!',
      role: 'USER',
    });

    expect(postSpy).toHaveBeenCalledWith('/api/system/users', {
      name: 'Jane Doe',
      email: 'jane.doe@example.com',
      password: 'SecurePassword123!',
      role: 'USER',
    });
    expect(result.id).toBe('u-created-1');
  });

  it('updates an existing system user', async () => {
    const mockResponse = {
      data: {
        code: '0',
        message: 'User updated successfully',
        data: {
          id: 'u-1',
          name: 'Jane Updated',
          email: 'jane.updated@example.com',
          role: 'ADMIN',
          active: true,
          deleted: false,
        },
        time: '2026-10-27T12:00:00Z',
      },
    };

    const putSpy = vi.spyOn(apiClient, 'put').mockResolvedValue(mockResponse as any);

    const result = await systemUserApi.updateUser('u-1', {
      name: 'Jane Updated',
      role: 'ADMIN',
    });

    expect(putSpy).toHaveBeenCalledWith('/api/system/users/u-1', {
      name: 'Jane Updated',
      role: 'ADMIN',
    });
    expect(result.name).toBe('Jane Updated');
  });

  it('deletes a system user', async () => {
    const mockResponse = {
      data: {
        code: '0',
        message: 'User deleted successfully',
        data: null,
        time: '2026-10-27T12:00:00Z',
      },
    };

    const deleteSpy = vi.spyOn(apiClient, 'delete').mockResolvedValue(mockResponse as any);

    await systemUserApi.deleteUser('u-1');
    expect(deleteSpy).toHaveBeenCalledWith('/api/system/users/u-1');
  });

  it('authenticates system user successfully', async () => {
    const mockLoginRes = {
      data: {
        code: '0',
        message: 'Login successful',
        data: {
          id: 'admin-1',
          name: 'System Admin',
          email: 'admin@rivo.internal',
          role: 'ADMIN',
          access_token: 'jwt-access-token',
        },
      },
    };

    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue(mockLoginRes as any);

    const result = await authApi.login({
      email: 'admin@rivo.internal',
      password: 'SecurePassword123!',
    });

    expect(postSpy).toHaveBeenCalledWith('/api/system/auth/login', {
      email: 'admin@rivo.internal',
      password: 'SecurePassword123!',
    });
    expect(result.id).toBe('admin-1');
  });

  it('falls back to client auth when system portal access is denied', async () => {
    const postSpy = vi
      .spyOn(apiClient, 'post')
      .mockRejectedValueOnce(new Error('Access denied: Administrative privileges required'))
      .mockResolvedValueOnce({
        data: {
          code: '0',
          message: 'Login successful',
          data: {
            id: 'user-1',
            name: 'Regular User',
            email: 'user@example.com',
            role: 'USER',
            access_token: 'client-access-token',
          },
        },
      } as any);

    const result = await authApi.login({
      email: 'user@example.com',
      password: 'SecurePassword123!',
    });

    expect(postSpy).toHaveBeenCalledTimes(2);
    expect(postSpy).toHaveBeenNthCalledWith(1, '/api/system/auth/login', {
      email: 'user@example.com',
      password: 'SecurePassword123!',
    });
    expect(postSpy).toHaveBeenNthCalledWith(2, '/api/client/auth/login', {
      email: 'user@example.com',
      password: 'SecurePassword123!',
    });
    expect(result.id).toBe('user-1');
  });
});
