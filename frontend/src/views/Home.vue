<template>
  <div class="home">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <h1 class="title">
          <el-icon><EditPen /></el-icon>
          AI小说写作平台
        </h1>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建新小说
        </el-button>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ novels.length }}</div>
            <div class="stat-label">总小说数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ completedNovels }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <el-icon class="stat-icon"><Check /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ writingNovels }}</div>
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

      <!-- 小说列表 -->
      <el-card class="novels-card">
        <template #header>
          <div class="card-header">
            <span>我的小说</span>
            <el-input
              v-model="searchQuery"
              placeholder="搜索小说..."
              style="width: 300px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </template>

        <div v-if="filteredNovels.length === 0" class="empty-state">
          <el-empty description="还没有小说，点击上方按钮创建第一本小说吧！">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新小说
            </el-button>
          </el-empty>
        </div>

        <div v-else class="novels-grid">
          <el-card
            v-for="novel in filteredNovels"
            :key="novel.id"
            class="novel-card"
            shadow="hover"
            @click="goToNovelDetail(novel.id)"
          >
            <div class="novel-header">
              <h3 class="novel-title">{{ novel.title }}</h3>
              <el-dropdown @command="handleNovelAction">
                <el-icon class="more-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', novel }">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', novel }">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <div class="novel-meta">
              <el-tag :type="getStatusType(novel.status)" size="small">
                {{ getStatusText(novel.status) }}
              </el-tag>
              <span class="novel-genre">{{ novel.genre }}</span>
            </div>
            
            <p class="novel-summary">{{ novel.summary || '暂无简介' }}</p>
            
            <div class="novel-progress">
              <div class="progress-label">完成进度</div>
              <el-progress
                :percentage="getNovelProgress(novel)"
                :stroke-width="6"
                :show-text="false"
              />
              <div class="progress-steps">
                <span :class="{ active: novel.progress.outline }">大纲</span>
                <span :class="{ active: novel.progress.characters }">角色</span>
                <span :class="{ active: novel.progress.chapters }">章节</span>
                <span :class="{ active: novel.progress.writing }">写作</span>
              </div>
            </div>
            
            <div class="novel-footer">
              <span class="novel-date">{{ formatDate(novel.updatedAt) }}</span>
              <span class="novel-words">{{ novel.wordCount || 0 }} 字</span>
            </div>
          </el-card>
        </div>
      </el-card>
    </el-main>

    <!-- 创建小说对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新小说"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="小说标题" prop="title">
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="createForm.title" placeholder="可留空（至少填写 标题/简介/设定 中之一）" />
            <el-button :disabled="!canGenerateTitle || loading.title" @click="generateTitle">
              <el-icon v-if="loading.title" class="is-loading"><Loading /></el-icon>
              <span v-else>生成</span>
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="小说类型" prop="genre">
          <el-select v-model="createForm.genre" placeholder="请选择类型" style="width: 100%">
            <el-option label="科幻" value="科幻" />
            <el-option label="奇幻" value="奇幻" />
            <el-option label="都市" value="都市" />
            <el-option label="历史" value="历史" />
            <el-option label="悬疑" value="悬疑" />
            <el-option label="言情" value="言情" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="写作风格" prop="style">
          <el-select v-model="createForm.style" placeholder="请选择风格" style="width: 100%">
            <el-option label="轻松搞笑" value="轻松搞笑" />
            <el-option label="严肃正剧" value="严肃正剧" />
            <el-option label="热血励志" value="热血励志" />
            <el-option label="悬疑惊悚" value="悬疑惊悚" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input v-model="createForm.tags" placeholder="多个标签用中文逗号分隔，如：重生，搞笑，都市" />
        </el-form-item>
        
        <el-form-item label="小说简介" prop="summary">
          <div style="display:flex;gap:8px;width:100%">
            <el-input
              v-model="createForm.summary"
              type="textarea"
              :rows="4"
              placeholder="可留空（至少填写 标题/简介/设定 中之一）- 作为小说封面预览内容"
            />
            <el-button :disabled="!canGenerateSummary || loading.summary" @click="generateSummary">
              <el-icon v-if="loading.summary" class="is-loading"><Loading /></el-icon>
              <span v-else>生成</span>
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="故事设定" prop="setting">
          <div style="display:flex;gap:8px;width:100%">
            <el-input
              v-model="createForm.setting"
              type="textarea"
              :rows="3"
              placeholder="可留空（至少填写 标题/简介/设定 中之一）- 用于AI辅助生成"
            />
            <el-button :disabled="!canGenerateSetting || loading.setting" @click="generateSetting">
              <el-icon v-if="loading.setting" class="is-loading"><Loading /></el-icon>
              <span v-else>生成</span>
            </el-button>
          </div>
        </el-form-item>

      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateNovel" :loading="isCreating">
            创建小说
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useNovelStore } from '@/stores/novel'
import { useAIStore } from '@/stores/ai'
import axios from 'axios'

const router = useRouter()
const novelStore = useNovelStore()
const aiStore = useAIStore()

// 响应式数据
const searchQuery = ref('')
const showCreateDialog = ref(false)
const isCreating = ref(false)
const createFormRef = ref()

const createForm = ref({
  title: '',
  genre: '',
  style: '',
  tags: '',
  summary: '',
  setting: ''
})

const loading = ref({ title: false, summary: false, setting: false })

const createRules = {
  title: [
    { min: 0, max: 50, message: '标题长度在 0 到 50 个字符', trigger: 'blur' }
  ],
  genre: [
    { required: true, message: '请选择小说类型', trigger: 'change' }
  ],
  style: [
    { required: true, message: '请选择写作风格', trigger: 'change' }
  ],
  tags: [
    { required: true, message: '请输入标签', trigger: 'blur' }
  ]
}

