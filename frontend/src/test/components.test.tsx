import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Pagination } from '../components/ui/Pagination';
import { Navbar } from '../components/layout/Navbar';
import { SystemUserStats } from '../features/users/SystemUserStats';
import type { UserResponseDTO } from '../types/user';

describe('UI Component Library', () => {
  it('renders Navbar with only Login and Register buttons for guest users', () => {
    const handleLogin = vi.fn();
    const handleRegister = vi.fn();
    render(
      <Navbar
        currentUser={null}
        currentView="home"
        onNavigate={vi.fn()}
        onOpenLogin={handleLogin}
        onOpenRegister={handleRegister}
        onLogout={vi.fn()}
      />
    );

    const loginBtn = screen.getByRole('button', { name: /Login/i });
    const registerBtn = screen.getByRole('button', { name: /Register/i });
    expect(loginBtn).toBeInTheDocument();
    expect(registerBtn).toBeInTheDocument();

    fireEvent.click(loginBtn);
    expect(handleLogin).toHaveBeenCalledTimes(1);

    fireEvent.click(registerBtn);
    expect(handleRegister).toHaveBeenCalledTimes(1);
  });
  it('renders Button with variants and handles click events', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick} variant="primary">Register User</Button>);
    const button = screen.getByRole('button', { name: /Register User/i });
    expect(button).toBeInTheDocument();
    fireEvent.click(button);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('renders Badge with specific role styling', () => {
    render(<Badge role="ADMIN">ADMIN</Badge>);
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('renders Input with error message', () => {
    render(<Input label="Full Name" error="Name is required" placeholder="John Doe" />);
    expect(screen.getByText('Full Name')).toBeInTheDocument();
    expect(screen.getByText('Name is required')).toBeInTheDocument();
  });

  it('renders Pagination and triggers page navigation', () => {
    const handlePageChange = vi.fn();
    render(
      <Pagination
        currentPage={1}
        totalRecords={25}
        pageSize={10}
        onPageChange={handlePageChange}
      />
    );
    expect(screen.getByText(/Page 1 of 3/i)).toBeInTheDocument();
    const nextBtn = screen.getByRole('button', { name: /Next/i });
    fireEvent.click(nextBtn);
    expect(handlePageChange).toHaveBeenCalledWith(2);
  });

  it('renders SystemUserStats with at most 5 sample user records', () => {
    const sampleUsers: UserResponseDTO[] = [
      {
        id: '1',
        name: 'Jane Admin',
        email: 'jane@example.com',
        role: 'ADMIN',
        active: true,
        deleted: false,
      },
      {
        id: '2',
        name: 'Bob User',
        email: 'bob@example.com',
        role: 'USER',
        active: true,
        deleted: false,
      },
      {
        id: '3',
        name: 'Alice Inactive',
        email: 'alice@example.com',
        role: 'USER',
        active: false,
        deleted: false,
      },
    ];

    render(<SystemUserStats users={sampleUsers} totalRecords={sampleUsers.length} />);
    expect(screen.getByText('Total System Users')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('Active Accounts')).toBeInTheDocument();
    expect(screen.getByText('Admin Authorities')).toBeInTheDocument();
  });
});
