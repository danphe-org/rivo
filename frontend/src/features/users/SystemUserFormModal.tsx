import React, { useState, useEffect } from 'react';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../context/ToastContext';
import { systemUserApi } from '../../api/systemUsers';
import type { UserResponseDTO, UserCreateDTO, UserUpdateDTO } from '../../types/user';
import { Eye, EyeOff, Mail, User, Phone, Check } from 'lucide-react';

interface SystemUserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  userToEdit?: UserResponseDTO | null;
}

export const SystemUserFormModal: React.FC<SystemUserFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  userToEdit,
}) => {
  const isEditing = Boolean(userToEdit);
  const { showSuccess, showError } = useToast();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('ADMIN');
  const [active, setActive] = useState(true);

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (userToEdit) {
      setName(userToEdit.name || '');
      setEmail(userToEdit.email || '');
      setPhone(userToEdit.phone || '');
      setRole(userToEdit.role || 'ADMIN');
      setActive(userToEdit.active ?? true);
      setPassword('');
    } else {
      setName('');
      setEmail('');
      setPassword('');
      setPhone('');
      setRole('ADMIN');
      setActive(true);
    }
    setErrors({});
  }, [userToEdit, isOpen]);

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!name.trim()) {
      newErrors.name = 'Full name is required';
    } else if (name.trim().length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    }

    if (!email.trim()) {
      newErrors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      newErrors.email = 'Please provide a valid email format';
    }

    if (!isEditing && !password) {
      newErrors.password = 'Password is required for registration';
    } else if (password) {
      if (password.length < 8) {
        newErrors.password = 'Password must be at least 8 characters';
      } else if (!/[A-Z]/.test(password)) {
        newErrors.password = 'Must contain at least 1 uppercase letter';
      } else if (!/[a-z]/.test(password)) {
        newErrors.password = 'Must contain at least 1 lowercase letter';
      } else if (!/[0-9]/.test(password)) {
        newErrors.password = 'Must contain at least 1 number';
      } else if (!/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password)) {
        newErrors.password = 'Must contain at least 1 special character';
      }
    }

    if (phone && !/^\+?[0-9]{7,20}$/.test(phone.trim())) {
      newErrors.phone = 'Phone must be between 7 and 20 digits';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      if (isEditing && userToEdit) {
        const updatePayload: UserUpdateDTO = {
          name: name.trim(),
          email: email.trim(),
          phone: phone.trim() || undefined,
          role,
          active,
        };
        if (password) {
          updatePayload.password = password;
        }
        await systemUserApi.updateUser(userToEdit.id, updatePayload);
        showSuccess(`User "${name}" updated successfully`);
      } else {
        const createPayload: UserCreateDTO = {
          name: name.trim(),
          email: email.trim(),
          password,
          phone: phone.trim() || undefined,
          role,
        };
        await systemUserApi.createUser(createPayload);
        showSuccess(`System user "${name}" registered successfully`);
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      showError(err.message || 'Operation failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Edit System User' : 'Register New System User'}
      description={
        isEditing
          ? 'Update profile details, authority role, or access status.'
          : 'Provision a new administrative or backoffice user with immediate access.'
      }
      maxWidth="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Full Name"
          required
          placeholder="e.g. John Doe"
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={errors.name}
          leftIcon={<User className="w-4 h-4" />}
        />

        <Input
          label="Email Address"
          type="email"
          required
          placeholder="name@company.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
          leftIcon={<Mail className="w-4 h-4" />}
        />

        <div className="relative">
          <Input
            label={isEditing ? 'New Password (Optional)' : 'Password'}
            type={showPassword ? 'text' : 'password'}
            required={!isEditing}
            placeholder={isEditing ? 'Leave blank to keep current' : '••••••••••••'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
            helperText={
              !isEditing
                ? 'Min 8 chars, 1 uppercase, 1 lowercase, 1 number & 1 special character.'
                : undefined
            }
            rightIcon={
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-slate-400 hover:text-slate-600 focus:outline-none"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            }
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Phone Number (Optional)"
            placeholder="+1234567890"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            error={errors.phone}
            leftIcon={<Phone className="w-4 h-4" />}
          />

          <Select
            label="System Role"
            value={role}
            onChange={(e) => setRole(e.target.value)}
            options={[
              { value: 'ADMIN', label: 'Admin (System Operator)' },
              { value: 'STAFF', label: 'Staff' },
              { value: 'MANAGER', label: 'Manager' },
              { value: 'USER', label: 'User (Standard)' },
            ]}
          />
        </div>

        {isEditing && (
          <div className="pt-2">
            <label className="flex items-center gap-3 p-3 rounded-xl border border-slate-200 bg-slate-50/50 cursor-pointer hover:bg-slate-50 transition-colors">
              <input
                type="checkbox"
                checked={active}
                onChange={(e) => setActive(e.target.checked)}
                className="w-4 h-4 text-emerald-600 rounded border-slate-300 focus:ring-emerald-500"
              />
              <div>
                <p className="text-xs font-semibold text-slate-800">Account Active</p>
                <p className="text-[11px] text-slate-500">
                  Allow this user to authenticate and access authorized portals.
                </p>
              </div>
            </label>
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isSubmitting}
            leftIcon={<Check className="w-4 h-4" />}
          >
            {isEditing ? 'Save Changes' : 'Register User'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
