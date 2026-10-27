export interface ResponseTemplate<T> {
  code: string;
  message: string;
  data: T;
  time: string;
}

export interface Page<T> {
  totalRecords: number;
  limit: number;
  currentPage: number;
  data: T[];
}

export interface AbstractFilter {
  pageNumber?: number;
  pageSize?: number;
  searchParameter?: string;
  sortField?: string;
  sortOrder?: 'ASC' | 'DESC';
  active?: boolean;
}