// 生成可用性：当 类型/风格/标签 全部有值，且 三者(标题/简介/设定)中至少一个已填写
const baseFilled = computed(() => !!(createForm.value.genre && createForm.value.style && createForm.value.tags))
const anyCoreFilled = computed(() => !!(createForm.value.title || createForm.value.summary || createForm.value.setting))
const canGenerateTitle = computed(() => baseFilled.value && anyCoreFilled.value && !createForm.value.title)
const canGenerateSummary = computed(() => baseFilled.value && anyCoreFilled.value && !createForm.value.summary)
const canGenerateSetting = computed(() => baseFilled.value && anyCoreFilled.value && !createForm.value.setting)

const generateTitle = async () => {
  if (!canGenerateTitle.value) return
  const outline = createForm.value.summary || createForm.value.setting
  if (!outline) return
  try {
    loading.value.title = true
    const { data } = await axios.post('/api/agent/novel/generateTitle', null, { params: { outline } })
    createForm.value.title = data || createForm.value.title
  } finally {
    loading.value.title = false
  }
}

const generateSummary = async () => {
  if (!canGenerateSummary.value) return
  const params = {
    title: createForm.value.title || '',
    genre: createForm.value.genre,
    style: createForm.value.style,
    tags: createForm.value.tags,
    setting: createForm.value.setting || ''
  }
  try {
    loading.value.summary = true
    const { data } = await axios.post('/api/agent/novel/generateSummary', null, { params })
    createForm.value.summary = data || createForm.value.summary
  } finally {
    loading.value.summary = false
  }
}

const generateSetting = async () => {
  if (!canGenerateSetting.value) return
  const params = {
    title: createForm.value.title || '',
    genre: createForm.value.genre,
    style: createForm.value.style,
    tags: createForm.value.tags,
    summary: createForm.value.summary || ''
  }
  try {
    loading.value.setting = true
    const { data } = await axios.post('/api/agent/novel/generateSetting', null, { params })
    createForm.value.setting = data || createForm.value.setting
  } finally {
    loading.value.setting = false
  }
}

// 创建时如需由AI补全标题

// 计算属性
const novels = computed(() => novelStore.novels)

const filteredNovels = computed(() => {
  if (!searchQuery.value) return novels.value
  return novels.value.filter(novel =>
    novel.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    novel.genre.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (novel.summary && novel.summary.toLowerCase().includes(searchQuery.value.toLowerCase()))
  )
})

const completedNovels = computed(() => 
  novels.value.filter(novel => novel.status === 'completed').length
)

const writingNovels = computed(() => 
  novels.value.filter(novel => ['outline', 'characters', 'chapters', 'writing'].includes(novel.status)).length
)

const totalWords = computed(() => 
  novels.value.reduce((total, novel) => total + (novel.wordCount || 0), 0)
)

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
  const progress = novel.progress
  let completed = 0
  if (progress.outline) completed += 25
  if (progress.characters) completed += 25
  if (progress.chapters) completed += 25
  if (progress.writing) completed += 25
  return completed
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const goToNovelDetail = (novelId) => {
  router.push(`/novel/${novelId}`)
}

const handleNovelAction = ({ action, novel }) => {
  if (action === 'edit') {
    // 编辑小说
    createForm.value = {
      title: novel.title,
      genre: novel.genre,
      summary: novel.summary,
      inspiration: novel.inspiration || ''
    }
    showCreateDialog.value = true
  } else if (action === 'delete') {
    // 删除小说
    ElMessageBox.confirm(
      `确定要删除小说《${novel.title}》吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      novelStore.deleteNovel(novel.id)
      ElMessage.success('删除成功')
    }).catch(() => {
      // 用户取消删除
    })
  }
}

const handleCreateNovel = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    isCreating.value = true
    
    // 如标题为空，且简介或设定有值，则调用AI生成标题
    let finalTitle = createForm.value.title
    if (!finalTitle && (createForm.value.summary || createForm.value.setting)) {
      const outline = createForm.value.summary || createForm.value.setting
      const { data } = await (await import('axios')).default.post('/api/agent/novel/generateTitle', null, { params: { outline } })
      finalTitle = data || finalTitle
    }

    // 创建小说
    const novel = await novelStore.createNovel({
      title: finalTitle,
      genre: createForm.value.genre,
      style: createForm.value.style,
      tags: createForm.value.tags,
      summary: createForm.value.summary,
      setting: createForm.value.setting,
      wordCount: 0
    })
    
    ElMessage.success('小说创建成功！')
    showCreateDialog.value = false
    
    // 重置表单
    createForm.value = { title: '', genre: '', style: '', tags: '', summary: '', setting: '' }
    
    // 跳转到小说详情页
    router.push(`/novel/${novel.id}`)
  } catch (error) {
    console.error('创建小说失败:', error)
  } finally {
    isCreating.value = false
  }
}

onMounted(() => {
  novelStore.loadAllNovels()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.7;
}

.novels-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.novels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.novel-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.novel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px 0 rgba(0, 0, 0, 0.15);
}

.novel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.novel-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  line-height: 1.4;
}

.more-icon {
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.more-icon:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.novel-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.novel-genre {
  color: #909399;
  font-size: 14px;
}

.novel-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.novel-progress {
  margin-bottom: 16px;
}

.progress-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #c0c4cc;
}

.progress-steps span {
  transition: color 0.3s ease;
}

.progress-steps span.active {
  color: #409eff;
  font-weight: 600;
}

.novel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
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
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .novels-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
}
</style>
