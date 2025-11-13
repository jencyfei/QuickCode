/**
 * 短信导入解析工具
 * 支持多种短信格式的解析
 */

/**
 * 解析粘贴的短信内容
 * 支持格式：
 * 1. Android短信导出格式
 * 2. 简单文本格式
 * 3. 带时间戳的格式
 * 
 * @param {string} text - 粘贴的短信文本
 * @returns {Array} - 解析后的短信数组
 */
export function parseSmsText(text) {
  if (!text || !text.trim()) {
    return []
  }

  const smsList = []
  
  // 尝试多种解析方式
  
  // 方式1: Android标准导出格式
  const androidFormat = parseAndroidFormat(text)
  if (androidFormat.length > 0) {
    return androidFormat
  }
  
  // 方式2: 简单分段格式（用---或空行分隔）
  const simpleFormat = parseSimpleFormat(text)
  if (simpleFormat.length > 0) {
    return simpleFormat
  }
  
  // 方式3: 单条短信（最基础）
  const singleSms = parseSingleSms(text)
  if (singleSms) {
    return [singleSms]
  }
  
  return []
}

/**
 * 解析Android标准格式
 * 格式示例：
 * 发件人: 95533
 * 时间: 2025-11-06 10:30
 * 内容: 【验证码】您的验证码是123456
 */
function parseAndroidFormat(text) {
  const smsList = []
  
  // 按---或===分隔
  const blocks = text.split(/\n[-=]{3,}\n/)
  
  for (const block of blocks) {
    const lines = block.trim().split('\n')
    
    let sender = ''
    let content = ''
    let receivedAt = null
    
    for (const line of lines) {
      const trimLine = line.trim()
      
      // 匹配发件人
      if (trimLine.match(/^(发件人|From|Sender)[：:]/i)) {
        sender = trimLine.split(/[：:]/)[1]?.trim() || ''
      }
      // 匹配时间
      else if (trimLine.match(/^(时间|Time|Date)[：:]/i)) {
        const timeStr = trimLine.split(/[：:]/)[1]?.trim()
        receivedAt = parseTimeString(timeStr)
      }
      // 匹配内容
      else if (trimLine.match(/^(内容|Content|Message)[：:]/i)) {
        content = trimLine.split(/[：:]/).slice(1).join(':').trim()
      }
      // 如果没有前缀，可能是内容的延续
      else if (content && trimLine) {
        content += '\n' + trimLine
      }
    }
    
    // 如果没有明确的内容标记，将非标签行都视为内容
    if (!content && lines.length > 0) {
      const contentLines = lines.filter(line => 
        !line.match(/^(发件人|时间|From|Sender|Time|Date)[：:]/i)
      )
      content = contentLines.join('\n').trim()
    }
    
    if (sender && content) {
      smsList.push({
        sender,
        content,
        received_at: receivedAt || new Date().toISOString(),
        phone_number: sender
      })
    }
  }
  
  return smsList
}

/**
 * 解析简单格式
 * 每条短信用空行分隔，第一行是发件人，后续是内容
 */
function parseSimpleFormat(text) {
  const smsList = []
  
  // 按双换行符分隔
  const blocks = text.split(/\n\s*\n/)
  
  for (const block of blocks) {
    const lines = block.trim().split('\n').filter(line => line.trim())
    
    if (lines.length >= 2) {
      const sender = lines[0].trim()
      const content = lines.slice(1).join('\n').trim()
      
      smsList.push({
        sender,
        content,
        received_at: new Date().toISOString(),
        phone_number: sender
      })
    }
  }
  
  return smsList
}

/**
 * 解析单条短信
 * 最简单的情况：只有内容，尝试提取发件人
 */
function parseSingleSms(text) {
  const trimText = text.trim()
  
  if (!trimText) {
    return null
  }
  
  // 尝试从内容中提取发件人
  let sender = '未知'
  
  // 检查常见短信格式
  const senderPatterns = [
    /^【(.+?)】/,           // 【发件人】内容
    /^\[(.+?)\]/,           // [发件人]内容
    /^(\d{3,6})[：:\s]/,   // 95533: 内容
  ]
  
  for (const pattern of senderPatterns) {
    const match = trimText.match(pattern)
    if (match) {
      sender = match[1]
      break
    }
  }
  
  // 检查已知的短信发送号码
  const knownSenders = [
    '95533', '95588', '95599', '95555', // 银行
    '10086', '10010', '10000',           // 运营商
    '106',                                // 106开头的服务号
  ]
  
  for (const known of knownSenders) {
    if (trimText.includes(known) || trimText.startsWith(known)) {
      sender = known
      break
    }
  }
  
  return {
    sender,
    content: trimText,
    received_at: new Date().toISOString(),
    phone_number: sender
  }
}

/**
 * 解析时间字符串
 */
function parseTimeString(timeStr) {
  if (!timeStr) {
    return new Date().toISOString()
  }
  
  try {
    // 尝试多种时间格式
    const formats = [
      /(\d{4})[-/](\d{1,2})[-/](\d{1,2})\s+(\d{1,2}):(\d{1,2}):?(\d{1,2})?/, // 2025-11-06 10:30:00
      /(\d{4})[-/](\d{1,2})[-/](\d{1,2})\s+(\d{1,2}):(\d{1,2})/,              // 2025-11-06 10:30
      /(\d{1,2})[-/](\d{1,2})\s+(\d{1,2}):(\d{1,2})/,                          // 11-06 10:30
    ]
    
    for (const format of formats) {
      const match = timeStr.match(format)
      if (match) {
        const date = new Date(timeStr)
        if (!isNaN(date.getTime())) {
          return date.toISOString()
        }
      }
    }
    
    // 尝试直接解析
    const date = new Date(timeStr)
    if (!isNaN(date.getTime())) {
      return date.toISOString()
    }
  } catch (e) {
    console.error('时间解析失败:', e)
  }
  
  return new Date().toISOString()
}

/**
 * 验证短信数据
 */
export function validateSmsData(smsData) {
  const errors = []
  
  if (!smsData.sender || smsData.sender.trim() === '') {
    errors.push('发件人不能为空')
  }
  
  if (!smsData.content || smsData.content.trim() === '') {
    errors.push('短信内容不能为空')
  }
  
  if (smsData.content && smsData.content.length > 1000) {
    errors.push('短信内容过长（最多1000字符）')
  }
  
  return errors
}

/**
 * 格式化导入预览
 */
export function formatImportPreview(smsList) {
  if (smsList.length === 0) {
    return '未识别到有效短信'
  }
  
  const preview = smsList.map((sms, index) => {
    return `${index + 1}. ${sms.sender}\n   ${sms.content.substring(0, 50)}${sms.content.length > 50 ? '...' : ''}`
  }).join('\n\n')
  
  return `识别到 ${smsList.length} 条短信：\n\n${preview}`
}

