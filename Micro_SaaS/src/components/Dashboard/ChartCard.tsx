import { ReactNode } from 'react';

interface ChartCardProps {
  title: string;
  description?: string;
  children: ReactNode;
  action?: {
    label: string;
    onClick: () => void;
  };
}

const ChartCard = ({
  title,
  description,
  children,
  action,
}: ChartCardProps) => {
  return (
    <div className="glass-card p-6 border border-white/20 hover:border-white/40 transition">
      {/* 标题栏 */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-lg font-semibold text-text-primary">{title}</h3>
          {description && (
            <p className="text-sm text-text-secondary mt-1">{description}</p>
          )}
        </div>
        {action && (
          <button
            onClick={action.onClick}
            className="px-3 py-1 text-sm text-primary hover:bg-primary/10 rounded-lg transition"
          >
            {action.label}
          </button>
        )}
      </div>

      {/* 图表内容 */}
      <div className="w-full h-80 flex items-center justify-center">
        {children}
      </div>

      {/* 底部信息 */}
      <div className="mt-6 pt-6 border-t border-white/10">
        <p className="text-xs text-text-tertiary">
          最后更新: {new Date().toLocaleString('zh-CN')}
        </p>
      </div>
    </div>
  );
};

export default ChartCard;
