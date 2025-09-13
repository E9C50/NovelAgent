package com.fuxin.springai.model.dbo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "novel_character")
public class NovelCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String characterId; // 角色ID

    @Column(name = "novel_id", length = 64)
    private String novelId; // 所属小说ID

    @Column(name = "name", length = 32)
    private String name; // 角色名称

    @Column(name = "age")
    private String age; // 角色年龄

    @Column(name = "gender")
    private String gender; // 角色性别

    @Column(name = "appearance", length = 1024)
    private String appearance; // 外貌描述

    @Column(name = "personality", length = 1024)
    private String personality; // 性格特点

    @Column(name = "background", length = 1024)
    private String background; // 背景故事

    @Column(name = "role", length = 1024)
    private String role; // 角色在故事中的作用
}
