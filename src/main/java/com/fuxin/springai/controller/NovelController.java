package com.fuxin.springai.controller;

import com.fuxin.springai.model.dbo.NovelDetail;
import com.fuxin.springai.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/novel")
public class NovelController {

    private final NovelService novelService;

    @Autowired
    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @PostMapping("/save")
    public String saveNovel(NovelDetail novelDetail) {
        return novelService.saveNovel(novelDetail);
    }

    @PostMapping("/detail")
    public NovelDetail getNovelDetailById(String novelId) {
        return novelService.getNovelDetail(novelId);
    }

}
