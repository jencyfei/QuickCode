import RegisterForm from '@/components/Auth/RegisterForm';

const RegisterPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="glass-card p-8 w-full max-w-md">
        {/* 标题 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-center mb-2">创建账户</h1>
          <p className="text-center text-text-secondary">加入 DataViz Insights</p>
        </div>

        {/* 注册表单 */}
        <RegisterForm />

        {/* 分割线 */}
        <div className="my-6 flex items-center gap-4">
          <div className="flex-1 h-px bg-gray-300"></div>
          <span className="text-text-tertiary text-sm">或</span>
          <div className="flex-1 h-px bg-gray-300"></div>
        </div>

        {/* 社交注册 */}
        <div className="space-y-2">
          <button className="w-full py-2 px-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition text-sm">
            使用 Google 注册
          </button>
          <button className="w-full py-2 px-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition text-sm">
            使用 GitHub 注册
          </button>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
