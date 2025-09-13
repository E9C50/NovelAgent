package com.fuxin.springai.service.impl;

import com.fuxin.springai.mapper.NovelChapterRepository;
import com.fuxin.springai.mapper.NovelRepository;
import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NovelServiceImpl implements NovelService {

    private final NovelRepository novelRepository;

    private final NovelChapterRepository novelChapterRepository;

    @Autowired
    public NovelServiceImpl(NovelRepository novelRepository, NovelChapterRepository novelChapterRepository) {
        this.novelRepository = novelRepository;
        this.novelChapterRepository = novelChapterRepository;
    }

    @Override
    public String saveNovel(NovelDetail novelDetail) {
        novelDetail = novelRepository.save(novelDetail);
        return novelDetail.getNovelId();
    }

    @Override
    public NovelDetail getNovelDetail(String novelId) {
        return novelRepository.getByNovelId(novelId);
    }

    @Override
    public void saveNovelCharacter(String novelId, List<NovelCharacter> novelCharacters) {
        NovelDetail novelDetail = getNovelDetail(novelId);
        novelDetail.setCharacters(novelCharacters);
        novelRepository.save(novelDetail);
    }

    @Override
    public void saveNovelChapter(NovelChapter novelChapter) {
        novelChapterRepository.save(novelChapter);
    }

    @Override
    public void saveNovelChapters(List<NovelChapter> novelChapters) {
        novelChapterRepository.saveAll(novelChapters);
    }

    @Override
    public NovelChapter getNovelChapterByNovelIdAndChapterNumber(String novelId, int chapterIndex) {
        return novelChapterRepository.getByNovelIdEqualsAndChapterNumberEquals(novelId, chapterIndex).getFirst();
    }
}
