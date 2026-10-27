import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { Badge } from '../../components/ui/Badge';

export const RolesOverview: React.FC = () => {
  const roles = [
    {
      name: 'ADMIN',
      description: 'Super-administrative authority across the whole Rivo platform. Has access to all system APIs, user lifecycle, and role assignment.',
      permissions: ['ALL_PERMISSIONS', 'SYSTEM_USERS_MANAGE', 'PORTAL_CONFIG', 'AUDIT_LOGS_VIEW'],
      level: 'Tier 1 (Highest)',
    },
    {
      name: 'MANAGER',
      description: 'Departmental management role for handling client operations, reports, and team-level auditing.',
      permissions: ['USERS_READ', 'REPORTS_GENERATE', 'CLIENT_STORES_MANAGE'],
      level: 'Tier 2',
    },
    {
      name: 'STAFF',
      description: 'Internal operational staff with read-only backoffice permissions and day-to-day transaction processing.',
      permissions: ['CLIENT_ORDERS_VIEW', 'SUPPORT_TICKETS_REPLY'],
      level: 'Tier 3',
    },
    {
      name: 'USER',
      description: 'Default baseline authenticated role. Granted to registered system users and accounts.',
      permissions: ['PROFILE_MANAGE', 'STORE_ACCESS'],
      level: 'Tier 4',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">System Roles &amp; Privileges</h1>
        <p className="text-sm text-slate-500 mt-1">
          Authority hierarchies and permissions defined in the Rivo system backend.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {roles.map((r) => (
          <div key={r.name} className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2.5 rounded-xl bg-purple-50 text-purple-600">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-800 text-base">{r.name}</h3>
                  <span className="text-xs text-slate-400">{r.level}</span>
                </div>
              </div>
              <Badge role={r.name}>{r.name}</Badge>
            </div>

            <p className="text-xs text-slate-600 leading-relaxed">{r.description}</p>

            <div className="pt-2 border-t border-slate-100">
              <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block mb-2">
                Sample Grants
              </span>
              <div className="flex flex-wrap gap-1.5">
                {r.permissions.map((p) => (
                  <span key={p} className="text-[11px] font-mono bg-slate-100 text-slate-700 px-2 py-0.5 rounded-md">
                    {p}
                  </span>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
