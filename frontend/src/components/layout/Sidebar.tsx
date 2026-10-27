import React from 'react';
import {
  Users,
  ShieldAlert,
  Server,
  Layers,
  Sparkles,
  ExternalLink
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentTab, onTabChange }) => {
  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shrink-0 min-h-screen border-r border-slate-800">
      {/* Brand Logo */}
      <div className="h-16 flex items-center px-6 gap-3 border-b border-slate-800/80 bg-slate-950/40">
        <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center text-white font-black text-lg shadow-lg shadow-emerald-500/20">
          R
        </div>
        <div>
          <h1 className="text-white font-bold text-base tracking-wide flex items-center gap-1.5">
            RIVO <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded-md bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">System</span>
          </h1>
          <p className="text-[11px] text-slate-400">Enterprise Admin Portal</p>
        </div>
      </div>

      {/* Navigation Sections */}
      <div className="flex-1 py-6 px-3 space-y-6 overflow-y-auto">
        <div>
          <p className="px-3 text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            System Administration
          </p>
          <nav className="space-y-1">
            <button
              onClick={() => onTabChange('users')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                currentTab === 'users'
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20 font-semibold'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Users className="w-4 h-4 shrink-0" />
              <span>User Registration & CRUD</span>
            </button>
            <button
              onClick={() => onTabChange('roles')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                currentTab === 'roles'
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20 font-semibold'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>Roles & Privileges</span>
              <span className="ml-auto text-[10px] bg-slate-800 text-slate-400 px-1.5 py-0.5 rounded">System</span>
            </button>
          </nav>
        </div>

        <div>
          <p className="px-3 text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Infrastructure & Core
          </p>
          <nav className="space-y-1">
            <button
              onClick={() => onTabChange('health')}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                currentTab === 'health'
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20 font-semibold'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Server className="w-4 h-4 shrink-0" />
              <span>API Health & Gateway</span>
            </button>
          </nav>
        </div>

        <div>
          <p className="px-3 text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Client Portal (Storefront)
          </p>
          <div className="px-3 py-3 rounded-xl bg-slate-800/40 border border-slate-800 text-xs text-slate-400 space-y-2">
            <div className="flex items-center gap-2 text-slate-300 font-medium">
              <Layers className="w-4 h-4 text-emerald-400" />
              <span>Client Portal Scope</span>
            </div>
            <p className="text-[11px] leading-relaxed">
              Client self-registration & customer accounts will be managed here.
            </p>
          </div>
        </div>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800/80 bg-slate-950/30 text-xs text-slate-400 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
          <span>v1.0.0 Backoffice</span>
        </div>
        <a
          href="http://localhost:8000/docs"
          target="_blank"
          rel="noreferrer"
          className="text-slate-400 hover:text-emerald-400 flex items-center gap-1 transition-colors"
          title="Open FastAPI Swagger Docs"
        >
          <span>Docs</span>
          <ExternalLink className="w-3 h-3" />
        </a>
      </div>
    </aside>
  );
};
