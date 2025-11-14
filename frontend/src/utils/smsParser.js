/**
 * 短信内容解析工具
 * 根据标签类型智能提取关键信息
 */

/**
 * 提取验证码
 * @param {string} content - 短信内容
 * @returns {string|null} - 验证码或null
 */
export function extractVerificationCode(content) {
  // 匹配4-8位数字验证码
  const patterns = [
    /验证码[：:是为]?\s*(\d{4,8})/,
    /code[：:is]?\s*(\d{4,8})/i,
    /(\d{6})/,  // 6位纯数字
    /(\d{4})/   // 4位纯数字
  ]
  
  for (const pattern of patterns) {
    const match = content.match(pattern)
    if (match) {
      return match[1]
    }
  }
  
  return null
}

/**
 * 提取取件码
 * @param {string} content - 短信内容
 * @returns {string|null} - 取件码或null
 */
export function extractPickupCode(content) {
  const patterns = [
    // 优先级1：明确的标签格式
    /取件码[：:是为]?\s*([A-Z0-9-]{4,12})/i,  // 支持横杠，长度增加到12
    /提货码[：:是为]?\s*([A-Z0-9-]{4,12})/i,
    /取货码[：:是为]?\s*([A-Z0-9-]{4,12})/i,
    /取件号[：:是为]?\s*([A-Z0-9-]{4,12})/i,
    /code[：:is]?\s*([A-Z0-9-]{4,12})/i,
    
    // 优先级2："凭"后的8个字符（精确匹配格式如"2-4-2029"）
    /凭\s*(\d+-\d+-\d+)/i,  // 优先匹配"凭X-X-XXXX"格式
    /凭\s*([A-Z0-9-]{3,12})/i,  // 其他"凭"格式
    
    // 优先级3：其他格式
    /到\s*(.{2,15}?)取件/,  // 提取"到...取件"之间的内容作为取件码
    /(\d+-\d+-\d+)/,  // 匹配横杠分隔的数字，如"7-5-3028"
    /(\d{4})/  // 4位纯数字（最后尝试）
  ]
  
  for (const pattern of patterns) {
    const match = content.match(pattern)
    if (match) {
      const code = match[1].trim()
      // 如果是"到...取件"模式，只保留数字、字母和横杠
      if (pattern.source.includes('到') && pattern.source.includes('取件')) {
        const cleaned = code.replace(/[^A-Z0-9-]/gi, '')
        if (cleaned.length >= 3) {
          return cleaned
        }
      }
      return code
    }
  }
  
  return null
}

/**
 * 提取取件日期
 * @param {string} content - 短信内容
 * @returns {string|null} - 取件日期（YYYY-MM-DD格式）或null
 */
