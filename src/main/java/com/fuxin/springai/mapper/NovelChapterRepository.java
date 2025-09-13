package com.fuxin.springai.mapper;


import com.fuxin.springai.model.dbo.NovelChapter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NovelChapterRepository extends CrudRepository<NovelChapter, String> {

    List<NovelChapter> getByNovelIdEqualsAndChapterNumberEquals(String novelId, Integer chapterNumber);
}
