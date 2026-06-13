package com.platform.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.ai.domain.entity.Prompt;
import com.platform.ai.mapper.PromptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prompt模板初始化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptInitService implements CommandLineRunner {

    private final PromptMapper promptMapper;

    @Override
    public void run(String... args) {
        long count = promptMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("Prompt模板已存在，跳过初始化");
            return;
        }

        log.info("初始化默认Prompt模板...");
        List<Prompt> prompts = getDefaultPrompts();
        for (Prompt prompt : prompts) {
            promptMapper.insert(prompt);
        }
        log.info("初始化完成，共插入 {} 个Prompt模板", prompts.size());
    }

    private List<Prompt> getDefaultPrompts() {
        return List.of(
            // === 写作类 ===
            createPrompt("文章摘要", "生成简洁的文章摘要", "请为以下文章生成一个简洁的摘要，不超过200个字符。只返回摘要内容。\n\n{{content}}", "writing"),
            createPrompt("标题生成", "为文章生成吸引人的标题", "请为以下内容生成5个吸引人的标题建议。每个标题占一行，带编号。\n\n{{content}}", "writing"),
            createPrompt("标签生成", "为文章生成相关标签", "请为以下文章生成5个相关的标签关键词，用逗号分隔。\n\n标题：{{title}}\n内容：{{content}}", "writing"),
            createPrompt("文章润色", "优化文章表达", "请润色以下文章，使其表达更流畅、专业，但保持原意不变。\n\n{{content}}", "writing"),
            createPrompt("文章续写", "继续写下去", "请根据以下内容继续写下去，保持风格一致，续写约300字。\n\n{{content}}", "writing"),

            // === 代码类 ===
            createPrompt("代码审查", "检查代码问题", "请审查以下代码，指出潜在的问题、bug和改进建议。\n\n```{{language}}\n{{code}}\n```", "code"),
            createPrompt("代码解释", "解释代码逻辑", "请详细解释以下代码的功能和逻辑：\n\n```{{language}}\n{{code}}\n```", "code"),
            createPrompt("代码重构", "优化代码结构", "请重构以下代码，提高可读性和可维护性，保持功能不变。\n\n```{{language}}\n{{code}}\n```", "code"),
            createPrompt("单元测试", "生成单元测试", "请为以下代码编写单元测试，覆盖主要场景：\n\n```{{language}}\n{{code}}\n```", "code"),
            createPrompt("Bug修复", "修复代码问题", "请分析以下代码中的bug并提供修复方案：\n\n```{{language}}\n{{code}}\n```\n\n错误信息：{{error}}", "code"),

            // === 翻译类 ===
            createPrompt("中译英", "中文翻译成英文", "请将以下中文翻译成英文，保持专业和自然：\n\n{{content}}", "translation"),
            createPrompt("英译中", "英文翻译成中文", "请将以下英文翻译成中文，保持专业和自然：\n\n{{content}}", "translation"),
            createPrompt("技术文档翻译", "翻译技术文档", "请将以下技术文档翻译成{{target_language}}，保持术语准确：\n\n{{content}}", "translation"),

            // === 总结类 ===
            createPrompt("会议纪要", "整理会议要点", "请根据以下会议记录，整理出会议要点、决定事项和待办事项：\n\n{{content}}", "summary"),
            createPrompt("学习笔记", "整理学习内容", "请将以下学习内容整理成结构化的笔记，包含要点、关键概念和总结：\n\n{{content}}", "summary"),
            createPrompt("竞品分析", "分析竞品信息", "请根据以下信息进行竞品分析，包括优劣势对比和建议：\n\n{{content}}", "summary"),

            // === 营销类 ===
            createPrompt("产品描述", "撰写产品文案", "请为以下产品撰写吸引人的描述文案：\n\n产品：{{product}}\n特点：{{features}}", "marketing"),
            createPrompt("社交媒体", "生成社媒内容", "请为以下主题生成一条适合{{platform}}的帖子，包含emoji和话题标签：\n\n{{topic}}", "marketing"),
            createPrompt("邮件模板", "撰写商务邮件", "请撰写一封关于{{subject}}的商务邮件：\n\n收件人：{{recipient}}\n要点：{{points}}", "marketing"),

            // === 对话类 ===
            createPrompt("角色扮演", "AI角色扮演", "请你扮演{{role}}，我会向你提问，请以该角色的身份和知识回答。\n\n我的问题是：{{question}}", "chat"),
            createPrompt("头脑风暴", "创意头脑风暴", "请围绕以下主题进行头脑风暴，提供10个创意想法：\n\n{{topic}}", "chat"),
            createPrompt("问题分析", "深入分析问题", "请从多个角度分析以下问题，并提供解决方案：\n\n{{question}}", "chat")
        );
    }

    private Prompt createPrompt(String name, String description, String promptText, String category) {
        Prompt prompt = new Prompt();
        prompt.setName(name);
        prompt.setDescription(description);
        prompt.setPromptText(promptText);
        prompt.setCategory(category);
        prompt.setStatus(1);
        return prompt;
    }
}
