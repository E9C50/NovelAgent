<template>
  <div class="character-management">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="$router.back()" circle>
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="page-info">
            <h1 class="page-title">角色管理</h1>
            <p class="page-subtitle">{{ novel?.title }}</p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加角色
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要内容 -->
    <el-main class="main-content">
      <!-- 角色统计 -->
      <div class="character-stats">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ characters.length }}</div>
            <div class="stat-label">总角色数</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ mainCharacters.length }}</div>
            <div class="stat-label">主要角色</div>
          </div>
          <el-icon class="stat-icon"><Star /></el-icon>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ supportingCharacters.length }}</div>
            <div class="stat-label">配角</div>
          </div>
          <el-icon class="stat-icon"><UserFilled /></el-icon>
        </el-card>
      </div>

      <!-- 角色列表 -->
      <el-card class="characters-card">
        <template #header>
          <div class="card-header">
            <span>角色列表</span>
            <div class="header-actions">
              <el-input
                v-model="searchQuery"
                placeholder="搜索角色..."
                style="width: 200px"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="roleFilter"
                placeholder="筛选角色"
                style="width: 120px"
                clearable
              >
                <el-option label="全部" value="" />
                <el-option label="主角" value="主角" />
                <el-option label="女主角" value="女主角" />
                <el-option label="配角" value="配角" />
                <el-option label="反派" value="反派" />
                <el-option label="导师" value="导师" />
              </el-select>
            </div>
          </div>
        </template>

        <div v-if="filteredCharacters.length === 0" class="empty-state">
          <el-empty description="还没有角色，点击上方按钮添加第一个角色吧！">
            <el-button type="primary" @click="showAddDialog = true">
              添加角色
            </el-button>
          </el-empty>
        </div>

        <div v-else class="characters-grid">
          <el-card
            v-for="character in filteredCharacters"
            :key="character.id"
            class="character-card"
            shadow="hover"
          >
            <div class="character-header">
              <div class="character-avatar">
                <el-avatar :size="60" :src="character.avatar">
                  {{ character.name.charAt(0) }}
                </el-avatar>
              </div>
              <div class="character-info">
                <h3 class="character-name">{{ character.name }}</h3>
                <el-tag :type="getRoleType(character.role)" size="small">
                  {{ character.role }}
                </el-tag>
              </div>
              <div class="character-actions">
                <el-dropdown @command="handleCharacterAction">
                  <el-icon class="more-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'edit', character }">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', character }">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="character-details">
              <div class="detail-item">
                <span class="detail-label">年龄:</span>
                <span class="detail-value">{{ character.age }}岁</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">性别:</span>
                <span class="detail-value">{{ character.gender }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">性格:</span>
                <span class="detail-value">{{ character.personality }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">背景:</span>
                <span class="detail-value">{{ character.background }}</span>
              </div>
              <div class="detail-item" v-if="character.appearance">
                <span class="detail-label">外貌:</span>
                <span class="detail-value">{{ character.appearance }}</span>
              </div>
              <div class="detail-item" v-if="character.motivation">
                <span class="detail-label">动机:</span>
                <span class="detail-value">{{ character.motivation }}</span>
              </div>
              <div class="detail-item" v-if="character.relationships">
                <span class="detail-label">关系:</span>
                <span class="detail-value">{{ character.relationships }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>
    </el-main>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingCharacter ? '编辑角色' : '添加角色'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="characterFormRef"
        :model="characterForm"
        :rules="characterRules"
        label-width="80px"
      >
        <el-form-item label="角色姓名" prop="name">
          <el-input v-model="characterForm.name" placeholder="请输入角色姓名" />
        </el-form-item>
        
        <el-form-item label="角色定位" prop="role">
          <el-select v-model="characterForm.role" placeholder="请选择角色定位" style="width: 100%">
            <el-option label="主角" value="主角" />
            <el-option label="女主角" value="女主角" />
            <el-option label="配角" value="配角" />
            <el-option label="反派" value="反派" />
            <el-option label="导师" value="导师" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number
                v-model="characterForm.age"
                :min="1"
                :max="200"
                placeholder="年龄"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="characterForm.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="性格特点" prop="personality">
          <el-input
            v-model="characterForm.personality"
            type="textarea"
            :rows="3"
            placeholder="请描述角色的性格特点"
          />
        </el-form-item>
        
        <el-form-item label="背景设定" prop="background">
          <el-input
            v-model="characterForm.background"
            type="textarea"
            :rows="3"
            placeholder="请描述角色的背景设定"
          />
        </el-form-item>
        
        <el-form-item label="外貌描述" prop="appearance">
          <el-input
            v-model="characterForm.appearance"
            type="textarea"
            :rows="2"
            placeholder="请描述角色的外貌特征（可选）"
          />
        </el-form-item>
        
        <el-form-item label="行为动机" prop="motivation">
          <el-input
            v-model="characterForm.motivation"
            type="textarea"
            :rows="2"
            placeholder="请描述角色的行为动机（可选）"
          />
        </el-form-item>
        
        <el-form-item label="人际关系" prop="relationships">
          <el-input
            v-model="characterForm.relationships"
            type="textarea"
            :rows="2"
            placeholder="请描述角色的人际关系（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="isSaving">
            {{ editingCharacter ? '保存' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useNovelStore } from '@/stores/novel'

const route = useRoute()
const novelStore = useNovelStore()

// 响应式数据
const searchQuery = ref('')
const roleFilter = ref('')
const showAddDialog = ref(false)
const editingCharacter = ref(null)
const isSaving = ref(false)
const characterFormRef = ref()

const characterForm = ref({
  name: '',
  role: '',
  age: null,
  gender: '',
  personality: '',
  background: '',
  appearance: '',
  motivation: '',
  relationships: ''
})

const characterRules = {
  name: [
    { required: true, message: '请输入角色姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色定位', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  personality: [
    { required: true, message: '请输入性格特点', trigger: 'blur' }
  ],
  background: [
    { required: true, message: '请输入背景设定', trigger: 'blur' }
  ]
}

// 计算属性
const novelId = computed(() => route.params.id)

const novel = computed(() => novelStore.getNovelById(novelId.value))

const characters = computed(() => novelStore.getCharactersByNovelId(novelId.value))

const filteredCharacters = computed(() => {
  let filtered = characters.value

  // 按搜索关键词过滤
  if (searchQuery.value) {
    filtered = filtered.filter(character =>
      character.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      character.personality.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      character.background.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 按角色定位过滤
  if (roleFilter.value) {
    filtered = filtered.filter(character => character.role === roleFilter.value)
  }

  return filtered
})

const mainCharacters = computed(() => 
  characters.value.filter(char => ['主角', '女主角'].includes(char.role))
)

const supportingCharacters = computed(() => 
  characters.value.filter(char => !['主角', '女主角'].includes(char.role))
)

// 方法
const getRoleType = (role) => {
  const roleMap = {
    '主角': 'primary',
    '女主角': 'success',
    '配角': 'info',
    '反派': 'danger',
    '导师': 'warning',
    '其他': 'info'
  }
  return roleMap[role] || 'info'
}

const handleCharacterAction = ({ action, character }) => {
  if (action === 'edit') {
    editCharacter(character)
  } else if (action === 'delete') {
    deleteCharacter(character)
  }
}

const editCharacter = (character) => {
  editingCharacter.value = character
  characterForm.value = {
    name: character.name,
    role: character.role,
    age: character.age,
    gender: character.gender,
    personality: character.personality,
    background: character.background,
    appearance: character.appearance || '',
    motivation: character.motivation || '',
    relationships: character.relationships || ''
  }
  showAddDialog.value = true
}

const deleteCharacter = (character) => {
  ElMessageBox.confirm(
    `确定要删除角色"${character.name}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    novelStore.deleteCharacter(character.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消删除
  })
}

const handleSave = async () => {
  if (!characterFormRef.value) return
  
  try {
    await characterFormRef.value.validate()
    isSaving.value = true
    
    const characterData = {
      ...characterForm.value,
      novelId: novelId.value
    }
    
    if (editingCharacter.value) {
      // 编辑角色
      novelStore.updateCharacter(editingCharacter.value.id, characterData)
      ElMessage.success('角色更新成功！')
    } else {
      // 添加角色
      novelStore.addCharacter(characterData)
      ElMessage.success('角色添加成功！')
    }
    
    handleCancel()
  } catch (error) {
    console.error('保存角色失败:', error)
  } finally {
    isSaving.value = false
  }
}

const handleCancel = () => {
  showAddDialog.value = false
  editingCharacter.value = null
  characterForm.value = {
    name: '',
    role: '',
    age: null,
    gender: '',
    personality: '',
    background: '',
    appearance: '',
    motivation: '',
    relationships: ''
  }
}

onMounted(() => {
  novelStore.loadNovels()
})
</script>

<style scoped>
.character-management {
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

.character-stats {
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

.characters-card {
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

.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.character-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.character-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px 0 rgba(0, 0, 0, 0.15);
}

.character-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.character-avatar {
  flex-shrink: 0;
}

.character-info {
  flex: 1;
}

.character-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.character-actions {
  flex-shrink: 0;
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

.character-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  min-width: 60px;
  flex-shrink: 0;
}

.detail-value {
  color: #303133;
  line-height: 1.4;
  flex: 1;
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
  
  .character-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .characters-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
}
</style>
