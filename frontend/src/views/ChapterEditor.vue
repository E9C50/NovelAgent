<template>
  <div class="chapter-editor">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="$router.back()" circle>
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="chapter-info">
            <h1 class="chapter-title">{{ chapter?.title || '加载中...' }}</h1>
            <div class="chapter-meta">
              <span class="chapter-number">第{{ chapter?.chapterNumber }}章</span>
              <el-tag :type="getStatusType(chapter?.status)" size="small">
                {{ getStatusText(chapter?.status) }}
              </el-tag>
              <span class="word-count">{{ chapter?.wordCount || 0 }} 字</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-button @click="saveChapter" :loading="isSaving">
            <el-icon><Document /></el-icon>
            保存
          </el-button>
          <el-button type="primary" @click="generateContent" :loading="isGenerating">
            <el-icon><Magic /></el-icon>
            AI生成
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要内容 -->
    <el-main class="main-content">
      <div class="editor-layout">
        <!-- 左侧编辑区域 -->
        <div class="editor-panel">
          <el-card class="editor-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>章节内容</span>
                <div class="editor-tools">
                  <el-button-group>
                    <el-button
                      :type="editorMode === 'edit' ? 'primary' : ''"
                      @click="editorMode = 'edit'"
                      size="small"
                    >
                      <el-icon><EditPen /></el-icon>
                      编辑模式
                    </el-button>
                    <el-button
                      :type="editorMode === 'preview' ? 'primary' : ''"
                      @click="editorMode = 'preview'"
                      size="small"
                    >
                      <el-icon><View /></el-icon>
                      预览模式
                    </el-button>
                  </el-button-group>
                </div>
              </div>
            </template>

            <!-- 编辑模式 -->
            <div v-if="editorMode === 'edit'" class="editor-content">
              <el-input
                v-model="chapterContent"
                type="textarea"
                :rows="25"
                placeholder="开始写作吧..."
                class="content-editor"
                @input="updateWordCount"
              />
            </div>

            <!-- 预览模式 -->
            <div v-else class="preview-content">
              <div class="markdown-preview" v-html="renderedContent"></div>
            </div>
          </el-card>
        </div>

        <!-- 右侧信息面板 -->
        <div class="info-panel">
          <!-- 章节信息 -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <span>章节信息</span>
            </template>
            
            <div class="chapter-details">
              <div class="detail-item">
                <span class="detail-label">章节标题:</span>
                <span class="detail-value">{{ chapter?.title }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">章节简介:</span>
                <span class="detail-value">{{ chapter?.summary }}</span>
              </div>
              <div class="detail-item" v-if="chapter?.keyEvents && chapter.keyEvents.length > 0">
                <span class="detail-label">关键事件:</span>
                <ul class="detail-list">
                  <li v-for="event in chapter.keyEvents" :key="event">{{ event }}</li>
                </ul>
              </div>
              <div class="detail-item" v-if="chapter?.characters && chapter.characters.length > 0">
                <span class="detail-label">涉及角色:</span>
                <div class="characters-tags">
                  <el-tag
                    v-for="character in chapter.characters"
                    :key="character"
                    size="small"
                    class="character-tag"
                  >
                    {{ character }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 写作统计 -->
          <el-card class="stats-card" shadow="never">
            <template #header>
              <span>写作统计</span>
            </template>
            
            <div class="writing-stats">
              <div class="stat-item">
                <div class="stat-label">当前字数</div>
                <div class="stat-value">{{ currentWordCount }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">目标字数</div>
                <div class="stat-value">{{ chapter?.wordCount || 0 }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">完成进度</div>
                <div class="stat-value">
                  <el-progress
                    :percentage="completionPercentage"
                    :stroke-width="6"
                    :show-text="false"
                  />
                  <span class="progress-text">{{ completionPercentage }}%</span>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-label">写作时间</div>
                <div class="stat-value">{{ writingTime }}</div>
              </div>
            </div>
          </el-card>

          <!-- 快速操作 -->
          <el-card class="actions-card" shadow="never">
            <template #header>
              <span>快速操作</span>
            </template>
            
            <div class="quick-actions">
              <el-button @click="insertTemplate" block>
                <el-icon><DocumentAdd /></el-icon>
                插入模板
              </el-button>
              <el-button @click="formatText" block>
                <el-icon><Magic /></el-icon>
                格式化文本
              </el-button>
              <el-button @click="exportChapter" block>
                <el-icon><Download /></el-icon>
                导出章节
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-main>

    <!-- AI生成内容对话框 -->
    <el-dialog
      v-model="showAIDialog"
      title="AI内容生成"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-generation">
        <el-form :model="aiForm" label-width="80px">
          <el-form-item label="生成类型">
            <el-radio-group v-model="aiForm.type">
              <el-radio label="full">完整章节</el-radio>
              <el-radio label="paragraph">段落续写</el-radio>
              <el-radio label="dialogue">对话生成</el-radio>
              <el-radio label="description">场景描述</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="生成提示" v-if="aiForm.type !== 'full'">
            <el-input
              v-model="aiForm.prompt"
              type="textarea"
              :rows="3"
              placeholder="请描述您想要生成的内容..."
            />
          </el-form-item>
          
          <el-form-item label="字数要求">
            <el-input-number
              v-model="aiForm.wordCount"
              :min="100"
              :max="5000"
              placeholder="字数"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        
        <div v-if="isGenerating" class="generation-progress">
          <el-progress
            :percentage="generationProgress"
            :stroke-width="8"
            :show-text="true"
            :format="(percentage) => `${percentage}%`"
          />
          <p class="progress-text">{{ currentStepText }}</p>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAIDialog = false">取消</el-button>
          <el-button type="primary" @click="handleAIGenerate" :loading="isGenerating">
            生成内容
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNovelStore } from '@/stores/novel'
import { useAIStore } from '@/stores/ai'

const route = useRoute()
const novelStore = useNovelStore()
const aiStore = useAIStore()

// 响应式数据
const editorMode = ref('edit')
const chapterContent = ref('')
const isSaving = ref(false)
const showAIDialog = ref(false)
const isGenerating = ref(false)
const startTime = ref(null)

const aiForm = ref({
  type: 'full',
  prompt: '',
  wordCount: 2000
})

// 计算属性
const novelId = computed(() => route.params.id)
const chapterId = computed(() => route.params.chapterId)

const novel = computed(() => novelStore.getNovelById(novelId.value))

const chapter = computed(() => {
  const chapters = novelStore.getChaptersByNovelId(novelId.value)
  return chapters.find(ch => ch.id === chapterId.value)
})

const currentWordCount = computed(() => {
  return chapterContent.value.length
})

const completionPercentage = computed(() => {
  const target = chapter.value?.wordCount || 0
  if (target === 0) return 0
  return Math.min(Math.round((currentWordCount.value / target) * 100), 100)
})

const writingTime = computed(() => {
  if (!startTime.value) return '0分钟'
  const minutes = Math.floor((Date.now() - startTime.value) / 60000)
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60
  return `${hours}小时${remainingMinutes}分钟`
})

const generationProgress = computed(() => aiStore.generationProgress)
const currentStepText = computed(() => aiStore.currentStep)

const renderedContent = computed(() => {
  // 简单的Markdown渲染
  return chapterContent.value
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/\n/g, '<br>')
})

// 方法
const getStatusType = (status) => {
  const statusMap = {
    draft: 'info',
    outline: 'warning',
    writing: 'primary',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    outline: '大纲',
    writing: '写作中',
    completed: '已完成'
  }
  return statusMap[status] || '未知'
}

const updateWordCount = () => {
  // 实时更新字数统计
  if (chapter.value && currentWordCount.value > 0) {
    novelStore.updateChapter(chapter.value.id, {
      wordCount: currentWordCount.value,
      status: currentWordCount.value > 0 ? 'writing' : 'draft'
    })
  }
}

const saveChapter = async () => {
  if (!chapter.value) return
  
  try {
    isSaving.value = true
    
    novelStore.updateChapter(chapter.value.id, {
      content: chapterContent.value,
      wordCount: currentWordCount.value,
      status: currentWordCount.value > 0 ? 'writing' : 'draft',
      updatedAt: new Date().toISOString()
    })
    
    ElMessage.success('保存成功！')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    isSaving.value = false
  }
}

const generateContent = () => {
  showAIDialog.value = true
}

const handleAIGenerate = async () => {
  if (!chapter.value) return
  
  try {
    isGenerating.value = true
    
    let generatedContent = ''
    
    if (aiForm.value.type === 'full') {
      // 生成完整章节
      const content = await aiStore.generateChapterContent(
        chapter.value,
        novel.value,
        novelStore.getCharactersByNovelId(novelId.value)
      )
      generatedContent = content
    } else {
      // 生成特定类型的内容
      generatedContent = await generateSpecificContent()
    }
    
    // 将生成的内容添加到现有内容中
    if (chapterContent.value) {
      chapterContent.value += '\n\n' + generatedContent
    } else {
      chapterContent.value = generatedContent
    }
    
    ElMessage.success('内容生成成功！')
    showAIDialog.value = false
  } catch (error) {
    console.error('生成失败:', error)
    ElMessage.error('生成失败，请重试')
  } finally {
    isGenerating.value = false
  }
}

const generateSpecificContent = async () => {
  // 模拟生成特定类型的内容
  const templates = {
    paragraph: `这是根据您的提示生成的段落内容。${aiForm.value.prompt} 相关内容将在这里展开...`,
    dialogue: `"这是生成的对话内容，"角色A说道。

"根据您的提示，"角色B回应道，"我们可以继续这个对话。"`,
    description: `这是根据您的提示生成的场景描述。${aiForm.value.prompt} 相关的环境描写将在这里展开，包括视觉、听觉、嗅觉等感官细节...`
  }
  
  return templates[aiForm.value.type] || '生成的内容将在这里显示...'
}

const insertTemplate = () => {
  const template = `# ${chapter.value?.title || '章节标题'}

## 章节开始

${chapter.value?.summary || '章节简介'}

---

## 正文内容

在这里开始写作...

---

## 章节结束

`
  
  if (chapterContent.value) {
    chapterContent.value += '\n\n' + template
  } else {
    chapterContent.value = template
  }
  
  ElMessage.success('模板插入成功！')
}

const formatText = () => {
  // 简单的文本格式化
  chapterContent.value = chapterContent.value
    .replace(/\n{3,}/g, '\n\n') // 移除多余的空行
    .replace(/\s{2,}/g, ' ') // 移除多余的空格
    .trim()
  
  ElMessage.success('文本格式化完成！')
}

const exportChapter = () => {
  const content = `# ${chapter.value?.title || '章节标题'}

## 章节信息
- 章节序号：第${chapter.value?.chapterNumber}章
- 字数统计：${currentWordCount.value}字
- 写作时间：${writingTime.value}
- 最后更新：${new Date().toLocaleString()}

## 章节内容

${chapterContent.value}

---
导出时间：${new Date().toLocaleString()}
`
  
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${chapter.value?.title || '章节'}.txt`
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('章节导出成功！')
}

// 监听章节变化
watch(chapter, (newChapter) => {
  if (newChapter) {
    chapterContent.value = newChapter.content || ''
    if (!startTime.value) {
      startTime.value = Date.now()
    }
  }
}, { immediate: true })

onMounted(() => {
  novelStore.loadNovels()
  startTime.value = Date.now()
})
</script>

<style scoped>
.chapter-editor {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #909399;
}

.chapter-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.editor-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.editor-panel {
  min-height: 600px;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.editor-card,
.info-card,
.stats-card,
.actions-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-tools {
  display: flex;
  gap: 10px;
}

.editor-content {
  min-height: 500px;
}

.content-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.content-editor :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  resize: none;
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
}

.preview-content {
  min-height: 500px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.markdown-preview {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3 {
  color: #303133;
  margin: 20px 0 10px 0;
}

.markdown-preview h1 {
  font-size: 24px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
}

.markdown-preview h2 {
  font-size: 20px;
}

.markdown-preview h3 {
  font-size: 16px;
}

.markdown-preview strong {
  font-weight: 600;
  color: #303133;
}

.markdown-preview em {
  font-style: italic;
  color: #606266;
}

.chapter-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  line-height: 1.5;
}

.detail-list {
  margin: 0;
  padding-left: 20px;
  color: #303133;
}

.characters-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.character-tag {
  margin: 0;
}

.writing-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-generation {
  min-height: 200px;
}

.generation-progress {
  text-align: center;
  padding: 20px;
}

.progress-text {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
    padding: 15px 0;
  }
  
  .chapter-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .editor-tools {
    justify-content: center;
  }
}
</style>
