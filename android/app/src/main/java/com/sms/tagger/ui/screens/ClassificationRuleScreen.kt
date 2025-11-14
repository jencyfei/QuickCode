package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.Switch
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.data.model.RuleType
import com.sms.tagger.data.model.TagRule
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary

/**
 * 分类规则管理页面
 * 用于管理对短信内容进行分类的规则
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationRuleScreen(onBack: (() -> Unit)? = null) {
    // 初始化分类规则
    val initialClassificationRules = listOf(
        // 验证码分类规则
        TagRule(
            id = "classify_verify_01",
            ruleName = "验证码分类",
            tagName = "验证码",
            ruleType = RuleType.CONTENT,
            condition = "验证码",
            extractPosition = "验证码",
            extractLength = 6,
            isEnabled = true,
            priority = 10,
            isBuiltIn = true
        ),
        // 快递分类规则
        TagRule(
            id = "classify_express_01",
            ruleName = "快递分类",
            tagName = "快递",
            ruleType = RuleType.CONTENT,
            condition = "快递",
            extractPosition = "快递",
            extractLength = 0,
            isEnabled = true,
            priority = 9,
            isBuiltIn = true
        ),
        // 银行分类规则
        TagRule(
            id = "classify_bank_01",
            ruleName = "银行分类",
            tagName = "银行",
            ruleType = RuleType.CONTENT,
            condition = "银行",
            extractPosition = "银行",
            extractLength = 0,
            isEnabled = true,
            priority = 8,
            isBuiltIn = true
        ),
        // 通知分类规则
        TagRule(
            id = "classify_notify_01",
            ruleName = "通知分类",
            tagName = "通知",
            ruleType = RuleType.CONTENT,
            condition = "通知",
            extractPosition = "通知",
            extractLength = 0,
            isEnabled = true,
            priority = 7,
            isBuiltIn = true
        ),
        // 营销分类规则
        TagRule(
            id = "classify_marketing_01",
            ruleName = "营销分类",
            tagName = "营销",
            ruleType = RuleType.CONTENT,
            condition = "营销",
            extractPosition = "营销",
            extractLength = 0,
            isEnabled = true,
            priority = 6,
            isBuiltIn = true
        )
    )
    
    var rules by remember { mutableStateOf(initialClassificationRules) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TagRule?>(null) }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("短信分类规则") },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { 
                        editingRule = null
                        showAddRuleDialog = true 
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    containerColor = Color.White.copy(alpha = 0.35f),
                    contentColor = Color(0xFF667EEA),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加规则",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无规则，点击 + 添加",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 分离内置规则和自定义规则
                    val builtInRules = rules.filter { it.isBuiltIn }
                    val customRules = rules.filter { !it.isBuiltIn }
                    
                    // 内置分类规则分组
                    if (builtInRules.isNotEmpty()) {
                        item {
                            Text(
                                "🏷️ 内置分类规则",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667EEA),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(builtInRules) { rule ->
                            ClassificationRuleCard(
                                rule = rule,
                                isBuiltIn = true,
                                onEdit = { 
                                    editingRule = rule
                                    showAddRuleDialog = true 
                                },
                                onDelete = { 
                                    rules = rules.filter { it.id != rule.id }
                                },
                                onToggle = {
                                    rules = rules.map { 
                                        if (it.id == rule.id) it.copy(isEnabled = !it.isEnabled)
                                        else it
                                    }
                                }
                            )
                        }
                    }
                    
                    // 自定义分类规则分组
                    if (customRules.isNotEmpty()) {
                        item {
                            Text(
                                "⚙️ 自定义规则",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 0.dp)
                            )
                        }
                        items(customRules) { rule ->
                            ClassificationRuleCard(
                                rule = rule,
                                isBuiltIn = false,
                                onEdit = { 
                                    editingRule = rule
                                    showAddRuleDialog = true 
                                },
                                onDelete = { 
                                    rules = rules.filter { it.id != rule.id }
                                },
                                onToggle = {
                                    rules = rules.map { 
                                        if (it.id == rule.id) it.copy(isEnabled = !it.isEnabled)
                                        else it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 添加/编辑规则对话框
    if (showAddRuleDialog) {
        AddClassificationRuleDialog(
            rule = editingRule,
            onSave = { newRule ->
                if (editingRule != null) {
                    // 编辑规则时，保留原有的 isBuiltIn 标志
                    rules = rules.map { 
                        if (it.id == editingRule!!.id) {
                            newRule.copy(isBuiltIn = it.isBuiltIn)
                        } else {
                            it
                        }
                    }
                } else {
                    // 添加新规则时，设置为非内置规则
                    rules = rules + newRule.copy(
                        id = System.currentTimeMillis().toString(),
                        isBuiltIn = false
                    )
                }
                showAddRuleDialog = false
                editingRule = null
            },
            onDismiss = {
                showAddRuleDialog = false
                editingRule = null
            }
        )
    }
}

/**
 * 分类规则卡片
 */
