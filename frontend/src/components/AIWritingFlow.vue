<template>
  <el-dialog
    v-model="visible"
    title="AI写作助手"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="!isGenerating"
  >
    <div class="ai-writing-flow">
      <!-- 流程步骤指示器 -->
      <div class="flow-steps">
        <div
          v-for="(step, index) in steps"
          :key="step.key"
          class="step-item"
          :class="{
            active: currentStep === step.key,
            completed: completedSteps.includes(step.key),
            disabled: !canAccessStep(step.key)
          }"
        >
          <div class="step-number">
            <el-icon v-if="completedSteps.includes(step.key)"><Check /></el-icon>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="step-content">
            <div class="step-title">{{ step.title }}</div>
            <div class="step-desc">{{ step.description }}</div>
          </div>
        </div>
      </div>

      <!-- 当前步骤内容 -->
      <div class="step-content-area">
        <!-- 步骤1: 生成小说大纲 -->
        <div v-if="currentStep === 'outline'" class="step-panel">
          <div class="step-header">
            <h3>生成小说大纲</h3>
            <p>请描述您想要创作的小说类型、主题或灵感，AI将为您生成详细的大纲</p>
          </div>
          
          <el-form :model="outlineForm" label-width="80px">
            <el-form-item label="创作灵感">
              <el-input
                v-model="outlineForm.inspiration"
                type="textarea"
                :rows="4"
                placeholder="请描述您的创作灵感、想要表达的主题或故事想法..."
              />
            </el-form-item>
            
            <el-form-item label="小说类型">
              <el-select v-model="outlineForm.genre" placeholder="请选择类型" style="width: 100%">
                <el-option label="科幻" value="科幻" />
                <el-option label="奇幻" value="奇幻" />
                <el-option label="都市" value="都市" />
                <el-option label="历史" value="历史" />
                <el-option label="悬疑" value="悬疑" />
                <el-option label="言情" value="言情" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="预计字数">
              <el-select v-model="outlineForm.targetWords" placeholder="请选择预计字数" style="width: 100%">
                <el-option label="短篇 (1-3万字)" value="short" />
                <el-option label="中篇 (3-10万字)" value="medium" />
                <el-option label="长篇 (10-30万字)" value="long" />
                <el-option label="超长篇 (30万字以上)" value="very-long" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤2: 生成角色设定 -->
        <div v-if="currentStep === 'characters'" class="step-panel">
          <div class="step-header">
            <h3>生成角色设定</h3>
            <p>基于您的小说大纲，AI将为您创建主要角色和他们的关系</p>
          </div>
          
          <div v-if="generatedCharacters.length > 0" class="characters-preview">
            <h4>生成的角色</h4>
            <div class="characters-grid">
              <el-card
                v-for="character in generatedCharacters"
                :key="character.name"
                class="character-card"
                shadow="hover"
              >
                <div class="character-header">
                  <h5>{{ character.name }}</h5>
                  <el-tag :type="getCharacterRoleType(character.role)" size="small">
                    {{ character.role }}
                  </el-tag>
                </div>
                <div class="character-info">
                  <p><strong>年龄:</strong> {{ character.age }}岁</p>
                  <p><strong>性格:</strong> {{ character.personality }}</p>
                  <p><strong>背景:</strong> {{ character.background }}</p>
                </div>
              </el-card>
            </div>
          </div>
        </div>

        <!-- 步骤3: 生成章节大纲 -->
        <div v-if="currentStep === 'chapters'" class="step-panel">
          <div class="step-header">
            <h3>生成章节大纲</h3>
            <p>基于小说大纲和角色设定，AI将为您规划各章节的内容</p>
          </div>
          
          <div v-if="generatedChapterOutlines.length > 0" class="chapters-preview">
            <h4>章节规划</h4>
            <div class="chapters-list">
              <div
                v-for="chapter in generatedChapterOutlines"
                :key="chapter.chapterNumber"
                class="chapter-outline-item"
              >
                <div class="chapter-header">
                  <span class="chapter-number">第{{ chapter.chapterNumber }}章</span>
                  <h5 class="chapter-title">{{ chapter.title }}</h5>
                </div>
                <p class="chapter-summary">{{ chapter.summary }}</p>
                <div class="chapter-meta">
                  <span class="chapter-words">预计 {{ chapter.wordCount }} 字</span>
                  <span class="chapter-characters">主要角色: {{ chapter.characters.join('、') }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤4: 生成章节内容 -->
        <div v-if="currentStep === 'content'" class="step-panel">
          <div class="step-header">
            <h3>生成章节内容</h3>
            <p>选择要生成内容的章节，AI将为您创作具体的文字内容</p>
          </div>
          
          <div class="chapter-selection">
            <h4>选择章节</h4>
            <el-select
              v-model="selectedChapterId"
              placeholder="请选择要生成内容的章节"
              style="width: 100%"
            >
              <el-option
                v-for="chapter in generatedChapterOutlines"
                :key="chapter.chapterNumber"
                :label="`第${chapter.chapterNumber}章 - ${chapter.title}`"
                :value="chapter.chapterNumber"
              />
            </el-select>
          </div>
        </div>

        <!-- 生成进度 -->
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
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!isGenerating" @click="handleCancel">
          取消
        </el-button>
        <el-button
          v-if="!isGenerating && currentStep !== 'content'"
          type="primary"
          @click="handleNext"
          :disabled="!canProceed"
        >
          {{ isLastStep ? '完成' : '下一步' }}
        </el-button>
        <el-button
          v-if="!isGenerating && currentStep === 'content'"
          type="primary"
          @click="generateChapterContent"
          :disabled="!selectedChapterId"
        >
          生成内容
        </el-button>
        <el-button
          v-if="isGenerating"
          type="primary"
          disabled
        >
          生成中...
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useNovelStore } from '@/stores/novel'
import { useAIStore } from '@/stores/ai'
import axios from 'axios'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  novel: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'completed'])

