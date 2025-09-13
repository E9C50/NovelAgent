package com.fuxin.springai.service.impl;

import com.fuxin.springai.model.constant.PromptConstant;
import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelAgentService;
import com.fuxin.springai.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class NovelAgentServiceImpl implements NovelAgentService {

    private final ChatModel chatModel;

    private final NovelService novelService;

    @Autowired
    public NovelAgentServiceImpl(ChatModel openAiChatModel, NovelService novelService) {
        this.chatModel = openAiChatModel;
        this.novelService = novelService;
    }

    @Override
    public String generateTitle(String outline) {
        return chatModel.call(PromptConstant.buildTitlePrompt(outline));
    }

    @Override
    @Transactional
    public Flux<String> generateOutline(String novelId) {
        NovelDetail novelDetail = novelService.getNovelDetail(novelId);
        String outlinePrompt = PromptConstant.buildOutlinePrompt(novelDetail);
        return chatModel.stream(outlinePrompt);
    }

    @Override
    @Transactional
    public List<NovelCharacter> generateCharacter(String novelId, int characterCount) {
        NovelDetail novelDetail = novelService.getNovelDetail(novelId);
        String characterPrompt = PromptConstant.buildCharacterPrompt(novelDetail, characterCount);

        try {
            return ChatClient.create(chatModel).prompt()
                    .user(characterPrompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<NovelCharacter>>() {
                    });
        } catch (Exception e) {
            log.error("生成小说角色设定失败，错误信息：{}", e.getMessage(), e);
            return generateCharacter(novelId, characterCount);
        }
    }

    @Override
    @Transactional
    public List<NovelChapter> generateChaptersOutline(String novelId, int chapterCount) {
        NovelDetail novelDetail = novelService.getNovelDetail(novelId);
        String chapterOutlinePrompt = PromptConstant.buildChapterOutlinePrompt(novelDetail, chapterCount);

        try {
            return ChatClient.create(chatModel).prompt()
                    .user(chapterOutlinePrompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<NovelChapter>>() {
                    });
        } catch (Exception e) {
            log.error("生成各章节大纲失败，错误信息：{}", e.getMessage(), e);
            return generateChaptersOutline(novelId, chapterCount);
        }
    }

    @Override
    @Transactional
    public Flux<String> generateChapterContent(String novelId, int chapterIndex) {
        NovelDetail novelDetail = novelService.getNovelDetail(novelId);
        String chapterContentPrompt = PromptConstant.buildChapterContentPrompt(novelDetail, chapterIndex);
        return chatModel.stream(chapterContentPrompt);
    }
}
