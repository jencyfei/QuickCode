import { ReactNode } from 'react';

interface StatCardProps {
  icon: ReactNode;
  label: string;
  value: string | number;
  change?: {
    value: number;
    isPositive: boolean;
  };
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger';
}

const StatCard = ({
  icon,
  label,
  value,
  change,
  color = 'primary',
}: StatCardProps) => {
  const colorClasses = {
    primary: 'from-primary/20 to-primary/10 border-primary/30',
    secondary: 'from-secondary/20 to-secondary/10 border-secondary/30',
    success: 'from-green-500/20 to-green-500/10 border-green-500/30',
    warning: 'from-yellow-500/20 to-yellow-500/10 border-yellow-500/30',
    danger: 'from-red-500/20 to-red-500/10 border-red-500/30',
  };

  const changeColorClass = change?.isPositive
    ? 'text-green-600'
    : 'text-red-600';

  return (
    <div
      className={`glass-card p-6 border border-white/20 bg-gradient-to-br ${colorClasses[color]} hover:border-white/40 transition`}
    >
      {/* 图标 */}
      <div className="flex items-center justify-between mb-4">
        <div className="w-12 h-12 rounded-lg bg-white/10 flex items-center justify-center text-2xl">
          {icon}
        </div>
        {change && (
          <div className={`text-sm font-semibold ${changeColorClass}`}>
            {change.isPositive ? '↑' : '↓'} {Math.abs(change.value)}%
          </div>
        )}
      </div>

      {/* 标签 */}
      <p className="text-text-secondary text-sm mb-2">{label}</p>

      {/* 数值 */}
      <p className="text-3xl font-bold text-text-primary">{value}</p>

      {/* 底部信息 */}
      <p className="text-xs text-text-tertiary mt-4">
        {change && (
          <>
            {change.isPositive ? '增加' : '减少'} {Math.abs(change.value)}%
            {' '}相比上月
          </>
        )}
      </p>
    </div>
  );
};

export default StatCard;
