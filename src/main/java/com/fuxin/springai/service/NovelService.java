package com.fuxin.springai.service;

import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;

import java.util.List;

public interface NovelService {

    /**
     * 创建小说
     *
     * @param novelDetail 小说详情
     * @return 小说ID
     */
    String saveNovel(NovelDetail novelDetail);

    /**
     * 获取小说详情
     *
     * @param novelId 小说ID
     * @return 小说详情
     */
    NovelDetail getNovelDetail(String novelId);

    /**
     * 列出全部小说
     */
    Iterable<NovelDetail> listAll();

    /**
     * 保存小说角色设定
     *
     * @param novelId            小说ID
     * @param novelCharacterList 角色设定列表
     */
    void saveNovelCharacter(String novelId, List<NovelCharacter> novelCharacterList);

    /**
     * 保存小说章节
     *
     * @param novelChapter
     */
    void saveNovelChapter(NovelChapter novelChapter);

    /**
     * 批量保存小说章节
     *
     * @param novelChapters
     */
    void saveNovelChapters(List<NovelChapter> novelChapters);

    /**
     * 根据小说ID和章节序号获取章节
     *
     * @param novelId      小说ID
     * @param chapterIndex 章节序号
     * @return 小说章节
     */
    NovelChapter getNovelChapterByNovelIdAndChapterNumber(String novelId, int chapterIndex);
}
