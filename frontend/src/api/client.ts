import axios, { AxiosResponse } from 'axios';
import type { ResponseTemplate } from '../types/api';

const baseURL = import.meta.env.VITE_API_URL || '';

export const apiClient = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response: AxiosResponse<ResponseTemplate<any>>) => {
    if (response.data && response.data.code && response.data.code !== '0') {
      const errorMessage = response.data.message || 'Request failed with error code: ' + response.data.code;
      return Promise.reject(new Error(errorMessage));
    }
    return response;
  },
  (error) => {
    let message = 'An unexpected error occurred';
    if (error.response?.data) {
      const data = error.response.data;
      if (typeof data.message === 'string') {
        message = data.message;
      } else if (typeof data.detail === 'string') {
        message = data.detail;
      } else if (Array.isArray(data.detail)) {
        message = data.detail.map((d: any) => d.msg || JSON.stringify(d)).join(', ');
      }
    } else if (error.message) {
      message = error.message;
    }
    return Promise.reject(new Error(message));
  }
);