const novelStore = useNovelStore()
const aiStore = useAIStore()
const api = axios.create({ baseURL: '/api' })

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentStep = ref('outline')
const completedSteps = ref([])

const outlineForm = ref({
  inspiration: '',
  genre: '',
  targetWords: ''
})

const generatedCharacters = ref([])
const generatedChapterOutlines = ref([])
const selectedChapterId = ref(null)

const isGenerating = computed(() => aiStore.isGenerating)
const generationProgress = computed(() => aiStore.generationProgress)
const currentStepText = computed(() => aiStore.currentStep)

// 流程步骤定义
const steps = [
  {
    key: 'outline',
    title: '生成大纲',
    description: '创建小说结构和主题'
  },
  {
    key: 'characters',
    title: '角色设定',
    description: '创建主要角色'
  },
  {
    key: 'chapters',
    title: '章节规划',
    description: '规划各章节内容'
  },
  {
    key: 'content',
    title: '内容创作',
    description: '生成章节内容'
  }
]

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 'outline':
      return outlineForm.value.inspiration && outlineForm.value.genre && outlineForm.value.targetWords
    case 'characters':
      return generatedCharacters.value.length > 0
    case 'chapters':
      return generatedChapterOutlines.value.length > 0
    default:
      return false
  }
})

const isLastStep = computed(() => currentStep.value === 'content')

// 方法
const canAccessStep = (stepKey) => {
  const stepIndex = steps.findIndex(step => step.key === stepKey)
  const currentIndex = steps.findIndex(step => step.key === currentStep.value)
  return stepIndex <= currentIndex || completedSteps.value.includes(stepKey)
}

const getCharacterRoleType = (role) => {
  const roleMap = {
    '主角': 'primary',
    '女主角': 'success',
    '配角': 'info',
    '反派': 'danger',
    '导师': 'warning'
  }
  return roleMap[role] || 'info'
}

