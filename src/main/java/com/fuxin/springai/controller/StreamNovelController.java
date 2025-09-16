package com.fuxin.springai.controller;

import com.fuxin.springai.service.impl.NovelAgentServiceImpl;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream/novel")
public class StreamNovelController {

    private final NovelAgentServiceImpl streamingAgent;

    @Autowired
    public StreamNovelController(NovelAgentServiceImpl streamingAgent) {
        this.streamingAgent = streamingAgent;
    }

    /**
     * 流式生成小说大纲
     *
     * @param novelId
     * @return
     */
    @RequestMapping(value = "/generateOutline", method = RequestMethod.POST, produces = "text/event-stream")
    public Flux<String> generateOutline(@RequestParam @Validated String novelId) {
        return streamingAgent.generateOutline(novelId);
    }

    /**
     * 流式生成章节内容
     */
    @RequestMapping(value = "/generateChapterContent", method = RequestMethod.POST, produces = "text/event-stream")
    public Flux<String> generateChapterContent(@RequestParam @Validated String novelId, @RequestParam @Validated Integer chapterIndex) {
        return streamingAgent.generateChapterContent(novelId, chapterIndex);
    }

}