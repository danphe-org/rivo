import React from 'react';
import { Modal } from '../../components/ui/Modal';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import type { UserResponseDTO } from '../../types/user';
import { Mail, Phone, Shield, Hash, Clock } from 'lucide-react';

interface SystemUserDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: UserResponseDTO | null;
  onEditClick: (user: UserResponseDTO) => void;
}

export const SystemUserDetailModal: React.FC<SystemUserDetailModalProps> = ({
  isOpen,
  onClose,
  user,
  onEditClick,
}) => {
  if (!user) return null;

  const formatDate = (dateStr?: string | null) => {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="User Details & Audit Trail"
      description="Comprehensive profile, roles, and system metadata."
      maxWidth="lg"
    >
      <div className="space-y-6">
        {/* Header Profile Card */}
        <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 border border-slate-100">
          <div className="w-14 h-14 rounded-2xl bg-emerald-600 text-white flex items-center justify-center font-bold text-xl shadow-md shadow-emerald-500/20 shrink-0">
            {user.name.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h4 className="text-base font-bold text-slate-900 truncate">{user.name}</h4>
              <Badge variant={user.active && !user.deleted ? 'active' : 'inactive'}>
                {user.active && !user.deleted ? 'Active' : user.deleted ? 'Deleted' : 'Inactive'}
              </Badge>
              <Badge role={user.role}>{user.role}</Badge>
            </div>
            <p className="text-xs text-slate-500 mt-0.5 truncate">{user.email}</p>
          </div>
        </div>

        {/* Identity & Contact Info */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
            <span className="text-slate-400 font-semibold flex items-center gap-1.5 mb-1 uppercase tracking-wider">
              <Mail className="w-3.5 h-3.5 text-slate-400" />
              Email Address
            </span>
            <span className="font-semibold text-slate-800 text-sm">{user.email}</span>
          </div>

          <div className="p-3.5 rounded-xl border border-slate-100 bg-white">
            <span className="text-slate-400 font-semibold flex items-center gap-1.5 mb-1 uppercase tracking-wider">
              <Phone className="w-3.5 h-3.5 text-slate-400" />
              Phone Number
            </span>
            <span className="font-semibold text-slate-800 text-sm">
              {user.phone || 'Not configured'}
            </span>
          </div>

          <div className="p-3.5 rounded-xl border border-slate-100 bg-white sm:col-span-2">
            <span className="text-slate-400 font-semibold flex items-center gap-1.5 mb-1 uppercase tracking-wider">
              <Hash className="w-3.5 h-3.5 text-slate-400" />
              User ID (UUID)
            </span>
            <code className="text-xs text-slate-700 font-mono select-all bg-slate-50 px-2 py-1 rounded-lg border border-slate-200/60 block w-full truncate">
              {user.id}
            </code>
          </div>
        </div>

        {/* Roles List */}
        {user.roles && user.roles.length > 0 && (
          <div>
            <h5 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
              <Shield className="w-3.5 h-3.5 text-emerald-600" />
              Assigned Role Entities
            </h5>
            <div className="space-y-2">
              {user.roles.map((r) => (
                <div
                  key={r.id}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs"
                >
                  <div className="flex items-center gap-2">
                    <Badge role={r.role}>{r.role}</Badge>
                    <span className="text-slate-500 font-mono text-[11px]">{r.id}</span>
                  </div>
                  <Badge variant={r.active ? 'active' : 'inactive'}>
                    {r.active ? 'Enabled' : 'Disabled'}
                  </Badge>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Audit Trail Metadata */}
        <div>
          <h5 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-slate-500" />
            Audit Trail (Danphe / Bazar Architecture)
          </h5>
          <div className="rounded-xl border border-slate-100 bg-slate-50/60 p-3.5 grid grid-cols-2 gap-3 text-xs text-slate-600">
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Created On</p>
              <p className="font-semibold text-slate-800 mt-0.5">{formatDate(user.created_on)}</p>
            </div>
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Created By</p>
              <p className="font-semibold text-slate-800 mt-0.5">{user.created_by || 'SYSTEM'}</p>
            </div>
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Last Modified On</p>
              <p className="font-semibold text-slate-800 mt-0.5">{formatDate(user.modified_on || user.updated_on)}</p>
            </div>
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Last Modified By</p>
              <p className="font-semibold text-slate-800 mt-0.5">{user.modified_by || user.updated_by || '—'}</p>
            </div>
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Version Counter</p>
              <p className="font-semibold text-slate-800 mt-0.5">v{user.version ?? 1}</p>
            </div>
            <div>
              <p className="text-slate-400 font-medium text-[11px]">Audit Remark</p>
              <p className="font-semibold text-slate-800 mt-0.5">{user.remark || 'Standard Record'}</p>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
          <Button
            variant="primary"
            onClick={() => {
              onClose();
              onEditClick(user);
            }}
          >
            Edit User
          </Button>
        </div>
      </div>
    </Modal>
  );
};