const handleNext = async () => {
  if (!canProceed.value) return

  try {
    switch (currentStep.value) {
      case 'outline':
        await generateOutline()
        break
      case 'characters':
        await generateCharacters()
        break
      case 'chapters':
        await generateChapterOutlines()
        break
    }

    // 标记当前步骤为完成
    if (!completedSteps.value.includes(currentStep.value)) {
      completedSteps.value.push(currentStep.value)
    }

    // 移动到下一步
    const currentIndex = steps.findIndex(step => step.key === currentStep.value)
    if (currentIndex < steps.length - 1) {
      currentStep.value = steps[currentIndex + 1].key
    }
  } catch (error) {
    console.error('生成失败:', error)
    ElMessage.error('生成失败，请重试')
  }
}

const generateOutline = async () => {
  const outline = await aiStore.generateNovelOutline(props.novel)
  
  // 更新小说信息
  novelStore.updateNovel(props.novel.id, {
    ...outline,
    progress: {
      ...props.novel.progress,
      outline: true
    },
    status: 'outline'
  })

  // 保存到后端
  try {
    await api.post('/novel/outline/save', outline.summary, { params: { novelId: props.novel.id }, headers: { 'Content-Type': 'text/plain;charset=UTF-8' } })
  } catch (e) {
    console.error('保存大纲失败', e)
  }
  
  ElMessage.success('小说大纲生成成功！')
}

const generateCharacters = async () => {
  const characters = await aiStore.generateCharacters(props.novel)
  
  // 保存角色到store
  characters.forEach(character => {
    novelStore.addCharacter({
      ...character,
      novelId: props.novel.id
    })
  })

  // 同步保存到后端
  try {
    const payload = characters.map(c => ({
      ...c,
      characterId: c.characterId,
      novelId: props.novel.id
    }))
    await api.post('/novel/characters/save', payload, { params: { novelId: props.novel.id } })
  } catch (e) {
    console.error('保存角色失败', e)
  }
  
  generatedCharacters.value = characters
  
  // 更新小说进度
  novelStore.updateNovel(props.novel.id, {
    progress: {
      ...props.novel.progress,
      characters: true
    },
    status: 'characters'
  })
  
  ElMessage.success('角色设定生成成功！')
}

const generateChapterOutlines = async () => {
  const chapterOutlines = await aiStore.generateChapterOutlines(props.novel, generatedCharacters.value)
  
  // 保存章节大纲到store
  chapterOutlines
    .sort((a, b) => (a.chapterNumber || 0) - (b.chapterNumber || 0))
    .forEach(chapter => {
    novelStore.addChapter({
      ...chapter,
      novelId: props.novel.id,
      status: 'outline'
    })
  })

  // 同步保存到后端
  try {
    const payload = chapterOutlines.map(ch => ({
      ...ch,
      chapterId: ch.chapterId,
      novelId: props.novel.id
    }))
    await api.post('/novel/chapters/save', payload)
  } catch (e) {
    console.error('保存章节大纲失败', e)
  }
  
  generatedChapterOutlines.value = chapterOutlines
  
  // 更新小说进度
  novelStore.updateNovel(props.novel.id, {
    progress: {
      ...props.novel.progress,
      chapters: true
    },
    status: 'chapters'
  })
  
  ElMessage.success('章节大纲生成成功！')
}