export function extractPickupDate(content) {
  // 优先匹配完整的 YYYY-MM-DD 格式
  let match = content.match(/(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (match) {
    return `${match[1]}-${String(match[2]).padStart(2, '0')}-${String(match[3]).padStart(2, '0')}`
  }
  
  // 然后匹配 MM-DD 格式（通常在时间前面，如"11-11 10:35"或短信头部"11-07 19:05"）
  // 这个模式会匹配"日期 时间"的格式
  match = content.match(/(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{2})/)
  if (match) {
    const month = String(match[1]).padStart(2, '0')
    const day = String(match[2]).padStart(2, '0')
    const currentYear = new Date().getFullYear()
    return `${currentYear}-${month}-${day}`
  }
  
  // 最后尝试单独的 MM-DD 格式，但要避免匹配取件码中的数字
  // 查找所有的 MM-DD 模式
  const allMatches = [...content.matchAll(/(\d{1,2})-(\d{1,2})/g)]
  
  for (const m of allMatches) {
    const beforeText = content.substring(0, m.index)
    const afterText = content.substring(m.index + m[0].length)
    
    // 如果在"凭"字后面且后面跟着数字，这可能是取件码的一部分，跳过
    if (beforeText.includes('凭') && /^\d/.test(afterText)) {
      continue
    }
    
    // 找到一个有效的日期
    const month = String(m[1]).padStart(2, '0')
    const day = String(m[2]).padStart(2, '0')
    const currentYear = new Date().getFullYear()
    return `${currentYear}-${month}-${day}`
  }
  
  return null
}

/**
 * 提取取件时间
 * @param {string} content - 短信内容
 * @returns {string|null} - 取件时间（HH:MM格式）或null
 */
export function extractPickupTime(content) {
  // 匹配 HH:MM 或 H:MM 格式
  const pattern = /(\d{1,2}):(\d{2})/
  const match = content.match(pattern)
  
  if (match) {
    const hour = String(match[1]).padStart(2, '0')
    const minute = match[2]
    return `${hour}:${minute}`
  }
  
  return null
}

/**
 * 提取银行名称
 * @param {string} content - 短信内容
 * @returns {string|null} - 银行名称或null
 */
export function extractBankName(content) {
  const banks = [
    '招商银行', '工商银行', '建设银行', '农业银行', '中国银行',
    '交通银行', '邮储银行', '民生银行', '光大银行', '中信银行',
    '浦发银行', '兴业银行', '平安银行', '华夏银行', '广发银行',
    '招行', '工行', '建行', '农行', '中行'
  ]
  
  for (const bank of banks) {
    if (content.includes(bank)) {
      return bank
    }
  }
  
  return null
}

/**
 * 提取卡号（尾号）
 * @param {string} content - 短信内容
 * @returns {string|null} - 卡号或null
 */
export function extractCardNumber(content) {
  const patterns = [
    /尾号(\d{4})/,
    /卡号尾号(\d{4})/,
    /(\d{4})的?[储蓄借记信用]?卡/,
    /账户(\d{4})/
  ]
  
  for (const pattern of patterns) {
    const match = content.match(pattern)
    if (match) {
      return match[1]
    }
  }
  
  return null
}

/**
 * 提取金额
 * @param {string} content - 短信内容
 * @returns {string|null} - 金额或null
 */
export function extractAmount(content) {
  const patterns = [
    /金额[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /转账[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /支付[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /消费[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /([\d,]+\.?\d*)\s*元/
  ]
  
  for (const pattern of patterns) {
    const match = content.match(pattern)
    if (match) {
      return match[1]
    }
  }
  
  return null
}

/**
 * 提取余额
 * @param {string} content - 短信内容
 * @returns {string|null} - 余额或null
 */
export function extractBalance(content) {
  const patterns = [
    /余额[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /可用余额[：:为]?\s*([\d,]+\.?\d*)\s*元/,
    /账户余额[：:为]?\s*([\d,]+\.?\d*)\s*元/
  ]
  
  for (const pattern of patterns) {
    const match = content.match(pattern)
    if (match) {
      return `余额¥${match[1]}`
    }
  }
  
  return null
}

/**
 * 根据标签智能提取关键信息
 * @param {string} content - 短信内容
 * @param {Array} tags - 标签列表
 * @returns {object} - { type: '类型', key: '关键信息', summary: '摘要' }
 */
export function extractKeyInfo(content, tags = []) {
  if (!content) {
    return { type: 'text', key: '', summary: '' }
  }
  
  const tagNames = tags.map(t => t.name ? t.name.toLowerCase() : '')
  const contentLower = content.toLowerCase()
  
  // 验证码 - 优先级最高
  if (tagNames.some(name => name.includes('验证') || name.includes('code')) || 
      contentLower.includes('验证码')) {
    const code = extractVerificationCode(content)
    if (code) {
      return {
        type: 'code',
        key: code,
        summary: `验证码: ${code}`
      }
    }
  }
  
  // 快递/取件码
  if (tagNames.some(name => name.includes('快递') || name.includes('取件') || name.includes('菜鸟') || name.includes('驿站')) ||
      contentLower.includes('取件') || contentLower.includes('快递') || contentLower.includes('菜鸟')) {
    const code = extractPickupCode(content)
    if (code) {
      return {
        type: 'pickup',
        key: code,
        summary: `取件码: ${code}`
      }
    }
  }
  
  // 银行/金额 - 更宽松的匹配
  if (tagNames.some(name => name.includes('银行') || name.includes('转账') || name.includes('支付') || name.includes('消费')) ||
      contentLower.includes('银行') || contentLower.includes('消费') || contentLower.includes('储蓄卡') || contentLower.includes('信用卡')) {
    const bankName = extractBankName(content)
    const cardNumber = extractCardNumber(content)
    const amount = extractAmount(content)
    const balance = extractBalance(content)
    
    // 构建显示信息
    const parts = []
    
    if (bankName) {
      parts.push(bankName)
    }
    
    if (cardNumber) {
      parts.push(`尾号${cardNumber}`)
    }
    
    if (amount) {
      // 判断是消费还是收入
      if (content.includes('消费') || content.includes('支付') || content.includes('转出')) {
        parts.push(`消费¥${amount}`)
      } else if (content.includes('转入') || content.includes('收入') || content.includes('到账')) {
        parts.push(`收入¥${amount}`)
      } else {
        parts.push(`¥${amount}`)
      }
    }
    
    if (balance) {
      parts.push(`余额¥${balance}`)
    }
    
    if (parts.length > 0) {
      return {
        type: 'bank',
        key: amount || balance || '',
        summary: parts.join(' ')
      }
    }
  }
  
  // 默认：显示摘要（前30个字符）
  const summary = content.length > 30 ? content.substring(0, 30) + '...' : content
  return {
    type: 'text',
    key: '',
    summary
  }
}

/**
 * 格式化显示信息
 * @param {string} content - 短信内容
 * @param {Array} tags - 标签列表
 * @returns {string} - 格式化后的显示文本
 */
export function formatSmsDisplay(content, tags = []) {
  const info = extractKeyInfo(content, tags)
  return info.summary || content
}