@Composable
fun ClassificationRuleCard(
    rule: TagRule,
    isBuiltIn: Boolean = false,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onToggle: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 规则名称和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.ruleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "标签: ${rule.tagName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle?.invoke() }
                )
            }
            
            // 规则详情
            Text(
                text = "类型: ${if (rule.ruleType == RuleType.SENDER) "发信人" else "短信内容"}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "条件: ${rule.condition}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            if (rule.extractLength > 0) {
                Text(
                    text = "提取: ${rule.extractPosition} 后 ${rule.extractLength} 个字符",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit ?: {},
                    enabled = onEdit != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑", fontSize = 12.sp)
                }
                Button(
                    onClick = onDelete ?: {},
                    enabled = onDelete != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除", fontSize = 12.sp)
                }
            }
            
            // 内置规则标签
            if (isBuiltIn) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF667EEA).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🏷️ 内置规则（可编辑）",
                        fontSize = 12.sp,
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 添加/编辑分类规则对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassificationRuleDialog(
    rule: TagRule?,
    onSave: (TagRule) -> Unit,
    onDismiss: () -> Unit
) {
    var ruleName by remember { mutableStateOf(rule?.ruleName ?: "") }
    var tagName by remember { mutableStateOf(rule?.tagName ?: "") }
    var ruleType by remember { mutableStateOf(rule?.ruleType ?: RuleType.CONTENT) }
    var conditionKeyword by remember { mutableStateOf(rule?.condition ?: "") }
    var extractPosition by remember { mutableStateOf(rule?.extractPosition ?: "") }
    var extractLength by remember { mutableStateOf(rule?.extractLength?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            ),
        containerColor = Color.White.copy(alpha = 0.95f),
        title = { 
            Text(
                if (rule == null) "添加分类规则" else "编辑分类规则",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 规则名称
                Text("规则名称", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    placeholder = { Text("例如: 验证码分类") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 标签名称
                Text("标签名称", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    placeholder = { Text("例如: 验证码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 规则类型
                Text("规则类型", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RuleType.values().forEach { type ->
                        Button(
                            onClick = { ruleType = type },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (ruleType == type)
                                    Color(0xFF667EEA)
                                else
                                    Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (type == RuleType.SENDER) "发信人" else "短信内容",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                // 条件关键词
                Text("条件关键词", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = conditionKeyword,
                    onValueChange = { conditionKeyword = it },
                    placeholder = { Text("例如: 验证码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 提取位置
                Text("提取位置（关键词）", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = extractPosition,
                    onValueChange = { extractPosition = it },
                    placeholder = { Text("例如: 验证码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 提取长度
                Text("提取长度（字符数）", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = extractLength,
                    onValueChange = { extractLength = it },
                    placeholder = { Text("例如: 6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ruleName.isNotBlank() && tagName.isNotBlank() && 
                        conditionKeyword.isNotBlank()) {
                        onSave(
                            TagRule(
                                id = rule?.id ?: "",
                                ruleName = ruleName,
                                tagName = tagName,
                                ruleType = ruleType,
                                condition = conditionKeyword,
                                extractPosition = extractPosition,
                                extractLength = extractLength.toIntOrNull() ?: 0,
                                isEnabled = rule?.isEnabled ?: true,
                                priority = rule?.priority ?: 0
                            )
                        )
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .background(
                        color = Color(0xFF667EEA),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    "保存",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .height(40.dp)
                    .background(
                        color = Color(0xFFE8E8E8),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8E8E8)
                )
            ) {
                Text(
                    "取消",
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
