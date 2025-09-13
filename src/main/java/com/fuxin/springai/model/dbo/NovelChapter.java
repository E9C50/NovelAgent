package com.fuxin.springai.model.dbo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "novel_chapter")
public class NovelChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String chapterId; // 章节ID

    @Column(name = "novel_id", length = 64)
    private String novelId; // 所属小说ID

    @Column(name = "chapter_number")
    private Integer chapterNumber; // 章节编号

    @Column(name = "title", length = 128)
    private String title; // 章节标题

    @Column(name = "summary", length = 2048)
    private String summary; // 章节摘要

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content; // 章节内容
}
