import React from 'react';

export interface BadgeProps {
  children: React.ReactNode;
  variant?: 'role' | 'active' | 'inactive' | 'default';
  role?: string;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  role,
  className = '',
}) => {
  let colorStyles = 'bg-slate-100 text-slate-700 border-slate-200';

  if (variant === 'active') {
    colorStyles = 'bg-emerald-50 text-emerald-700 border-emerald-200 ring-1 ring-emerald-500/10';
  } else if (variant === 'inactive') {
    colorStyles = 'bg-rose-50 text-rose-700 border-rose-200 ring-1 ring-rose-500/10';
  } else if (variant === 'role' || role) {
    const roleKey = (role || String(children)).toUpperCase();
    if (roleKey === 'ADMIN' || roleKey === 'SUPERADMIN') {
      colorStyles = 'bg-purple-50 text-purple-700 border-purple-200 ring-1 ring-purple-500/10';
    } else if (roleKey === 'MANAGER') {
      colorStyles = 'bg-amber-50 text-amber-700 border-amber-200 ring-1 ring-amber-500/10';
    } else if (roleKey === 'STAFF') {
      colorStyles = 'bg-sky-50 text-sky-700 border-sky-200 ring-1 ring-sky-500/10';
    } else {
      colorStyles = 'bg-blue-50 text-blue-700 border-blue-200 ring-1 ring-blue-500/10';
    }
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${colorStyles} ${className}`}
    >
      {children}
    </span>
  );
};
