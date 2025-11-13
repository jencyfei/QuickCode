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
 * 规则管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManageScreen(onBack: (() -> Unit)? = null) {
    var rules by remember { mutableStateOf<List<TagRule>>(emptyList()) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TagRule?>(null) }
    
    // 内置快递规则
    val builtInExpressRules = listOf(
        TagRule(
            id = "builtin_cainiao_01",
            ruleName = "菜鸟驿站取件码",
            tagName = "快递",
            ruleType = RuleType.CONTENT,
            condition = "【菜鸟驿站】",
            extractPosition = "凭",
            extractLength = 8,
            isEnabled = true,
            priority = 100
        )
    )
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("自定义规则") },
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
            // 合并内置规则和自定义规则
            val allRules = builtInExpressRules + rules
            
            if (allRules.isEmpty()) {
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
                    // 内置规则分组
                    if (builtInExpressRules.isNotEmpty()) {
                        item {
                            Text(
                                "📦 内置快递规则",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667EEA),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(builtInExpressRules) { rule ->
                            RuleCard(
                                rule = rule,
                                isBuiltIn = true,
                                onEdit = null,
                                onDelete = null,
                                onToggle = null
                            )
                        }
                    }
                    
                    // 自定义规则分组
                    if (rules.isNotEmpty()) {
                        item {
                            Text(
                                "⚙️ 自定义规则",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 0.dp)
                            )
                        }
                        items(rules) { rule ->
                            RuleCard(
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
        AddRuleDialog(
            rule = editingRule,
            onSave = { newRule ->
                if (editingRule != null) {
                    rules = rules.map { if (it.id == editingRule!!.id) newRule else it }
                } else {
                    rules = rules + newRule.copy(id = System.currentTimeMillis().toString())
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
 * 规则卡片
 */
@Composable
fun RuleCard(
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
            Text(
                text = "提取: ${rule.extractPosition} 后 ${rule.extractLength} 个字符",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            
            // 操作按钮
            if (isBuiltIn) {
                // 内置规则：只显示"内置"标签
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
                        "🔒 内置规则（不可编辑）",
                        fontSize = 12.sp,
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // 自定义规则：显示编辑和删除按钮
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
            }
        }
    }
}

/**
 * 添加/编辑规则对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    rule: TagRule?,
    onSave: (TagRule) -> Unit,
    onDismiss: () -> Unit
) {
    var ruleName by remember { mutableStateOf(rule?.ruleName ?: "") }
    var tagName by remember { mutableStateOf(rule?.tagName ?: "") }
    var ruleType by remember { mutableStateOf(rule?.ruleType ?: RuleType.SENDER) }
    var senderConditionType by remember { mutableStateOf("contains") }  // 发信人条件类型
    var conditionKeyword by remember { mutableStateOf(rule?.condition?.substringAfter("|") ?: "") }  // 条件关键词
    var extractPosition by remember { mutableStateOf(rule?.extractPosition ?: "") }
    var extractLength by remember { mutableStateOf(rule?.extractLength?.toString() ?: "") }
    var expandedConditionType by remember { mutableStateOf(false) }  // 下拉菜单展开状态
    
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
                if (rule == null) "添加新规则" else "编辑规则",
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
                    placeholder = { Text("例如: 快递取件码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 标签名称
                Text("标签名称", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    placeholder = { Text("例如: 快递") },
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
                
                // 条件
                if (ruleType == RuleType.SENDER) {
                    Text("条件类型", style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedConditionType = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                senderConditionType,
                                color = Color(0xFF333333),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        }
                        DropdownMenu(
                            expanded = expandedConditionType,
                            onDismissRequest = { expandedConditionType = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("contains", "startsWith", "endsWith").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        senderConditionType = type
                                        expandedConditionType = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("关键词", style = MaterialTheme.typography.labelSmall)
                    TextField(
                        value = conditionKeyword,
                        onValueChange = { conditionKeyword = it },
                        placeholder = { Text("例如: 菜鸟") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text("条件关键词", style = MaterialTheme.typography.labelSmall)
                    TextField(
                        value = conditionKeyword,
                        onValueChange = { conditionKeyword = it },
                        placeholder = { Text("例如: 取件码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // 提取位置
                Text("提取位置（关键词）", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = extractPosition,
                    onValueChange = { extractPosition = it },
                    placeholder = { Text("例如: 取件码") },
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
                        conditionKeyword.isNotBlank() && extractPosition.isNotBlank() && 
                        extractLength.isNotBlank()) {
                        // 构建条件字符串
                        val finalCondition = if (ruleType == RuleType.SENDER) {
                            "$senderConditionType|$conditionKeyword"
                        } else {
                            conditionKeyword
                        }
                        
                        onSave(
                            TagRule(
                                id = rule?.id ?: "",
                                ruleName = ruleName,
                                tagName = tagName,
                                ruleType = ruleType,
                                condition = finalCondition,
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
