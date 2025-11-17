import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '@/components/Common/Button';
import Input from '@/components/Common/Input';
import { authService } from '@/services/auth';

const RegisterForm = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  // 验证表单
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name) {
      newErrors.name = '名字不能为空';
    } else if (formData.name.length < 2) {
      newErrors.name = '名字至少2个字符';
    }

    if (!formData.email) {
      newErrors.email = '邮箱不能为空';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '邮箱格式不正确';
    }

    if (!formData.password) {
      newErrors.password = '密码不能为空';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少6个字符';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.password)) {
      newErrors.password = '密码需包含大小写字母和数字';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '确认密码不能为空';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次密码输入不一致';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
    // 清除该字段的错误
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  // 处理注册
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError(null);

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      await authService.register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      // 注册成功，跳转到Dashboard
      navigate('/dashboard');
    } catch (error) {
      const message = error instanceof Error ? error.message : '注册失败，请重试';
      setApiError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* API错误提示 */}
      {apiError && (
        <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
          {apiError}
        </div>
      )}

      {/* 名字输入 */}
      <Input
        label="名字"
        type="text"
        name="name"
        placeholder="请输入您的名字"
        value={formData.name}
        onChange={handleInputChange}
        error={errors.name}
        disabled={isLoading}
      />

      {/* 邮箱输入 */}
      <Input
        label="邮箱"
        type="email"
        name="email"
        placeholder="请输入邮箱"
        value={formData.email}
        onChange={handleInputChange}
        error={errors.email}
        disabled={isLoading}
      />

      {/* 密码输入 */}
      <Input
        label="密码"
        type="password"
        name="password"
        placeholder="请输入密码 (至少6个字符)"
        value={formData.password}
        onChange={handleInputChange}
        error={errors.password}
        helperText="需包含大小写字母和数字"
        disabled={isLoading}
      />

      {/* 确认密码输入 */}
      <Input
        label="确认密码"
        type="password"
        name="confirmPassword"
        placeholder="请再次输入密码"
        value={formData.confirmPassword}
        onChange={handleInputChange}
        error={errors.confirmPassword}
        disabled={isLoading}
      />

      {/* 注册按钮 */}
      <Button
        type="submit"
        fullWidth
        isLoading={isLoading}
        disabled={isLoading}
      >
        {isLoading ? '注册中...' : '注册'}
      </Button>

      {/* 登录链接 */}
      <p className="text-center text-sm text-text-secondary">
        已有账号？{' '}
        <a href="/login" className="text-primary hover:opacity-80">
          立即登录
        </a>
      </p>
    </form>
  );
};

export default RegisterForm;
