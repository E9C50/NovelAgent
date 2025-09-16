<template>
  <div class="chapter-management">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="$router.back()" circle>
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="page-info">
            <h1 class="page-title">章节管理</h1>
            <p class="page-subtitle">{{ novel?.title }}</p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加章节
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要内容 -->
    <el-main class="main-content">
      <!-- 章节统计 -->
      <div class="chapter-stats">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ chapters.length }}</div>
            <div class="stat-label">总章节数</div>
          </div>
          <el-icon class="stat-icon"><List /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ completedChapters.length }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <el-icon class="stat-icon"><Check /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ writingChapters.length }}</div>
            <div class="stat-label">写作中</div>
          </div>
          <el-icon class="stat-icon"><Edit /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalWords }}</div>
            <div class="stat-label">总字数</div>
          </div>
          <el-icon class="stat-icon"><DocumentCopy /></el-icon>
        </el-card>
      </div>

      <!-- 章节列表 -->
      <el-card class="chapters-card">
        <template #header>
          <div class="card-header">
            <span>章节列表</span>
            <div class="header-actions">
              <el-input
                v-model="searchQuery"
                placeholder="搜索章节..."
                style="width: 200px"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="statusFilter"
                placeholder="筛选状态"
                style="width: 120px"
                clearable
              >
                <el-option label="全部" value="" />
                <el-option label="草稿" value="draft" />
                <el-option label="大纲" value="outline" />
                <el-option label="写作中" value="writing" />
                <el-option label="已完成" value="completed" />
              </el-select>
            </div>
          </div>
        </template>

        <div v-if="filteredChapters.length === 0" class="empty-state">
          <el-empty description="还没有章节，点击上方按钮添加第一个章节吧！">
            <el-button type="primary" @click="showAddDialog = true">
              添加章节
            </el-button>
          </el-empty>
        </div>

        <div v-else class="chapters-list">
          <div
            v-for="chapter in filteredChapters"
            :key="chapter.id"
            class="chapter-item"
            @click="goToChapter(chapter.id)"
          >
            <div class="chapter-header">
              <div class="chapter-number">
                第{{ chapter.chapterNumber }}章
              </div>
              <div class="chapter-info">
                <h3 class="chapter-title">{{ chapter.title }}</h3>
                <p class="chapter-summary">{{ chapter.summary }}</p>
              </div>
              <div class="chapter-status">
                <el-tag :type="getStatusType(chapter.status)" size="small">
                  {{ getStatusText(chapter.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="chapter-meta">
              <div class="meta-item">
                <el-icon><DocumentCopy /></el-icon>
                <span>{{ chapter.wordCount || 0 }} 字</span>
              </div>
              <div class="meta-item" v-if="chapter.characters && chapter.characters.length > 0">
                <el-icon><User /></el-icon>
                <span>{{ chapter.characters.join('、') }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(chapter.updatedAt) }}</span>
              </div>
            </div>
            
            <div class="chapter-actions">
              <el-button
                type="primary"
                size="small"
                @click.stop="goToChapter(chapter.id)"
              >
                <el-icon><EditPen /></el-icon>
                编辑
              </el-button>
              <el-dropdown @command="handleChapterAction" trigger="click">
                <el-button size="small" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', chapter }">
                      <el-icon><Edit /></el-icon>
                      编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'duplicate', chapter }">
                      <el-icon><CopyDocument /></el-icon>
                      复制章节
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', chapter }">
                      <el-icon><Delete /></el-icon>
                      删除章节
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-card>
    </el-main>

    <!-- 添加/编辑章节对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingChapter ? '编辑章节' : '添加章节'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="chapterFormRef"
        :model="chapterForm"
        :rules="chapterRules"
        label-width="80px"
      >
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节标题" />
        </el-form-item>
        
        <el-form-item label="章节序号" prop="chapterNumber">
          <el-input-number
            v-model="chapterForm.chapterNumber"
            :min="1"
            placeholder="章节序号"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="章节简介" prop="summary">
          <el-input
            v-model="chapterForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请描述本章节的主要内容"
          />
        </el-form-item>
        
        <el-form-item label="关键事件" prop="keyEvents">
          <el-input
            v-model="chapterForm.keyEvents"
            type="textarea"
            :rows="3"
            placeholder="请列出本章节的关键事件（每行一个）"
          />
        </el-form-item>
        
        <el-form-item label="涉及角色" prop="characters">
          <el-select
            v-model="chapterForm.characters"
            multiple
            placeholder="请选择本章节涉及的角色"
            style="width: 100%"
          >
            <el-option
              v-for="character in characters"
              :key="character.id"
              :label="character.name"
              :value="character.name"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="预计字数" prop="wordCount">
          <el-input-number
            v-model="chapterForm.wordCount"
            :min="0"
            placeholder="预计字数"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="isSaving">
            {{ editingChapter ? '保存' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useNovelStore } from '@/stores/novel'

const route = useRoute()
const router = useRouter()
const novelStore = useNovelStore()

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const showAddDialog = ref(false)
const editingChapter = ref(null)
const isSaving = ref(false)
const chapterFormRef = ref()

const chapterForm = ref({
  title: '',
  chapterNumber: null,
  summary: '',
  keyEvents: '',
  characters: [],
  wordCount: null
})

const chapterRules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 1, max: 50, message: '标题长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  chapterNumber: [
    { required: true, message: '请输入章节序号', trigger: 'blur' }
  ],
  summary: [
    { required: true, message: '请输入章节简介', trigger: 'blur' }
  ]
}

