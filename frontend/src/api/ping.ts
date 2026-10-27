import { apiClient } from './client';
import type { ResponseTemplate } from '../types/api';

export async function pingSystem(): Promise<string> {
  const res = await apiClient.get<ResponseTemplate<string>>('/api/system/ping');
  return res.data.data;
}

export async function pingClient(): Promise<string> {
  const res = await apiClient.get<ResponseTemplate<string>>('/api/client/ping');
  return res.data.data;
}
