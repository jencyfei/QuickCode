import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/Layout/DashboardLayout';
import StatCard from '@/components/Dashboard/StatCard';
import ChartCard from '@/components/Dashboard/ChartCard';

const DashboardPage = () => {
  const [stats, setStats] = useState({
    totalAnalyses: 0,
    successRate: 0,
    avgProcessTime: 0,
    activeUsers: 0,
  });

  // 模拟数据加载
  useEffect(() => {
    // 这里将来会调用真实API
    setStats({
      totalAnalyses: 1234,
      successRate: 98.5,
      avgProcessTime: 2.3,
      activeUsers: 567,
    });
  }, []);

  return (
    <DashboardLayout title="Dashboard">
      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          icon="📊"
          label="总分析数"
          value={stats.totalAnalyses}
          change={{ value: 12, isPositive: true }}
          color="primary"
        />
        <StatCard
          icon="✅"
          label="成功率"
          value={`${stats.successRate}%`}
          change={{ value: 2.3, isPositive: true }}
          color="success"
        />
        <StatCard
          icon="⏱️"
          label="平均处理时间"
          value={`${stats.avgProcessTime}s`}
          change={{ value: 5, isPositive: false }}
          color="warning"
        />
        <StatCard
          icon="👥"
          label="活跃用户"
          value={stats.activeUsers}
          change={{ value: 8, isPositive: true }}
          color="secondary"
        />
      </div>

      {/* 图表区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* 分析趋势 */}
        <ChartCard
          title="分析趋势"
          description="过去30天的分析数量"
          action={{
            label: '查看详情',
            onClick: () => console.log('查看详情'),
          }}
        >
          <div className="w-full h-full flex items-center justify-center text-text-secondary">
            <p>图表组件 (待集成 Recharts)</p>
          </div>
        </ChartCard>

        {/* 数据来源分布 */}
        <ChartCard
          title="数据来源分布"
          description="各类型数据的占比"
          action={{
            label: '导出',
            onClick: () => console.log('导出'),
          }}
        >
          <div className="w-full h-full flex items-center justify-center text-text-secondary">
            <p>图表组件 (待集成 Recharts)</p>
          </div>
        </ChartCard>
      </div>

      {/* 最近分析 */}
      <ChartCard
        title="最近分析"
        description="您最近的分析记录"
      >
        <div className="w-full">
          <div className="space-y-3">
            {[1, 2, 3].map((item) => (
              <div
                key={item}
                className="flex items-center justify-between p-4 bg-white/5 rounded-lg hover:bg-white/10 transition"
              >
                <div>
                  <p className="font-medium text-text-primary">分析 #{item}</p>
                  <p className="text-sm text-text-secondary">
                    2025年11月{17 - item}日
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium text-green-600">✓ 成功</p>
                  <p className="text-xs text-text-tertiary">2.5s</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </ChartCard>
    </DashboardLayout>
  );
};

export default DashboardPage;
