package com.fuxin.springai.controller;

import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelCharacter;
import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/novel")
public class NovelController {

    private final NovelService novelService;

    @Autowired
    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @PostMapping("/save")
    public String saveNovel(@RequestBody NovelDetail novelDetail) {
        return novelService.saveNovel(novelDetail);
    }

    @GetMapping("/detail")
    public NovelDetail getNovelDetailById(@RequestParam String novelId) {
        return novelService.getNovelDetail(novelId);
    }

    @GetMapping("/list")
    public List<NovelDetail> listAll() {
        Iterable<NovelDetail> iterable = novelService.listAll();
        List<NovelDetail> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }

    @PostMapping("/outline/save")
    public void saveOutline(@RequestParam String novelId, @RequestBody String outline) {
        NovelDetail detail = novelService.getNovelDetail(novelId);
        detail.setOutline(outline);
        novelService.saveNovel(detail);
    }

    @PostMapping("/summary/save")
    public void saveSummary(@RequestParam String novelId, @RequestBody String summary) {
        NovelDetail detail = novelService.getNovelDetail(novelId);
        detail.setSummary(summary);
        novelService.saveNovel(detail);
    }

    @PostMapping("/setting/save")
    public void saveSetting(@RequestParam String novelId, @RequestBody String setting) {
        NovelDetail detail = novelService.getNovelDetail(novelId);
        detail.setSetting(setting);
        novelService.saveNovel(detail);
    }

    @PostMapping("/characters/save")
    public void saveNovelCharacters(@RequestParam String novelId, @RequestBody java.util.List<NovelCharacter> characters) {
        novelService.saveNovelCharacter(novelId, characters);
    }

    @PostMapping("/chapters/save")
    public void saveNovelChapters(@RequestBody java.util.List<NovelChapter> chapters) {
        novelService.saveNovelChapters(chapters);
    }

    @GetMapping("/chapter")
    public NovelChapter getChapter(@RequestParam String novelId, @RequestParam int chapterNumber) {
        return novelService.getNovelChapterByNovelIdAndChapterNumber(novelId, chapterNumber);
    }

}
