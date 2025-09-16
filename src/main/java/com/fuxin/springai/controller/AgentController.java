package com.fuxin.springai.controller;

import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.service.NovelAgentService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/novel")
public class AgentController {

    private final NovelAgentService agentService;
    private final ChatModel chatModel;

    @Autowired
    public AgentController(NovelAgentService agentService, ChatModel openAiChatModel) {
        this.agentService = agentService;
        this.chatModel = openAiChatModel;
    }

    /**
     * 同步生成标题
     */
    @PostMapping("/generateTitle")
    public String generateTitle(@RequestParam @Validated String outline) {
        return agentService.generateTitle(outline);
    }

    /**
     * 同步生成角色设定
     */
    @PostMapping("/generateCharacters")
    public List<NovelCharacter> generateCharacters(@RequestParam @Validated String novelId, @RequestParam(defaultValue = "5") Integer count) {
        return agentService.generateCharacter(novelId, count);
    }

    /**
     * 同步生成章节大纲
     */
    @PostMapping("/generateChaptersOutline")
    public List<NovelChapter> generateChaptersOutline(@RequestParam @Validated String novelId, @RequestParam(defaultValue = "10") Integer count) {
        return agentService.generateChaptersOutline(novelId, count);
    }

    /**
     * 同步生成简介
     */
    @PostMapping("/generateSummary")
    public String generateSummary(@RequestParam(required = false) String title,
                                  @RequestParam String genre,
                                  @RequestParam String style,
                                  @RequestParam String tags,
                                  @RequestParam(required = false) String setting) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息生成一段不超过150字的小说简介，语言为中文：\n");
        prompt.append("类型: ").append(genre).append("\n");
        prompt.append("风格: ").append(style).append("\n");
        prompt.append("标签: ").append(tags).append("\n");
        if (title != null && !title.isEmpty()) {
            prompt.append("标题: ").append(title).append("\n");
        }
        if (setting != null && !setting.isEmpty()) {
            prompt.append("设定: ").append(setting).append("\n");
        }
        prompt.append("要求: 简洁、有吸引力，作为小说封面预览内容，不要包含分段或列表。\n");
        return chatModel.call(prompt.toString());
    }

    /**
     * 同步生成设定
     */
    @PostMapping("/generateSetting")
    public String generateSetting(@RequestParam(required = false) String title,
                                  @RequestParam String genre,
                                  @RequestParam String style,
                                  @RequestParam String tags,
                                  @RequestParam(required = false) String summary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息生成小说世界观与故事设定（不超过200字），语言为中文：\n");
        prompt.append("类型: ").append(genre).append("\n");
        prompt.append("风格: ").append(style).append("\n");
        prompt.append("标签: ").append(tags).append("\n");
        if (title != null && !title.isEmpty()) {
            prompt.append("标题: ").append(title).append("\n");
        }
        if (summary != null && !summary.isEmpty()) {
            prompt.append("简介: ").append(summary).append("\n");
        }
        prompt.append("要求: 描述核心世界观、主角背景与核心冲突，用于AI辅助生成，避免分段。\n");
        return chatModel.call(prompt.toString());
    }
}


