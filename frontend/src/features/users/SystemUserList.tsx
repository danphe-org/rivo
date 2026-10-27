import React, { useState, useEffect, useCallback } from 'react';
import {
  Search,
  UserPlus,
  RefreshCw,
  Trash2,
  Edit3,
  Eye,
  Filter,
} from 'lucide-react';
import { systemUserApi } from '../../api/systemUsers';
import { useToast } from '../../context/ToastContext';
import type { UserResponseDTO, UserFilter } from '../../types/user';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Badge } from '../../components/ui/Badge';
import { Pagination } from '../../components/ui/Pagination';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { SystemUserFormModal } from './SystemUserFormModal';
import { SystemUserDetailModal } from './SystemUserDetailModal';
import { SystemUserStats } from './SystemUserStats';

export const SystemUserList: React.FC = () => {
  const { showSuccess, showError } = useToast();

  const [users, setUsers] = useState<UserResponseDTO[]>([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // Filter States
  const [pageNumber, setPageNumber] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchParameter, setSearchParameter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState<string>('all');
  const [sortField, setSortField] = useState('created_on');
  const [sortOrder, setSortOrder] = useState<'ASC' | 'DESC'>('DESC');

  // Modal States
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedUserForEdit, setSelectedUserForEdit] = useState<UserResponseDTO | null>(null);

  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [selectedUserForDetail, setSelectedUserForDetail] = useState<UserResponseDTO | null>(null);

  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<UserResponseDTO | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    try {
      const filter: UserFilter = {
        pageNumber,
        pageSize,
        searchParameter: searchParameter.trim() || undefined,
        role: roleFilter || undefined,
        sortField,
        sortOrder,
      };

      if (activeFilter === 'true') {
        filter.active = true;
      } else if (activeFilter === 'false') {
        filter.active = false;
      }

      const res = await systemUserApi.searchUsers(filter);
      setUsers(res.data || []);
      setTotalRecords(res.totalRecords || 0);
    } catch (err: any) {
      showError(err.message || 'Failed to load system users');
    } finally {
      setIsLoading(false);
    }
  }, [pageNumber, pageSize, searchParameter, roleFilter, activeFilter, sortField, sortOrder, showError]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPageNumber(1);
    fetchUsers();
  };

  const handleResetFilters = () => {
    setSearchParameter('');
    setRoleFilter('');
    setActiveFilter('all');
    setSortField('created_on');
    setSortOrder('DESC');
    setPageNumber(1);
  };

  const handleDeleteConfirm = async () => {
    if (!userToDelete) return;
    setIsDeleting(true);
    try {
      await systemUserApi.deleteUser(userToDelete.id);
      showSuccess(`User "${userToDelete.name}" deleted successfully`);
      setIsDeleteOpen(false);
      setUserToDelete(null);
      fetchUsers();
    } catch (err: any) {
      showError(err.message || 'Failed to delete user');
    } finally {
      setIsDeleting(false);
    }
  };

  const formatDate = (dateStr?: string | null) => {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">System Users</h1>
          <p className="text-sm text-slate-500 mt-1">
            Backoffice user directory, role assignment, and CRUD management.
          </p>
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <Button
            variant="outline"
            size="md"
            onClick={fetchUsers}
            disabled={isLoading}
            leftIcon={<RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>

          <Button
            variant="primary"
            size="md"
            onClick={() => {
              setSelectedUserForEdit(null);
              setIsFormOpen(true);
            }}
            leftIcon={<UserPlus className="w-4 h-4" />}
          >
            Register User
          </Button>
        </div>
      </div>

      {/* KPI Stats Bar */}
      <SystemUserStats users={users} totalRecords={totalRecords} />

      {/* Filter and Search Bar */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-12 gap-3">
          <div className="md:col-span-4">
            <Input
              placeholder="Search by name, email, or phone..."
              value={searchParameter}
              onChange={(e) => setSearchParameter(e.target.value)}
              leftIcon={<Search className="w-4 h-4" />}
            />
          </div>

          <div className="md:col-span-3">
            <Select
              value={roleFilter}
              onChange={(e) => {
                setRoleFilter(e.target.value);
                setPageNumber(1);
              }}
              options={[
                { value: '', label: 'All Roles' },
                { value: 'USER', label: 'User' },
                { value: 'ADMIN', label: 'Admin' },
                { value: 'MANAGER', label: 'Manager' },
                { value: 'STAFF', label: 'Staff' },
              ]}
            />
          </div>

          <div className="md:col-span-2">
            <Select
              value={activeFilter}
              onChange={(e) => {
                setActiveFilter(e.target.value);
                setPageNumber(1);
              }}
              options={[
                { value: 'all', label: 'All Statuses' },
                { value: 'true', label: 'Active Only' },
                { value: 'false', label: 'Inactive Only' },
              ]}
            />
          </div>

          <div className="md:col-span-3 flex items-center gap-2">
            <Button type="submit" variant="primary" className="flex-1" leftIcon={<Filter className="w-4 h-4" />}>
              Filter
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={handleResetFilters}
              title="Reset all filters"
            >
              Reset
            </Button>
          </div>
        </form>
      </div>

      {/* Data Table Container */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50/80 text-xs uppercase font-bold text-slate-500 tracking-wider border-b border-slate-100">
              <tr>
                <th className="py-3.5 px-6">User / Identity</th>
                <th className="py-3.5 px-6">System Role</th>
                <th className="py-3.5 px-6">Phone Number</th>
                <th className="py-3.5 px-6">Account Status</th>
                <th className="py-3.5 px-6">Registered On</th>
                <th className="py-3.5 px-6 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading && users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-16 text-center text-slate-400">
                    <RefreshCw className="w-8 h-8 mx-auto animate-spin text-emerald-500 mb-3" />
                    <p className="font-medium text-slate-600">Loading system users...</p>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-16 text-center">
                    <div className="max-w-sm mx-auto">
                      <div className="w-12 h-12 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
                        <Search className="w-6 h-6" />
                      </div>
                      <h3 className="text-base font-bold text-slate-800">No users found</h3>
                      <p className="text-xs text-slate-500 mt-1">
                        Try adjusting your search criteria or register a new user.
                      </p>
                      <Button
                        variant="primary"
                        size="sm"
                        className="mt-4"
                        onClick={() => {
                          setSelectedUserForEdit(null);
                          setIsFormOpen(true);
                        }}
                        leftIcon={<UserPlus className="w-4 h-4" />}
                      >
                        Register User
                      </Button>
                    </div>
                  </td>
                </tr>
              ) : (
                users.map((user) => {
                  const isActive = user.active && !user.deleted;
                  return (
                    <tr
                      key={user.id}
                      className="hover:bg-slate-50/70 transition-colors group cursor-pointer"
                      onClick={() => {
                        setSelectedUserForDetail(user);
                        setIsDetailOpen(true);
                      }}
                    >
                      {/* Name & Email */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-slate-100 text-slate-700 font-bold flex items-center justify-center text-sm ring-1 ring-slate-200 group-hover:bg-emerald-50 group-hover:text-emerald-700 group-hover:ring-emerald-200 transition-colors">
                            {user.name.charAt(0).toUpperCase()}
                          </div>
                          <div>
                            <p className="font-bold text-slate-900 group-hover:text-emerald-700 transition-colors">
                              {user.name}
                            </p>
                            <p className="text-xs text-slate-500">{user.email}</p>
                          </div>
                        </div>
                      </td>

                      {/* Role Badge */}
                      <td className="py-4 px-6">
                        <Badge role={user.role}>{user.role}</Badge>
                      </td>

                      {/* Phone */}
                      <td className="py-4 px-6 text-slate-600 font-mono text-xs">
                        {user.phone || <span className="text-slate-300 font-sans">—</span>}
                      </td>

                      {/* Status */}
                      <td className="py-4 px-6">
                        <Badge variant={isActive ? 'active' : 'inactive'}>
                          {isActive ? 'Active' : user.deleted ? 'Deleted' : 'Inactive'}
                        </Badge>
                      </td>

                      {/* Registered Date */}
                      <td className="py-4 px-6 text-xs text-slate-500">
                        {formatDate(user.created_on)}
                      </td>

                      {/* Action Buttons */}
                      <td
                        className="py-4 px-6 text-right"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => {
                              setSelectedUserForDetail(user);
                              setIsDetailOpen(true);
                            }}
                            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                            title="View Details"
                          >
                            <Eye className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => {
                              setSelectedUserForEdit(user);
                              setIsFormOpen(true);
                            }}
                            className="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                            title="Edit User"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => {
                              setUserToDelete(user);
                              setIsDeleteOpen(true);
                            }}
                            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                            title="Delete User"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <Pagination
          currentPage={pageNumber}
          totalRecords={totalRecords}
          pageSize={pageSize}
          onPageChange={(page) => setPageNumber(page)}
          onPageSizeChange={(size) => {
            setPageSize(size);
            setPageNumber(1);
          }}
        />
      </div>

      {/* User Form Modal (Register & Edit) */}
      <SystemUserFormModal
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setSelectedUserForEdit(null);
        }}
        onSuccess={() => fetchUsers()}
        userToEdit={selectedUserForEdit}
      />

      {/* User Detail Modal */}
      <SystemUserDetailModal
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedUserForDetail(null);
        }}
        user={selectedUserForDetail}
        onEditClick={(user) => {
          setSelectedUserForEdit(user);
          setIsFormOpen(true);
        }}
      />

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => {
          setIsDeleteOpen(false);
          setUserToDelete(null);
        }}
        onConfirm={handleDeleteConfirm}
        title="Delete System User"
        message={`Are you sure you want to delete user "${userToDelete?.name}" (${userToDelete?.email})? This action soft-deletes the record in accordance with system audit policies.`}
        confirmLabel="Delete User"
        isDestructive
        isLoading={isDeleting}
      />
    </div>
  );
};
