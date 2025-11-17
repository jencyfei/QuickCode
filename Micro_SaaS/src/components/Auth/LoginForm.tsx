import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '@/components/Common/Button';
import Input from '@/components/Common/Input';
import { authService } from '@/services/auth';

const LoginForm = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  // 验证表单
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.email) {
      newErrors.email = '邮箱不能为空';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '邮箱格式不正确';
    }

    if (!formData.password) {
      newErrors.password = '密码不能为空';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少6个字符';
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

  // 处理登录
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError(null);

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      await authService.login({
        email: formData.email,
        password: formData.password,
      });

      // 登录成功，跳转到Dashboard
      navigate('/dashboard');
    } catch (error) {
      const message = error instanceof Error ? error.message : '登录失败，请重试';
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
        placeholder="请输入密码"
        value={formData.password}
        onChange={handleInputChange}
        error={errors.password}
        disabled={isLoading}
      />

      {/* 记住密码和忘记密码 */}
      <div className="flex items-center justify-between text-sm">
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            className="w-4 h-4 rounded border-gray-300"
            disabled={isLoading}
          />
          <span className="text-text-secondary">记住我</span>
        </label>
        <a href="#" className="text-primary hover:opacity-80">
          忘记密码？
        </a>
      </div>

      {/* 登录按钮 */}
      <Button
        type="submit"
        fullWidth
        isLoading={isLoading}
        disabled={isLoading}
      >
        {isLoading ? '登录中...' : '登录'}
      </Button>

      {/* 注册链接 */}
      <p className="text-center text-sm text-text-secondary">
        还没有账号？{' '}
        <a href="/register" className="text-primary hover:opacity-80">
          立即注册
        </a>
      </p>
    </form>
  );
};

export default LoginForm;
