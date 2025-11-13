/**
 * 地址提取功能测试
 * 用于验证 extractAddress 函数的各种场景
 */

// 从 ExpressDetail.vue 提取的 extractAddress 函数
const extractAddress = (content, sender) => {
  // 1. 先清理内容，移除常见的无关前缀和后缀
  let cleanContent = content
    // 移除【发件人】格式
    .replace(/^.*?[【\[].*?[】\]]\s*/g, '')
    // 移除常见前缀
    .replace(/速递[】\]]\s*/g, '')
    .replace(/您的快递已到达/g, '')
    .replace(/您的快递已存入/g, '')
    .replace(/已到达/g, '')
    .replace(/快递已到达/g, '')
    .replace(/快递到达/g, '')
    .replace(/包裹到达/g, '')
    // 移除常见后缀
    .replace(/请.*?取件.*$/g, '')
    .replace(/凭.*?取件.*$/g, '')
    .replace(/，.*$/g, '')
    .replace(/。.*$/g, '')
  
  // 2. 地址提取模式（按优先级排序）
  const addressPatterns = [
    // 具体地址 + 驿站/快递柜/门卫等
    /([^\s，。！？]{2,20}?(?:驿站|快递柜|门卫|保安室|代收点|自提点|丰巢|菜鸟)(?:[^\s，。！？]{0,10})?)/,
    // 路名 + 驿站/快递柜
    /([^\s，。！？]{2,15}?[路街道巷][^\s，。！？]{0,15}?(?:驿站|快递柜|门卫))/,
    // 小区/大厦 + 具体位置（如门口、大门等）- 允许关键词在开头
    /([^\s，。！？]{0,15}?(?:小区|大厦|广场|商场|公寓|写字楼)(?:[^\s，。！？]{0,15}?(?:门口|大门|北门|南门|东门|西门|正门|侧门|1号门|2号门|3号门|快递柜|驿站|门卫))?)/,
    // 纯驿站名称
    /(菜鸟驿站[^\s，。！？]{0,15})/,
    /(丰巢[^\s，。！？]{0,15})/,
  ]
  
  for (const pattern of addressPatterns) {
    const match = cleanContent.match(pattern)
    if (match) {
      let address = match[1].trim()
      
      // 3. 进一步清理提取结果
      address = address
        // 移除开头的标点和空格
        .replace(/^[，。！？\s、]+/, '')
        // 移除结尾的标点和空格
        .replace(/[，。！？\s、]+$/, '')
        // 移除"请"、"取"等动词
        .replace(/请.*?取/, '')
        .replace(/到$/, '')
        .replace(/在$/, '')
        .replace(/处$/, '')
        // 移除开头的"快递"、"您的快递已"等
        .replace(/^快递/, '')
        .replace(/^您的快递已/, '')
      
      // 4. 验证地址长度和有效性
      if (address.length >= 2 && address.length <= 30) {
        return address
      }
    }
  }
  
  // 5. 如果没有匹配到，尝试从发件人提取
  if (sender) {
    // 清理发件人
    const cleanSender = sender
      .replace(/^.*?[【\[]/, '')
      .replace(/[】\]].*$/, '')
      .trim()
    
    if (cleanSender.includes('驿站') || cleanSender.includes('快递') || 
        cleanSender.includes('丰巢') || cleanSender.includes('菜鸟')) {
      return cleanSender
    }
  }
  
  return '未知地址'
}

// 测试用例
const testCases = [
  {
    name: '问题1: 速递前缀 + 小区快递柜',
    content: '【速递】您的快递已到达小区快递柜',
    sender: '顺丰速递',
    expected: '小区快递柜'
  },
  {
    name: '问题2: 小区门口',
    content: '您的快递已到达小区门口',
    sender: '菜鸟驿站',
    expected: '小区门口'
  },
  {
    name: '测试3: 菜鸟驿站带店名',
    content: '【菜鸟驿站】您的快递已到达XX路店，请凭取件码取件',
    sender: '菜鸟驿站',
    expected: '菜鸟驿站'
  },
  {
    name: '测试4: 具体小区名 + 门口',
    content: '您的快递已到达阳光小区北门，取件码: 1234',
    sender: '中通快递',
    expected: '阳光小区北门'
  },
  {
    name: '测试5: 丰巢快递柜',
    content: '【丰巢】您的快递已存入丰巢智能柜',
    sender: '丰巢',
    expected: '丰巢智能柜'  // 更准确的地址
  },
  {
    name: '测试6: 路名 + 驿站',
    content: '快递到达人民路菜鸟驿站，请及时取件',
    sender: '菜鸟',
    expected: '人民路菜鸟驿站'
  },
  {
    name: '测试7: 小区 + 快递柜',
    content: '包裹到达绿城小区快递柜',
    sender: '申通',
    expected: '绿城小区快递柜'
  },
  {
    name: '测试8: 大厦 + 门卫',
    content: '快递已到达科技大厦门卫处',
    sender: '韵达',
    expected: '科技大厦门卫'
  },
  {
    name: '测试9: 从发件人提取',
    content: '您的快递已到，取件码: ABC123',
    sender: '【菜鸟驿站(XX店)】',
    expected: '菜鸟驿站(XX店)'
  },
  {
    name: '测试10: 商场 + 驿站',
    content: '快递到达万达广场菜鸟驿站',
    sender: '菜鸟',
    expected: '万达广场菜鸟驿站'
  }
]

// 运行测试
console.log('=== 地址提取测试 ===\n')

let passCount = 0
let failCount = 0

testCases.forEach((testCase, index) => {
  const result = extractAddress(testCase.content, testCase.sender)
  const passed = result === testCase.expected
  
  if (passed) {
    passCount++
    console.log(`✅ ${testCase.name}`)
  } else {
    failCount++
    console.log(`❌ ${testCase.name}`)
    console.log(`   输入: ${testCase.content}`)
    console.log(`   期望: ${testCase.expected}`)
    console.log(`   实际: ${result}`)
  }
  console.log('')
})

console.log('=== 测试结果 ===')
console.log(`通过: ${passCount}/${testCases.length}`)
console.log(`失败: ${failCount}/${testCases.length}`)
console.log(`成功率: ${(passCount / testCases.length * 100).toFixed(1)}%`)

// 导出函数供其他模块使用
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { extractAddress }
}
