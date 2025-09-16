<template>
  <div class="novel-detail">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="$router.back()" circle>
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="novel-info">
            <h1 class="novel-title">{{ novel?.title || '加载中...' }}</h1>
            <div class="novel-meta">
              <el-tag :type="getStatusType(novel?.status)" size="small">
                {{ getStatusText(novel?.status) }}
              </el-tag>
              <span class="novel-genre">{{ novel?.genre }}</span>
              <span class="novel-date">{{ formatDate(novel?.updatedAt) }}</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="startAIWriting">
            <el-icon><Magic /></el-icon>
            AI写作助手
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要内容 -->
    <el-main class="main-content">
      <div class="content-layout">
        <!-- 左侧内容 -->
        <div class="left-content">
          <!-- 小说信息卡片 -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>小说信息</span>
                <div class="card-actions" style="display:flex;gap:8px;align-items:center">
                  <el-button @click="onReset">重置</el-button>
                  <el-button type="primary" :loading="saving.all" @click="onSaveAll">保存全部</el-button>
                  <el-button :loading="saving.summary" @click="onSaveSummary">保存简介</el-button>
                  <el-button :loading="saving.setting" @click="onSaveSetting">保存设定</el-button>
                  <el-button :loading="saving.outline" @click="onSaveOutline">保存大纲</el-button>
                </div>
              </div>
            </template>
            <el-form :model="form" ref="formRef" label-width="90px">
              <el-form-item label="标题">
                <div style="display:flex;gap:8px;width:100%">
                  <el-input v-model="form.title" placeholder="请输入标题" />
                  <el-button :disabled="!canGenTitle || loading.title" @click="onGenTitle">
                    <el-icon v-if="loading.title" class="is-loading"><Loading /></el-icon>
                    <span v-else>生成</span>
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="类型">
                <el-select v-model="form.genre" placeholder="请选择类型" style="width:100%">
                  <el-option label="科幻" value="科幻" />
                  <el-option label="奇幻" value="奇幻" />
                  <el-option label="都市" value="都市" />
                  <el-option label="历史" value="历史" />
                  <el-option label="悬疑" value="悬疑" />
                  <el-option label="言情" value="言情" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>

              <el-form-item label="风格">
                <el-select v-model="form.style" placeholder="请选择风格" style="width:100%">
                  <el-option label="轻松搞笑" value="轻松搞笑" />
                  <el-option label="严肃正剧" value="严肃正剧" />
                  <el-option label="热血励志" value="热血励志" />
                  <el-option label="悬疑惊悚" value="悬疑惊悚" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>

              <el-form-item label="标签">
                <el-input v-model="form.tags" placeholder="多个标签用中文逗号分隔" />
              </el-form-item>

              <el-form-item label="简介">
                <div style="display:flex;gap:8px;width:100%">
                  <el-input
                    v-model="form.summary"
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 10 }"
                    placeholder="作为封面预览展示"
                    class="nice-textarea summary-textarea"
                    show-word-limit
                    maxlength="800"
                  />
                  <el-button :disabled="!canGenSummary || loading.summary" @click="onGenSummary">
                    <el-icon v-if="loading.summary" class="is-loading"><Loading /></el-icon>
                    <span v-else>生成</span>
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="设定">
                <div style="display:flex;gap:8px;width:100%">
                  <el-input
                    v-model="form.setting"
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 10 }"
                    placeholder="用于AI辅助生成"
                    class="nice-textarea setting-textarea"
                  />
                  <el-button :disabled="!canGenSetting || loading.setting" @click="onGenSetting">
                    <el-icon v-if="loading.setting" class="is-loading"><Loading /></el-icon>
                    <span v-else>生成</span>
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="大纲">
                <div style="display:flex;gap:8px;width:100%">
                  <el-input
                    v-model="form.outline"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 30 }"
                    placeholder="AI生成后请确认再保存"
                    class="nice-textarea outline-textarea"
                  />
                  <el-button :disabled="loading.outline || !form.genre || !form.title" @click="onGenOutline">
                    <el-icon v-if="loading.outline" class="is-loading"><Loading /></el-icon>
                    <span v-else>生成</span>
                  </el-button>
                </div>
              </el-form-item>

              
            </el-form>
          </el-card>

          
        </div>

        <!-- 右侧内容 -->
        <div class="right-content">
          <!-- 角色列表 -->
          <el-card class="characters-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>角色列表</span>
                <el-button type="text" @click="goToCharacters">查看全部</el-button>
              </div>
            </template>

            <div v-if="characterList.length === 0" class="empty-chapters">
              <el-empty description="还没有角色" :image-size="80" />
            </div>

            <div v-else class="characters-list">
              <div
                v-for="char in characterList"
                :key="char.id"
                class="character-item"
                @click="goToCharacters"
              >
                <div class="character-info">
                  <div class="character-title">{{ char.name }}</div>
                  <div class="character-meta">
                    <span v-if="char.role">{{ char.role }}</span>
                    <span v-if="char.gender">{{ char.gender }}</span>
                    <span v-if="char.age">{{ char.age }}</span>
                  </div>
                  <div class="character-brief" v-if="char.personality || char.background">
                    {{ char.personality || char.background }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 最近章节 -->
          <el-card class="recent-chapters-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>最近章节</span>
                <el-button type="text" @click="goToChapters">
                  查看全部
                </el-button>
              </div>
            </template>
            
            <div v-if="recentChapters.length === 0" class="empty-chapters">
              <el-empty description="还没有章节" :image-size="80">
                <el-button type="primary" @click="startAIWriting">
                  开始创作
                </el-button>
              </el-empty>
            </div>
            
            <div v-else class="chapters-list">
              <div
                v-for="chapter in recentChapters"
                :key="chapter.id"
                class="chapter-item"
                @click="goToChapter(chapter.id)"
              >
                <div class="chapter-info">
                  <div class="chapter-title">{{ chapter.title }}</div>
                  <div class="chapter-meta">
                    <span class="chapter-number">第{{ chapter.chapterNumber }}章</span>
                    <span class="chapter-words">{{ chapter.wordCount || 0 }} 字</span>
                  </div>
                </div>
                <div class="chapter-status">
                  <el-tag :type="getChapterStatusType(chapter.status)" size="small">
                    {{ getChapterStatusText(chapter.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-main>

    <!-- AI写作流程对话框 -->
    <AIWritingFlow
      v-model="showAIWritingFlow"
      :novel="novel"
      @completed="handleAIWritingCompleted"
    />

    <!-- 编辑小说信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑小说信息"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="小说标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入小说标题" />
        </el-form-item>
        
        <el-form-item label="小说类型" prop="genre">
          <el-select v-model="editForm.genre" placeholder="请选择类型" style="width: 100%">
            <el-option label="科幻" value="科幻" />
            <el-option label="奇幻" value="奇幻" />
            <el-option label="都市" value="都市" />
            <el-option label="历史" value="历史" />
            <el-option label="悬疑" value="悬疑" />
            <el-option label="言情" value="言情" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="小说简介" prop="summary">
          <el-input
            v-model="editForm.summary"
            type="textarea"
            :rows="4"
            placeholder="请输入小说简介"
          />
        </el-form-item>
        
        <el-form-item label="创作灵感" prop="inspiration">
          <el-input
            v-model="editForm.inspiration"
            type="textarea"
            :rows="3"
            placeholder="请描述您的创作灵感或想法"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit" :loading="isSaving">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNovelStore } from '@/stores/novel'
import AIWritingFlow from '@/components/AIWritingFlow.vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const novelStore = useNovelStore()

// 响应式数据
const showAIWritingFlow = ref(false)
const showEditDialog = ref(false)
const isSaving = ref(false)
const editFormRef = ref()

const editForm = ref({
  title: '',
  genre: '',
  summary: '',
  inspiration: ''
})

const editRules = {
  title: [
    { required: true, message: '请输入小说标题', trigger: 'blur' },
    { min: 1, max: 50, message: '标题长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  genre: [
    { required: true, message: '请选择小说类型', trigger: 'change' }
  ]
}

// 计算属性
const novelId = computed(() => route.params.id)

const novel = computed(() => novelStore.getNovelById(novelId.value))

// 可编辑表单与状态
const formRef = ref()
const form = ref({ title: '', genre: '', style: '', tags: '', summary: '', setting: '', outline: '' })
const loading = ref({ title: false, summary: false, setting: false, outline: false })
const saving = ref({ all: false, summary: false, setting: false, outline: false })

watch(novel, (n) => {
  if (!n) return
  form.value = {
    title: n.title || '',
    genre: n.genre || '',
    style: n.style || '',
    tags: n.tags || '',
    summary: n.summary || '',
    setting: n.setting || '',
    outline: n.outline || ''
  }
}, { immediate: true })

// 生成可用条件：类型/风格/标签齐备，且至少有标题/简介/设定之一
const baseFilled = computed(() => !!(form.value.genre && form.value.style && form.value.tags))
const anyCoreFilled = computed(() => !!(form.value.title || form.value.summary || form.value.setting))
const canGenTitle = computed(() => baseFilled.value && anyCoreFilled.value)
const canGenSummary = computed(() => baseFilled.value && anyCoreFilled.value)
const canGenSetting = computed(() => baseFilled.value && anyCoreFilled.value)

const onGenTitle = async () => {
  if (!canGenTitle.value) return
  const outline = form.value.summary || form.value.setting
  if (!outline) return
  try {
    loading.value.title = true
    const { data } = await axios.post('/api/agent/novel/generateTitle', null, { params: { outline } })
    form.value.title = data || form.value.title
  } finally {
    loading.value.title = false
  }
}

const onGenSummary = async () => {
  if (!canGenSummary.value) return
  const params = { title: form.value.title || '', genre: form.value.genre, style: form.value.style, tags: form.value.tags, setting: form.value.setting || '' }
  try {
    loading.value.summary = true
    const { data } = await axios.post('/api/agent/novel/generateSummary', null, { params })
    form.value.summary = data || form.value.summary
  } finally {
    loading.value.summary = false
  }
}

const onGenSetting = async () => {
  if (!canGenSetting.value) return
  const params = { title: form.value.title || '', genre: form.value.genre, style: form.value.style, tags: form.value.tags, summary: form.value.summary || '' }
  try {
    loading.value.setting = true
    const { data } = await axios.post('/api/agent/novel/generateSetting', null, { params })
    form.value.setting = data || form.value.setting
  } finally {
    loading.value.setting = false
  }
}

const onGenOutline = async () => {
  if (!form.value.title || !form.value.genre) return
  try {
    loading.value.outline = true
    const res = await fetch(`/api/stream/novel/generateOutline`, {
      method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ novelId: String(novelId.value) })
    })
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let assembled = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      let sepIndex
      while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
        const rawEvent = buffer.slice(0, sepIndex)
        buffer = buffer.slice(sepIndex + 2)
        const lines = rawEvent.split('\n')
        const dataLines = []
        for (const line of lines) {
          if (line.startsWith('data:')) {
            dataLines.push(line.slice(5))
          }
        }
        // 在同一个event内，使用\n连接多条data，保留段落换行；event之间不额外添加换行
        if (dataLines.length > 0) assembled += dataLines.join('\n')
        form.value.outline = assembled
      }
    }
    // flush remaining buffer as a final event if present
    if (buffer.trim().length > 0) {
      const lines = buffer.split('\n')
      const dataLines = []
      for (const line of lines) {
        if (line.startsWith('data:')) {
          dataLines.push(line.slice(5))
        }
      }
      if (dataLines.length > 0) assembled += dataLines.join('\n')
      form.value.outline = assembled
    }
  } finally {
    loading.value.outline = false
  }
}

const onSaveAll = async () => {
  if (!novel.value) return
  try {
    saving.value.all = true
    await novelStore.saveNovelDetail({ id: novel.value.id, ...form.value })
    await novelStore.loadNovels(novel.value.id)
    ElMessage.success('保存成功')
  } finally {
    saving.value.all = false
  }
}

const onSaveSummary = async () => {
  if (!novel.value) return
  try {
    saving.value.summary = true
    await novelStore.saveSummary(novel.value.id, form.value.summary || '')
    await novelStore.loadNovels(novel.value.id)
    ElMessage.success('简介已保存')
  } finally {
    saving.value.summary = false
  }
}

const onSaveSetting = async () => {
  if (!novel.value) return
  try {
    saving.value.setting = true
    await novelStore.saveSetting(novel.value.id, form.value.setting || '')
    await novelStore.loadNovels(novel.value.id)
    ElMessage.success('设定已保存')
  } finally {
    saving.value.setting = false
  }
}

const onSaveOutline = async () => {
  if (!novel.value) return
  try {
    saving.value.outline = true
    await novelStore.saveOutline(novel.value.id, form.value.outline || '')
    await novelStore.loadNovels(novel.value.id)
    ElMessage.success('大纲已保存')
  } finally {
    saving.value.outline = false
  }
}

const onReset = () => {
  if (!novel.value) return
  form.value = {
    title: novel.value.title || '',
    genre: novel.value.genre || '',
    style: novel.value.style || '',
    tags: novel.value.tags || '',
    summary: novel.value.summary || '',
    setting: novel.value.setting || '',
    outline: novel.value.outline || ''
  }
}

const recentChapters = computed(() => {
  const chapters = novelStore.getChaptersByNovelId(novelId.value)
  return [...chapters]
    .sort((a, b) => (b.chapterNumber || 0) - (a.chapterNumber || 0))
    .slice(0, 5)
})

const characterList = computed(() => {
  const list = novelStore.getCharactersByNovelId(novelId.value)
  return [...list].slice(0, 6)
})

// 方法
const getStatusType = (status) => {
  const statusMap = {
    draft: 'info',
    outline: 'warning',
    characters: 'warning',
    chapters: 'warning',
    writing: 'primary',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    outline: '大纲阶段',
    characters: '角色阶段',
    chapters: '章节阶段',
    writing: '写作中',
    completed: '已完成'
  }
  return statusMap[status] || '未知'
}

const getNovelProgress = (novel) => {
  if (!novel?.progress) return 0
  const progress = novel.progress
  let completed = 0
  if (progress.outline) completed += 25
  if (progress.characters) completed += 25
  if (progress.chapters) completed += 25
  if (progress.writing) completed += 25
  return completed
}

const getChapterStatusType = (status) => {
  const statusMap = {
    draft: 'info',
    outline: 'warning',
    writing: 'primary',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

const getChapterStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    outline: '大纲',
    writing: '写作中',
    completed: '已完成'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const startAIWriting = () => {
  showAIWritingFlow.value = true
}

const editNovelInfo = () => {
  if (!novel.value) return
  
  editForm.value = {
    title: novel.value.title,
    genre: novel.value.genre,
    summary: novel.value.summary || '',
    inspiration: novel.value.inspiration || ''
  }
  showEditDialog.value = true
}

const handleSaveEdit = async () => {
  if (!editFormRef.value || !novel.value) return
  
  try {
    await editFormRef.value.validate()
    isSaving.value = true
    
    novelStore.updateNovel(novel.value.id, {
      title: editForm.value.title,
      genre: editForm.value.genre,
      summary: editForm.value.summary,
      inspiration: editForm.value.inspiration
    })
    
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    isSaving.value = false
  }
}

const goToCharacters = () => {
  router.push(`/novel/${novelId.value}/characters`)
}

const goToChapters = () => {
  router.push(`/novel/${novelId.value}/chapters`)
}

const goToWriting = () => {
  router.push(`/novel/${novelId.value}/chapters`)
}

const goToChapter = (chapterId) => {
  router.push(`/novel/${novelId.value}/chapter/${chapterId}`)
}

const handleAIWritingCompleted = (data) => {
  ElMessage.success('AI写作流程完成！')
  // 刷新页面数据
  novelStore.loadNovels()
}

// 监听路由变化
watch(novelId, (newId) => {
  if (newId) {
    // 进入详情时从后端按ID加载，填充角色与章节
    novelStore.loadNovels(newId).then(() => {
      novelStore.setCurrentNovel(novelStore.getNovelById(newId))
    })
  }
}, { immediate: true })

onMounted(() => {
  if (novelId.value) {
    novelStore.loadNovels(novelId.value).then(() => {
      novelStore.setCurrentNovel(novelStore.getNovelById(novelId.value))
    })
  }
})
</script>

<style scoped>
.novel-detail {
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
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.novel-info {
  flex: 1;
}

.novel-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.novel-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #909399;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.left-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.actions-card,
.characters-card,
.recent-chapters-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.novel-summary,
.novel-inspiration {
  margin-bottom: 20px;
}

.novel-summary h4,
.novel-inspiration h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.novel-summary p,
.novel-inspiration p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.novel-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}



.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-chapters {
  text-align: center;
  padding: 20px;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nice-textarea :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  line-height: 1.6;
  font-size: 14px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fbfbfb;
}

.summary-textarea :deep(textarea) {
  min-height: 180px;
}

.setting-textarea :deep(textarea) {
  min-height: 220px;
}

.outline-textarea :deep(textarea) {
  min-height: 360px;
}

.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.chapter-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.characters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.character-item {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
}

.character-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.character-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.character-brief {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.chapter-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
    padding: 15px 0;
  }
  
  .novel-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
