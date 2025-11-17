import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center p-8">
      <div className="glass-card p-12 text-center max-w-md">
        <h1 className="text-6xl font-bold mb-4">404</h1>
        <p className="text-2xl font-semibold mb-4">页面未找到</p>
        <p className="text-text-secondary mb-8">抱歉，您访问的页面不存在。</p>
        <Link
          to="/dashboard"
          className="inline-block px-6 py-3 bg-primary text-white rounded-lg hover:opacity-90 transition"
        >
          返回首页
        </Link>
      </div>
    </div>
  );
};

export default NotFoundPage;
