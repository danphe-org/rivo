import React from 'react';
import { Users, UserCheck, ShieldCheck, UserX } from 'lucide-react';
import type { UserResponseDTO } from '../../types/user';

interface SystemUserStatsProps {
  users: UserResponseDTO[];
  totalRecords: number;
}

export const SystemUserStats: React.FC<SystemUserStatsProps> = ({ users, totalRecords }) => {
  const activeCount = users.filter((u) => u.active && !u.deleted).length;
  const adminCount = users.filter((u) => u.role?.toUpperCase() === 'ADMIN').length;
  const inactiveCount = users.filter((u) => !u.active || u.deleted).length;

  const stats = [
    {
      title: 'Total System Users',
      value: totalRecords,
      icon: Users,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      borderColor: 'border-blue-100',
    },
    {
      title: 'Active Accounts',
      value: activeCount,
      icon: UserCheck,
      color: 'text-emerald-600',
      bgColor: 'bg-emerald-50',
      borderColor: 'border-emerald-100',
    },
    {
      title: 'Admin Authorities',
      value: adminCount,
      icon: ShieldCheck,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      borderColor: 'border-purple-100',
    },
    {
      title: 'Inactive / Suspended',
      value: inactiveCount,
      icon: UserX,
      color: 'text-amber-600',
      bgColor: 'bg-amber-50',
      borderColor: 'border-amber-100',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {stats.map((stat) => {
        const IconComponent = stat.icon;
        return (
          <div
            key={stat.title}
            className={`p-5 rounded-2xl bg-white border ${stat.borderColor} shadow-sm flex items-center justify-between`}
          >
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                {stat.title}
              </p>
              <h3 className="text-2xl font-black text-slate-800 mt-1">{stat.value}</h3>
            </div>
            <div className={`p-3 rounded-xl ${stat.bgColor} ${stat.color}`}>
              <IconComponent className="w-5 h-5" />
            </div>
          </div>
        );
      })}
    </div>
  );
};
