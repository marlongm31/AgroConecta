import { api } from '../lib/api';
import type { ApiResponse, Expense, ExpenseRequest, PageResponse } from '../types';

export const expenseService = {
  async list(page = 0, size = 50): Promise<PageResponse<Expense>> {
    const { data } = await api.get<ApiResponse<PageResponse<Expense>>>('/expenses', {
      params: { page, size },
    });
    return data.data;
  },
  async create(payload: ExpenseRequest): Promise<Expense> {
    const { data } = await api.post<ApiResponse<Expense>>('/expenses', payload);
    return data.data;
  },
  async update(id: number, payload: ExpenseRequest): Promise<Expense> {
    const { data } = await api.put<ApiResponse<Expense>>(`/expenses/${id}`, payload);
    return data.data;
  },
  async remove(id: number): Promise<void> {
    await api.delete(`/expenses/${id}`);
  },
};
