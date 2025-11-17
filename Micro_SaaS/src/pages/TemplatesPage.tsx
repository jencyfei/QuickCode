import DashboardLayout from '@/components/Layout/DashboardLayout';
import Button from '@/components/Common/Button';

const TemplatesPage = () => {
  const templates = [
    {
      id: 1,
      name: '销售数据分析',
      description: '分析销售数据，生成销售趋势图表',
      icon: '📊',
      tags: ['销售', '趋势', '对比'],
    },
    {
      id: 2,
      name: '用户行为分析',
      description: '分析用户行为数据，生成用户画像',
      icon: '👥',
      tags: ['用户', '行为', '分析'],
    },
    {
      id: 3,
      name: '财务报表',
      description: '生成财务报表和财务分析图表',
      icon: '💰',
      tags: ['财务', '报表', '分析'],
    },
    {
      id: 4,
      name: '库存管理',
      description: '分析库存数据，优化库存管理',
      icon: '📦',
      tags: ['库存', '管理', '优化'],
    },
    {
      id: 5,
      name: '客户满意度',
      description: '分析客户反馈，评估满意度',
      icon: '⭐',
      tags: ['客户', '满意度', '反馈'],
    },
    {
      id: 6,
      name: '自定义模板',
      description: '创建自己的数据分析模板',
      icon: '✨',
      tags: ['自定义', '灵活', '强大'],
    },
  ];

  return (
    <DashboardLayout title="模板选择">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {templates.map((template) => (
          <div
            key={template.id}
            className="glass-card p-6 border border-white/20 hover:border-white/40 transition cursor-pointer group"
          >
            {/* 图标 */}
            <div className="text-5xl mb-4 group-hover:scale-110 transition">{template.icon}</div>

            {/* 标题 */}
            <h3 className="text-lg font-semibold text-text-primary mb-2">{template.name}</h3>

            {/* 描述 */}
            <p className="text-sm text-text-secondary mb-4">{template.description}</p>

            {/* 标签 */}
            <div className="flex flex-wrap gap-2 mb-6">
              {template.tags.map((tag) => (
                <span
                  key={tag}
                  className="px-2 py-1 text-xs bg-primary/20 text-primary rounded-full"
                >
                  {tag}
                </span>
              ))}
            </div>

            {/* 按钮 */}
            <Button
              variant="primary"
              size="sm"
              fullWidth
              onClick={() => console.log(`选择模板: ${template.name}`)}
            >
              使用此模板
            </Button>
          </div>
        ))}
      </div>
    </DashboardLayout>
  );
};

export default TemplatesPage;
