package com.fuxin.springai.model.dbo;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "novel_detail")
public class NovelDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String novelId; // 小说ID

    @Column(name = "title", length = 64)
    private String title; // 小说标题

    @Column(name = "style", length = 32)
    private String style; // 小说风格

    @Column(name = "genre", length = 32)
    private String genre; // 小说类型

    @Column(name = "setting", length = 256)
    private String setting; // 背景设定

    @Column(name = "outline", length = 2048)
    private String outline; // 小说大纲

    @Column(name = "tags", length = 256)
    private String tags; // 小说标签

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "novel_id", referencedColumnName = "novelId")
    private List<NovelCharacter> characters; // 角色设定

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "novel_id", referencedColumnName = "novelId")
    private List<NovelChapter> chapters; // 章节列表
}

