import React, { useEffect, useState } from 'react';
import { pingSystem } from '../../api/ping';
import { RefreshCw } from 'lucide-react';

export const Header: React.FC = () => {
  const [apiOnline, setApiOnline] = useState<boolean | null>(null);
  const [checking, setChecking] = useState<boolean>(false);

  const checkHealth = async () => {
    setChecking(true);
    try {
      const res = await pingSystem();
      setApiOnline(res === 'pong');
    } catch {
      setApiOnline(false);
    } finally {
      setChecking(false);
    }
  };

  useEffect(() => {
    checkHealth();
    const interval = setInterval(checkHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="h-16 bg-white border-b border-slate-200/80 px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm/50">
      <div className="flex items-center gap-3">
        <div>
          <h2 className="text-base font-bold text-slate-800">System Portal Management</h2>
          <p className="text-xs text-slate-500">FastAPI backend &amp; Danphe/Bazar architecture</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {/* Backend API Ping Status Badge */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full border bg-slate-50 text-xs">
          <span className="flex h-2 w-2 relative">
            {apiOnline === true && (
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            )}
            <span
              className={`relative inline-flex rounded-full h-2 w-2 ${
                apiOnline === true
                  ? 'bg-emerald-500'
                  : apiOnline === false
                  ? 'bg-rose-500'
                  : 'bg-amber-400'
              }`}
            ></span>
          </span>
          <span className="font-medium text-slate-700">
            {apiOnline === true
              ? 'Backend Online'
              : apiOnline === false
              ? 'Backend Disconnected'
              : 'Checking API...'}
          </span>
          <button
            onClick={checkHealth}
            disabled={checking}
            className="text-slate-400 hover:text-slate-600 transition-colors ml-1"
            title="Recheck System Health"
          >
            <RefreshCw className={`w-3 h-3 ${checking ? 'animate-spin' : ''}`} />
          </button>
        </div>

        {/* Admin User Badge */}
        <div className="flex items-center gap-3 pl-2 border-l border-slate-200">
          <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-xs ring-2 ring-emerald-500/20">
            AD
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-bold text-slate-800 leading-none">System Admin</p>
            <p className="text-[11px] text-slate-500 leading-none mt-1">admin@rivo.internal</p>
          </div>
        </div>
      </div>
    </header>
  );
};
