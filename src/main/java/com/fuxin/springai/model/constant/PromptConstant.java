package com.fuxin.springai.model.constant;

import com.fuxin.springai.model.dbo.NovelChapter;
import com.fuxin.springai.model.dbo.NovelDetail;

import java.util.List;
import java.util.stream.Collectors;

public class PromptConstant {

    /**
     * 构建小说标题提示语
     *
     * @param outline 小说大纲
     * @return
     */
    public static String buildTitlePrompt(String outline) {
        final String TITLE_PROMPT = """
                你是一位专业的小说家。请根据以下小说大纲，创作一个吸引人的小说标题：
                
                小说大纲：%s
                
                标题应简洁、有吸引力，并能反映小说的主题和风格。
                请用中文回复，保持专业性和创造性。
                """;
        return String.format(TITLE_PROMPT, outline);
    }

    /**
     * 构建小说大纲提示语
     *
     * @return
     */
    public static String buildOutlinePrompt(NovelDetail detail) {
        final String OUTLINE_PROMPT = """
                你是一位专业的小说家。请根据以下要求创作一部小说的详细大纲：
                
                小说标题：%s
                小说风格：%s
                小说类型：%s
                小说标签：%s
                背景设定：%s
                
                请提供：
                1. 故事梗概（200字左右）
                2. 完整的故事大纲，包含起承转合
                
                请用中文回复，不要有任何多余的解释或说明，保持专业性和创造性。
                """;
        return String.format(OUTLINE_PROMPT,
                detail.getTitle(), detail.getStyle(), detail.getGenre(),
                String.join("，", detail.getTags()), detail.getSetting());
    }

    /**
     * 构建角色设定提示语
     *
     * @param detail
     * @param characterCount
     * @return
     */
    public static String buildCharacterPrompt(NovelDetail detail, int characterCount) {
        final String CHARACTER_PROMPT = """
                你是一位专业的小说家。请根据以下小说信息和要求，创作主要角色设定：
                
                小说标题：%s
                小说风格：%s
                小说类型：%s
                小说标签：%s
                背景设定：%s
                小说大纲：%s
                
                角色数量：%d
                
                请提供每个角色的姓名、年龄、性别、外貌描述、性格特点、背景故事和在小说中的作用。
                
                请用中文回复，不要有任何多余的解释或说明，保持专业性和创造性。
                """;

        return String.format(CHARACTER_PROMPT,
                detail.getTitle(), detail.getStyle(), detail.getGenre(),
                String.join("，", detail.getTags()),
                detail.getSetting(), detail.getOutline(),
                characterCount);
    }

    /**
     * 构建章节大纲提示语
     *
     * @param detail
     * @param chapters
     * @return
     */
    public static String buildChapterOutlinePrompt(NovelDetail detail, int chapters) {
        final String CHAPTER_OUTLINE_PROMPT = """
                你是一位专业的小说家。请根据以下小说大纲和要求，创作各章节的大纲：
                
                小说标题：%s
                小说风格：%s
                小说类型：%s
                小说标签：%s
                背景设定：%s
                小说大纲：%s
                角色设定：%s
                
                章节数量：%d
                
                请为每一章节提供一个简短的标题和200字左右的内容概要。
                不要写具体内容(content)，只需提供章节标题和概要。
                
                请用中文回复，不要有任何多余的解释或说明，保持专业性和创造性。
                """;
        String characterInfo = detail.getCharacters().stream()
                .map(c -> String.format(
                        "姓名：%s，年龄：%s，性别：%s，外貌描述：%s，性格特点：%s，背景故事：%s，在小说中的作用：%s",
                        c.getName(), c.getAge(), c.getGender(), c.getAppearance(),
                        c.getPersonality(), c.getBackground(), c.getRole()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("无");

        return String.format(CHAPTER_OUTLINE_PROMPT,
                detail.getTitle(), detail.getStyle(), detail.getGenre(),
                detail.getTags(), detail.getSetting(),
                detail.getOutline(), characterInfo, chapters);
    }

    /**
     * 构建章节内容提示语
     *
     * @param detail
     * @param chapterIndex
     * @return
     */
    public static String buildChapterContentPrompt(NovelDetail detail, int chapterIndex) {
        final String CHAPTER_CONTENT_PROMPT = """
                你是一位专业的小说家。请根据以下小说大纲、章节大纲和要求，创作具体章节内容：
                
                小说标题：%s
                小说风格：%s
                小说类型：%s
                小说标签：%s
                背景设定：%s
                小说大纲：%s
                角色设定：%s
                
                章节序号：%d
                章节标题：%s
                章节概要：%s
                
                请将章节内容控制在5000字左右，确保内容连贯、有趣，并符合小说整体风格。
                
                请用中文回复，不要有任何多余的解释或说明，保持专业性和创造性。
                """;
        String characterInfo = detail.getCharacters().stream()
                .map(c -> String.format(
                        "姓名：%s，年龄：%s，性别：%s，外貌描述：%s，性格特点：%s，背景故事：%s，在小说中的作用：%s",
                        c.getName(), c.getAge(), c.getGender(), c.getAppearance(),
                        c.getPersonality(), c.getBackground(), c.getRole()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("无");
        NovelChapter chapter = detail.getChapters().stream()
                .filter(novelChapter -> novelChapter.getChapterNumber() == chapterIndex)
                .toList().getFirst();
        return String.format(CHAPTER_CONTENT_PROMPT,
                detail.getTitle(), detail.getStyle(), detail.getGenre(),
                detail.getTags(), detail.getSetting(),
                detail.getOutline(), characterInfo, chapterIndex,
                chapter.getTitle(), chapter.getSummary());
    }
}
