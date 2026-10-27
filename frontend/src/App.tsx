import React, { useState, useEffect } from 'react';
import { ToastProvider, useToast } from './context/ToastContext';
import { Navbar } from './components/layout/Navbar';
import { SystemUserFormModal } from './features/users/SystemUserFormModal';
import { LoginModal } from './features/auth/LoginModal';
import { SystemUserList } from './features/users/SystemUserList';
import { authApi } from './api/auth';
import type { UserResponseDTO } from './types/user';

export const MainContent: React.FC = () => {
  const { showSuccess } = useToast();
  const [currentUser, setCurrentUser] = useState<UserResponseDTO | null>(null);
  const [currentView, setCurrentView] = useState<'home' | 'users'>('home');
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);
  const [isLoginOpen, setIsLoginOpen] = useState(false);

  useEffect(() => {
    authApi
      .getMe()
      .then((user) => {
        if (user && user.id) {
          setCurrentUser(user);
        }
      })
      .catch(() => {});
  }, []);

  const handleLogout = async () => {
    try {
      await authApi.logoutSystem();
    } catch {
      // Ignore network errors on logout
    }
    setCurrentUser(null);
    setCurrentView('home');
    showSuccess('Logged out successfully');
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar
        currentUser={currentUser}
        currentView={currentView}
        onNavigate={setCurrentView}
        onOpenLogin={() => setIsLoginOpen(true)}
        onOpenRegister={() => setIsRegisterOpen(true)}
        onLogout={handleLogout}
      />

      <main className="flex-1">
        {currentView === 'home' ? (
          <div className="w-full h-full min-h-[calc(100vh-5rem)] flex items-center justify-center" />
        ) : (
          <div className="max-w-7xl mx-auto p-6 sm:p-10">
            <SystemUserList />
          </div>
        )}
      </main>

      <SystemUserFormModal
        isOpen={isRegisterOpen}
        onClose={() => setIsRegisterOpen(false)}
        onSuccess={() => {
          setIsRegisterOpen(false);
          setIsLoginOpen(true);
        }}
      />

      <LoginModal
        isOpen={isLoginOpen}
        onClose={() => setIsLoginOpen(false)}
        onSuccess={(user) => {
          setCurrentUser(user);
          setCurrentView('users');
        }}
        onSwitchToRegister={() => {
          setIsLoginOpen(false);
          setIsRegisterOpen(true);
        }}
      />
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <ToastProvider>
      <MainContent />
    </ToastProvider>
  );
};

export default App;
