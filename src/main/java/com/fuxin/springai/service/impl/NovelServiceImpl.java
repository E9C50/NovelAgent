package com.fuxin.springai.service.impl;

import com.fuxin.springai.mapper.NovelChapterRepository;
import com.fuxin.springai.mapper.NovelRepository;
import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public String saveNovel(NovelDetail novelDetail) {
        // 避免覆盖已有关联：如传入仅包含基本字段时，不改动 characters/chapters
        if (novelDetail.getNovelId() != null) {
            NovelDetail existing = novelRepository.getByNovelId(novelDetail.getNovelId());
            if (existing != null) {
                existing.setTitle(novelDetail.getTitle());
                existing.setGenre(novelDetail.getGenre());
                existing.setStyle(novelDetail.getStyle());
                existing.setTags(novelDetail.getTags());
                existing.setSummary(novelDetail.getSummary());
                existing.setSetting(novelDetail.getSetting());
                existing.setOutline(novelDetail.getOutline());
                // 不改 existing.characters 与 existing.chapters，除非调用专门的保存接口
                novelRepository.save(existing);
                return existing.getNovelId();
            }
        }
        novelDetail = novelRepository.save(novelDetail);
        return novelDetail.getNovelId();
    }

    @Override
    @Transactional(readOnly = true)
    public NovelDetail getNovelDetail(String novelId) {
        return novelRepository.getByNovelId(novelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<NovelDetail> listAll() {
        return novelRepository.findAll();
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
        List<NovelChapter> list = novelChapterRepository.getByNovelIdEqualsAndChapterNumberEquals(novelId, chapterIndex);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}
