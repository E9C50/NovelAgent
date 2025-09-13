package com.fuxin.springai;

import com.alibaba.fastjson2.JSON;
import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelAgentService;
import com.fuxin.springai.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
class NovelWritingAgentTest {

    private final NovelService novelService;
    private final NovelAgentService novelAgentService;

    private final String NOVEL_ID = "7f40ed0b-e886-4d07-b361-4cd7526464be";

    @Autowired
    public NovelWritingAgentTest(NovelService novelService, NovelAgentService novelAgentService) {
        this.novelService = novelService;
        this.novelAgentService = novelAgentService;
    }

    @Test
    void testGenerateTitle() {
        String outline = "我是一名普通的上班族，意外重生到一个平行世界，成为了全球首富的孙子。在这个充满机遇与挑战的世界里，我将如何利用我的智慧和前世的记忆，改变自己的命运，迎接全新的生活？";
        String title = novelAgentService.generateTitle(outline);
        log.info("自动生成标题为：{}", title);
    }

    @Test
    void testCreateNovel() {
        String novelId = novelService.saveNovel(getNovelDetail());
        log.info("创建小说成功，小说ID为：{}", novelId);
    }

    @Test
    void testGenerateNovel() {
        NovelDetail novelDetail = novelService.getNovelDetail(NOVEL_ID);

        StringBuffer novelOutline = new StringBuffer();
        novelAgentService.generateOutline(NOVEL_ID).doOnNext(c -> {
            novelOutline.append(c);
            System.out.print(c);
        }).collectList().block();

        novelDetail.setOutline(novelOutline.toString());
        novelService.saveNovel(novelDetail);
    }

    @Test
    void testGenerateCharacter() {
        List<NovelCharacter> novelCharacterList = novelAgentService.generateCharacter(NOVEL_ID, 2);
        log.info("角色设定：{}", JSON.toJSONString(novelCharacterList));

        novelCharacterList.forEach(novelCharacter -> {
            novelCharacter.setNovelId(NOVEL_ID);
            novelCharacter.setCharacterId(null);
        });

        novelService.saveNovelCharacter(NOVEL_ID, novelCharacterList);
    }

    @Test
    void testGenerateChaptersOutline() {
        List<NovelChapter> chaptersOutline = novelAgentService.generateChaptersOutline(NOVEL_ID, 10);
        log.info("章节大纲：{}", JSON.toJSONString(chaptersOutline));

        AtomicInteger counter = new AtomicInteger(1);
        chaptersOutline.forEach(novelChapter -> {
            novelChapter.setNovelId(NOVEL_ID);
            novelChapter.setChapterId(null);
            novelChapter.setChapterNumber(counter.getAndIncrement());
        });

        novelService.saveNovelChapters(chaptersOutline);
    }

    @Test
    void testGenerateChapterContent() {
        for (int chapterIndex = 1; chapterIndex <= 10; chapterIndex++) {
            StringBuffer chapterContent = new StringBuffer();
            novelAgentService.generateChapterContent(NOVEL_ID, chapterIndex)
                    .doOnNext(c -> {
                        chapterContent.append(c);
                        System.out.print(c);
                    })
                    .collectList()
                    .block();

            NovelChapter chapter = novelService.getNovelChapterByNovelIdAndChapterNumber(NOVEL_ID, chapterIndex);
            chapter.setContent(chapterContent.toString());
            novelService.saveNovelChapter(chapter);
        }
    }

    private static NovelDetail getNovelDetail() {
        NovelDetail novelDetail = new NovelDetail();
        novelDetail.setTitle("重生之全球首富竟是我孙子");
        novelDetail.setStyle("轻松搞笑");
        novelDetail.setGenre("都市");
        novelDetail.setSetting("我是一个普通的上班族，意外重生到一个平行世界，成为了全球首富的孙子。在这个充满机遇与挑战的世界里，我将如何利用我的智慧和前世的记忆，改变自己的命运，迎接全新的生活？");
        novelDetail.setTags("重生，搞笑，都市，逆袭");
        return novelDetail;
    }
}