const generateChapterContent = async () => {
  if (!selectedChapterId.value) return

  try {
    const selectedChapter = generatedChapterOutlines.value.find(
      chapter => chapter.chapterNumber === selectedChapterId.value
    )
    
    if (!selectedChapter) return

    const content = await aiStore.generateChapterContent(
      selectedChapter,
      props.novel,
      generatedCharacters.value
    )
    
    // 更新章节内容
    const chapterId = novelStore.getChaptersByNovelId(props.novel.id)
      .find(chapter => chapter.chapterNumber === selectedChapterId.value)?.id
    
    if (chapterId) {
      novelStore.updateChapter(chapterId, {
        content: content,
        status: 'writing',
        wordCount: selectedChapter.wordCount
      })
      // 回写到后端（直接更新该章）
      try {
        await api.post('/novel/chapters/save', [{
          chapterId: chapterId,
          novelId: props.novel.id,
          chapterNumber: selectedChapter.chapterNumber,
          title: selectedChapter.title,
          summary: selectedChapter.summary,
          content: content
        }])
      } catch (e) {
        console.error('保存章节内容失败', e)
      }
    }
    
    // 更新小说进度
    novelStore.updateNovel(props.novel.id, {
      progress: {
        ...props.novel.progress,
        writing: true
      },
      status: 'writing',
      wordCount: (props.novel.wordCount || 0) + selectedChapter.wordCount
    })
    
    ElMessage.success('章节内容生成成功！')
    
    // 完成整个流程
    handleComplete()
  } catch (error) {
    console.error('生成章节内容失败:', error)
    ElMessage.error('生成失败，请重试')
  }
}

const handleComplete = () => {
  emit('completed', {
    novel: props.novel,
    characters: generatedCharacters.value,
    chapters: generatedChapterOutlines.value
  })
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}

// 监听对话框打开，重置状态
watch(visible, (newValue) => {
  if (newValue) {
    currentStep.value = 'outline'
    completedSteps.value = []
    generatedCharacters.value = []
    generatedChapterOutlines.value = []
    selectedChapterId.value = null
    
    // 如果小说已有大纲，从大纲步骤开始
    if (props.novel?.progress?.outline) {
      currentStep.value = 'characters'
      completedSteps.value.push('outline')
    }
    
    // 如果已有角色，从章节步骤开始
    if (props.novel?.progress?.characters) {
      currentStep.value = 'chapters'
      completedSteps.value.push('outline', 'characters')
    }
    
    // 如果已有章节，从内容步骤开始
    if (props.novel?.progress?.chapters) {
      currentStep.value = 'content'
      completedSteps.value.push('outline', 'characters', 'chapters')
    }
  }
})
</script>

<style scoped>
.ai-writing-flow {
  min-height: 500px;
}

.flow-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  position: relative;
}

.step-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -50%;
  top: 50%;
  width: 100%;
  height: 2px;
  background-color: #e4e7ed;
  transform: translateY(-50%);
  z-index: 1;
}

.step-item.completed:not(:last-child)::after {
  background-color: #67c23a;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e4e7ed;
  color: #909399;
  font-weight: 600;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  background-color: #409eff;
  color: white;
}

.step-item.completed .step-number {
  background-color: #67c23a;
  color: white;
}

.step-item.disabled .step-number {
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.step-content {
  flex: 1;
}

.step-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.step-desc {
  font-size: 12px;
  color: #909399;
}

.step-item.active .step-title {
  color: #409eff;
}

.step-item.completed .step-title {
  color: #67c23a;
}

.step-item.disabled .step-title {
  color: #c0c4cc;
}

.step-content-area {
  min-height: 400px;
}

.step-panel {
  padding: 20px;
}

.step-header {
  margin-bottom: 30px;
  text-align: center;
}

.step-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.step-header p {
  color: #606266;
  margin: 0;
}

.characters-preview,
.chapters-preview {
  margin-top: 20px;
}

.characters-preview h4,
.chapters-preview h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
}

.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.character-card {
  border-radius: 8px;
}

.character-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.character-header h5 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.character-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.character-info p:last-child {
  margin-bottom: 0;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chapter-outline-item {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.chapter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.chapter-number {
  background-color: #409eff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.chapter-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chapter-summary {
  color: #606266;
  line-height: 1.5;
  margin: 0 0 12px 0;
}

.chapter-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.chapter-selection {
  margin-top: 20px;
}

.chapter-selection h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
}

.generation-progress {
  text-align: center;
  padding: 40px 20px;
}

.progress-text {
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .flow-steps {
    flex-direction: column;
    gap: 15px;
  }
  
  .step-item:not(:last-child)::after {
    display: none;
  }
  
  .characters-grid {
    grid-template-columns: 1fr;
  }
  
  .chapter-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