// 计算属性
const novelId = computed(() => route.params.id)

const novel = computed(() => novelStore.getNovelById(novelId.value))

const chapters = computed(() => novelStore.getChaptersByNovelId(novelId.value))

const characters = computed(() => novelStore.getCharactersByNovelId(novelId.value))

const filteredChapters = computed(() => {
  let filtered = chapters.value

  // 按搜索关键词过滤
  if (searchQuery.value) {
    filtered = filtered.filter(chapter =>
      chapter.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      chapter.summary.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 按状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(chapter => chapter.status === statusFilter.value)
  }

  // 按章节序号排序
  return filtered.sort((a, b) => a.chapterNumber - b.chapterNumber)
})

const completedChapters = computed(() => 
  chapters.value.filter(chapter => chapter.status === 'completed')
)

const writingChapters = computed(() => 
  chapters.value.filter(chapter => chapter.status === 'writing')
)

const totalWords = computed(() => 
  chapters.value.reduce((total, chapter) => total + (chapter.wordCount || 0), 0)
)

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

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const goToChapter = (chapterId) => {
  router.push(`/novel/${novelId.value}/chapter/${chapterId}`)
}

const handleChapterAction = ({ action, chapter }) => {
  if (action === 'edit') {
    editChapter(chapter)
  } else if (action === 'duplicate') {
    duplicateChapter(chapter)
  } else if (action === 'delete') {
    deleteChapter(chapter)
  }
}

const editChapter = (chapter) => {
  editingChapter.value = chapter
  chapterForm.value = {
    title: chapter.title,
    chapterNumber: chapter.chapterNumber,
    summary: chapter.summary,
    keyEvents: chapter.keyEvents ? chapter.keyEvents.join('\n') : '',
    characters: chapter.characters || [],
    wordCount: chapter.wordCount
  }
  showAddDialog.value = true
}

const duplicateChapter = (chapter) => {
  const newChapter = {
    ...chapter,
    id: undefined,
    title: `${chapter.title} (副本)`,
    chapterNumber: chapters.value.length + 1,
    status: 'draft',
    content: '',
    wordCount: 0
  }
  
  novelStore.addChapter({
    ...newChapter,
    novelId: novelId.value
  })
  
  ElMessage.success('章节复制成功！')
}

const deleteChapter = (chapter) => {
  ElMessageBox.confirm(
    `确定要删除"第${chapter.chapterNumber}章 - ${chapter.title}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    novelStore.deleteChapter(chapter.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消删除
  })
}

const handleSave = async () => {
  if (!chapterFormRef.value) return
  
  try {
    await chapterFormRef.value.validate()
    isSaving.value = true
    
    const chapterData = {
      ...chapterForm.value,
      novelId: novelId.value,
      keyEvents: chapterForm.value.keyEvents ? chapterForm.value.keyEvents.split('\n').filter(event => event.trim()) : [],
      status: editingChapter.value ? editingChapter.value.status : 'draft'
    }
    
    if (editingChapter.value) {
      // 编辑章节
      novelStore.updateChapter(editingChapter.value.id, chapterData)
      ElMessage.success('章节更新成功！')
    } else {
      // 添加章节
      novelStore.addChapter(chapterData)
      ElMessage.success('章节添加成功！')
    }
    
    handleCancel()
  } catch (error) {
    console.error('保存章节失败:', error)
  } finally {
    isSaving.value = false
  }
}

const handleCancel = () => {
  showAddDialog.value = false
  editingChapter.value = null
  chapterForm.value = {
    title: '',
    chapterNumber: null,
    summary: '',
    keyEvents: '',
    characters: [],
    wordCount: null
  }
}

onMounted(() => {
  novelStore.loadNovels()
})
</script>

<style scoped>
.chapter-management {
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

.page-info {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.chapter-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-icon {
  font-size: 36px;
  opacity: 0.7;
}

.chapters-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chapter-item {
  display: flex;
  flex-direction: column;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chapter-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.chapter-header {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  margin-bottom: 15px;
}

.chapter-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.chapter-summary {
  color: #606266;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.chapter-status {
  flex-shrink: 0;
}

.chapter-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chapter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 15px;
    padding: 15px 0;
  }
  
  .chapter-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .chapter-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .chapter-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .chapter-actions {
    justify-content: space-between;
  }
}
</style>
