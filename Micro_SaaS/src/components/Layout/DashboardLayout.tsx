import { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

interface DashboardLayoutProps {
  children: ReactNode;
  title?: string;
}

const DashboardLayout = ({ children, title }: DashboardLayoutProps) => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-bg-primary via-bg-secondary to-bg-tertiary">
      {/* 头部导航栏 */}
      <header className="glass-card border-b border-white/10 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            {/* Logo和标题 */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
                <span className="text-white font-bold text-lg">D</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-text-primary">DataViz Insights</h1>
                <p className="text-xs text-text-secondary">数据可视化平台</p>
              </div>
            </div>

            {/* 用户菜单 */}
            <div className="flex items-center gap-4">
              <div className="text-right">
                <p className="text-sm font-medium text-text-primary">{user?.name || '用户'}</p>
                <p className="text-xs text-text-secondary">{user?.email}</p>
              </div>
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm font-medium text-text-primary hover:bg-white/10 rounded-lg transition"
              >
                退出
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* 侧边栏和主内容 */}
      <div className="flex">
        {/* 侧边栏 */}
        <aside className="w-64 glass-card border-r border-white/10 min-h-[calc(100vh-80px)] p-6">
          <nav className="space-y-2">
            {/* 导航项 */}
            <NavItem
              icon="📊"
              label="Dashboard"
              href="/dashboard"
              active={true}
            />
            <NavItem
              icon="📋"
              label="模板"
              href="/templates"
            />
            <NavItem
              icon="📤"
              label="上传数据"
              href="/upload"
            />
            <NavItem
              icon="📈"
              label="分析结果"
              href="/results"
            />

            {/* 分割线 */}
            <div className="my-4 h-px bg-white/10"></div>

            {/* 设置项 */}
            <NavItem
              icon="⚙️"
              label="设置"
              href="/settings"
            />
            <NavItem
              icon="❓"
              label="帮助"
              href="/help"
            />
          </nav>
        </aside>

        {/* 主内容区域 */}
        <main className="flex-1 p-8">
          {/* 页面标题 */}
          {title && (
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-text-primary">{title}</h2>
              <p className="text-text-secondary mt-2">欢迎使用 DataViz Insights</p>
            </div>
          )}

          {/* 页面内容 */}
          {children}
        </main>
      </div>
    </div>
  );
};

// 导航项组件
interface NavItemProps {
  icon: string;
  label: string;
  href: string;
  active?: boolean;
}

const NavItem = ({ icon, label, href, active = false }: NavItemProps) => {
  return (
    <a
      href={href}
      className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
        active
          ? 'bg-primary/20 text-primary font-medium'
          : 'text-text-secondary hover:bg-white/5'
      }`}
    >
      <span className="text-xl">{icon}</span>
      <span>{label}</span>
    </a>
  );
};

export default DashboardLayout;
