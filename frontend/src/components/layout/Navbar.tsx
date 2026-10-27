import React from 'react';
import { Button } from '../ui/Button';
import { LogIn, UserPlus, LogOut, Users } from 'lucide-react';
import type { UserResponseDTO } from '../../types/user';

interface NavbarProps {
  onOpenLogin: () => void;
  onOpenRegister: () => void;
  currentUser: UserResponseDTO | null;
  onLogout: () => void;
  currentView: 'home' | 'users';
  onNavigate: (view: 'home' | 'users') => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  onOpenLogin,
  onOpenRegister,
  currentUser,
  onLogout,
  currentView,
  onNavigate,
}) => {
  return (
    <header className="h-20 bg-white/80 backdrop-blur-md border-b border-slate-200/60 sticky top-0 z-40 px-6 sm:px-12 flex items-center justify-between">
      {/* Brand */}
      <div
        className="flex items-center gap-3 cursor-pointer group"
        onClick={() => onNavigate('home')}
      >
        <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white font-black text-xl shadow-lg shadow-emerald-600/20 group-hover:scale-105 transition-transform">
          R
        </div>
        <span className="font-extrabold text-xl text-slate-900 tracking-tight">
          RIVO
        </span>
      </div>

      {/* Navigation Actions */}
      <nav className="flex items-center gap-3">
        {!currentUser ? (
          <>
            <Button
              variant="outline"
              size="md"
              onClick={onOpenLogin}
              leftIcon={<LogIn className="w-4 h-4" />}
            >
              Login
            </Button>
            <Button
              variant="primary"
              size="md"
              onClick={onOpenRegister}
              leftIcon={<UserPlus className="w-4 h-4" />}
            >
              Register
            </Button>
          </>
        ) : (
          <div className="flex items-center gap-3">
            <Button
              variant={currentView === 'users' ? 'primary' : 'outline'}
              size="md"
              onClick={() => onNavigate(currentView === 'users' ? 'home' : 'users')}
              leftIcon={<Users className="w-4 h-4" />}
            >
              {currentView === 'users' ? 'Home' : 'System Users'}
            </Button>

            <div className="flex items-center gap-2 pl-2 border-l border-slate-200 text-xs">
              <div className="w-8 h-8 rounded-xl bg-slate-900 text-white font-bold flex items-center justify-center">
                {currentUser.name.charAt(0).toUpperCase()}
              </div>
              <div className="hidden sm:block text-left">
                <p className="font-bold text-slate-800 leading-tight">{currentUser.name}</p>
                <p className="text-[10px] text-slate-400 leading-tight">{currentUser.role}</p>
              </div>
            </div>

            <Button
              variant="ghost"
              size="sm"
              onClick={onLogout}
              className="text-slate-500 hover:text-rose-600"
              title="Logout"
            >
              <LogOut className="w-4 h-4" />
            </Button>
          </div>
        )}
      </nav>
    </header>
  );
};
