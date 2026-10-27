import React, { useState, useEffect } from 'react';
import { pingSystem, pingClient } from '../../api/ping';
import { apiClient } from '../../api/client';
import { Button } from '../../components/ui/Button';
import { CheckCircle2, XCircle, RefreshCw, Activity, Terminal } from 'lucide-react';
import type { ResponseTemplate } from '../../types/api';

export const SystemHealth: React.FC = () => {
  const [systemPing, setSystemPing] = useState<string | null>(null);
  const [clientPing, setClientPing] = useState<string | null>(null);
  const [healthStatus, setHealthStatus] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [lastCheck, setLastCheck] = useState<string>('');

  const runDiagnostics = async () => {
    setLoading(true);
    try {
      try {
        const sys = await pingSystem();
        setSystemPing(sys);
      } catch (e: any) {
        setSystemPing('Error: ' + e.message);
      }

      try {
        const cli = await pingClient();
        setClientPing(cli);
      } catch (e: any) {
        setClientPing('Error: ' + e.message);
      }

      try {
        const res = await apiClient.get<ResponseTemplate<any>>('/health');
        setHealthStatus(res.data.data);
      } catch (e: any) {
        setHealthStatus({ error: e.message });
      }

      setLastCheck(new Date().toLocaleTimeString());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runDiagnostics();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">API Health &amp; Gateway Diagnostics</h1>
          <p className="text-sm text-slate-500 mt-1">
            Real-time ping probes and orchestrator metrics from FRONTEND_API.md contract.
          </p>
        </div>

        <Button
          variant="outline"
          onClick={runDiagnostics}
          disabled={loading}
          leftIcon={<RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />}
        >
          Run Diagnostic Probe
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              System Portal Ping
            </span>
            {systemPing === 'pong' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-500" />
            ) : (
              <XCircle className="w-5 h-5 text-rose-500" />
            )}
          </div>
          <p className="font-mono text-sm font-semibold text-slate-800">
            GET /api/system/ping
          </p>
          <div className="p-3 bg-slate-50 rounded-xl font-mono text-xs text-slate-700">
            Response: {systemPing || 'Waiting...'}
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              Client Portal Ping
            </span>
            {clientPing === 'pong' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-500" />
            ) : (
              <XCircle className="w-5 h-5 text-rose-500" />
            )}
          </div>
          <p className="font-mono text-sm font-semibold text-slate-800">
            GET /api/client/ping
          </p>
          <div className="p-3 bg-slate-50 rounded-xl font-mono text-xs text-slate-700">
            Response: {clientPing || 'Waiting...'}
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              Orchestrator Status
            </span>
            <Activity className="w-5 h-5 text-emerald-500" />
          </div>
          <p className="font-mono text-sm font-semibold text-slate-800">
            GET /health
          </p>
          <div className="p-3 bg-slate-50 rounded-xl font-mono text-xs text-slate-700">
            Status: {healthStatus?.status || (healthStatus?.error ? 'Unavailable' : 'Waiting...')}
          </div>
        </div>
      </div>

      {healthStatus && (
        <div className="p-6 rounded-2xl bg-slate-900 text-slate-100 shadow-md">
          <div className="flex items-center gap-2 mb-3 text-xs font-mono text-emerald-400">
            <Terminal className="w-4 h-4" />
            <span>Raw Probe Response &amp; Gateway Context</span>
          </div>
          <pre className="font-mono text-xs bg-slate-950/70 p-4 rounded-xl overflow-x-auto text-emerald-300">
            {JSON.stringify(
              {
                systemPing,
                clientPing,
                orchestratorHealth: healthStatus,
                lastCheckedAt: lastCheck,
              },
              null,
              2
            )}
          </pre>
        </div>
      )}
    </div>
  );
};
