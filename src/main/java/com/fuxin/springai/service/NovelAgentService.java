package com.fuxin.springai.service;

import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import reactor.core.publisher.Flux;

import java.util.List;

public interface NovelAgentService {

    /**
     * 根据大纲生成标题
     *
     * @param outline
     * @return
     */
    String generateTitle(String outline);

    /**
     * 流式生成小说大纲
     *
     * @param novelId
     * @return
     */
    Flux<String> generateOutline(String novelId);

    /**
     * 流式生成小说角色设定
     *
     * @param novelId
     * @return
     */
    List<NovelCharacter> generateCharacter(String novelId, int characterCount);

    /**
     * 流式生成各章节大纲
     *
     * @param novelId
     * @return
     */
    List<NovelChapter> generateChaptersOutline(String novelId, int chapterCount);

    /**
     * 流式生成章节内容
     *
     * @param novelId
     * @param chapterIndex
     * @return
     */
    Flux<String> generateChapterContent(String novelId, int chapterIndex);

}
