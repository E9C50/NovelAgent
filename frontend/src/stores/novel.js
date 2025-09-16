import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useNovelStore = defineStore('novel', () => {
  // 状态
  const novels = ref([])
  const currentNovel = ref(null)
  const characters = ref([])
  const chapters = ref([])
  const currentChapter = ref(null)

  // 计算属性
  const getNovelById = computed(() => {
    return (id) => novels.value.find(novel => novel.id === id)
  })

  const getCharactersByNovelId = computed(() => {
    return (novelId) => characters.value.filter(char => char.novelId === novelId)
  })

  const getChaptersByNovelId = computed(() => {
    return (novelId) => chapters.value.filter(chapter => chapter.novelId === novelId)
  })

  // 操作
  const api = axios.create({ baseURL: '/api' })

  const loadAllNovels = async () => {
    const { data } = await api.get('/novel/list')
    novels.value = (data || []).map(d => ({
      id: d.novelId,
      title: d.title,
      genre: d.genre,
      style: d.style || '',
      tags: d.tags || '',
      summary: d.summary,
      setting: d.setting || '',
      outline: d.outline || '',
      inspiration: '',
      wordCount: 0,
      progress: {
        outline: !!d.outline,
        characters: Array.isArray(d.characters) && d.characters.length > 0,
        chapters: Array.isArray(d.chapters) && d.chapters.length > 0,
        writing: Array.isArray(d.chapters) && d.chapters.some(c => !!c.content)
      },
      status: 'draft',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }))
  }

  const loadNovels = async (novelId) => {
    // 从后端按需加载
    if (!novelId) return
    const { data } = await api.get('/novel/detail', { params: { novelId } })
    if (data) {
      const novel = {
        id: data.novelId,
        title: data.title,
        genre: data.genre,
        style: data.style || '',
        tags: data.tags || '',
        summary: data.summary,
        setting: data.setting || '',
        outline: data.outline || '',
        inspiration: '',
        wordCount: 0,
        progress: {
          outline: !!data.outline,
          characters: Array.isArray(data.characters) && data.characters.length > 0,
          chapters: Array.isArray(data.chapters) && data.chapters.length > 0,
          writing: Array.isArray(data.chapters) && data.chapters.some(c => !!c.content)
        },
        status: 'draft',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      const charList = (data.characters || []).map(c => ({
        id: c.characterId,
        novelId: data.novelId,
        name: c.name,
        age: c.age,
        gender: c.gender,
        appearance: c.appearance,
        personality: c.personality,
        background: c.background,
        role: c.role,
        createdAt: new Date().toISOString()
      }))
      const chapterList = (data.chapters || []).map(ch => ({
        id: ch.chapterId,
        novelId: data.novelId,
        chapterNumber: ch.chapterNumber,
        title: ch.title,
        summary: ch.summary,
        content: ch.content,
        status: ch.content ? 'writing' : 'outline',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        wordCount: ch.content ? (ch.content.length) : (ch.wordCount || 0)
      })).sort((a, b) => (a.chapterNumber || 0) - (b.chapterNumber || 0))

      // 覆盖到本地store（以单本为主）
      novels.value = [novel]
      characters.value = charList
      chapters.value = chapterList
    }
  }

  const saveNovels = () => {
    localStorage.setItem('novels', JSON.stringify(novels.value))
  }

  const saveCharacters = () => {
    localStorage.setItem('characters', JSON.stringify(characters.value))
  }

  const saveChapters = () => {
    localStorage.setItem('chapters', JSON.stringify(chapters.value))
  }

  const createNovel = async (novelData) => {
    const payload = {
      title: novelData.title,
      genre: novelData.genre,
      summary: novelData.summary || '',
      setting: novelData.setting || '',
      style: novelData.style || '',
      tags: novelData.tags || ''
    }
    const { data: novelId } = await api.post('/novel/save', payload)
    const created = {
      id: novelId,
      ...novelData,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      status: 'draft',
      progress: { outline: false, characters: false, chapters: false, writing: false }
    }
    novels.value.push(created)
    return created
  }

  const saveNovelDetail = async (novel) => {
    const payload = {
      novelId: novel.id,
      title: novel.title,
      genre: novel.genre,
      style: novel.style || '',
      tags: novel.tags || '',
      summary: novel.summary || '',
      setting: novel.setting || '',
      outline: novel.outline || ''
    }
    await api.post('/novel/save', payload)
  }

  const saveSummary = async (novelId, summary) => {
    await api.post('/novel/summary/save', summary, { params: { novelId }, headers: { 'Content-Type': 'text/plain' } })
  }

  const saveSetting = async (novelId, setting) => {
    await api.post('/novel/setting/save', setting, { params: { novelId }, headers: { 'Content-Type': 'text/plain' } })
  }

  const saveOutline = async (novelId, outline) => {
    await api.post('/novel/outline/save', outline, { params: { novelId }, headers: { 'Content-Type': 'text/plain' } })
  }

  const updateNovel = (id, updates) => {
    const index = novels.value.findIndex(novel => novel.id === id)
    if (index !== -1) {
      novels.value[index] = { ...novels.value[index], ...updates, updatedAt: new Date().toISOString() }
    }
  }

  const deleteNovel = (id) => {
    const index = novels.value.findIndex(novel => novel.id === id)
    if (index !== -1) {
      novels.value.splice(index, 1)
      // 删除相关角色和章节
      characters.value = characters.value.filter(char => char.novelId !== id)
      chapters.value = chapters.value.filter(chapter => chapter.novelId !== id)
      saveNovels()
      saveCharacters()
      saveChapters()
    }
  }

  const setCurrentNovel = (novel) => {
    currentNovel.value = novel
  }

  const addCharacter = (characterData) => {
    const newCharacter = { id: characterData.id || Date.now().toString(), ...characterData, createdAt: new Date().toISOString() }
    characters.value.push(newCharacter)
    return newCharacter
  }

  const updateCharacter = (id, updates) => {
    const index = characters.value.findIndex(char => char.id === id)
    if (index !== -1) {
      characters.value[index] = {
        ...characters.value[index],
        ...updates
      }
      saveCharacters()
    }
  }

  const deleteCharacter = (id) => {
    const index = characters.value.findIndex(char => char.id === id)
    if (index !== -1) {
      characters.value.splice(index, 1)
      saveCharacters()
    }
  }

  const addChapter = (chapterData) => {
    const newChapter = { id: chapterData.id || Date.now().toString(), ...chapterData, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), status: chapterData.status || 'draft' }
    chapters.value.push(newChapter)
    return newChapter
  }

  const updateChapter = (id, updates) => {
    const index = chapters.value.findIndex(chapter => chapter.id === id)
    if (index !== -1) {
      chapters.value[index] = {
        ...chapters.value[index],
        ...updates,
        updatedAt: new Date().toISOString()
      }
      saveChapters()
    }
  }

  const deleteChapter = (id) => {
    const index = chapters.value.findIndex(chapter => chapter.id === id)
    if (index !== -1) {
      chapters.value.splice(index, 1)
      saveChapters()
    }
  }

  const setCurrentChapter = (chapter) => {
    currentChapter.value = chapter
  }

  return {
    // 状态
    novels,
    currentNovel,
    characters,
    chapters,
    currentChapter,
    // 计算属性
    getNovelById,
    getCharactersByNovelId,
    getChaptersByNovelId,
    // 操作
    loadAllNovels,
    loadNovels,
    createNovel,
    updateNovel,
    saveNovelDetail,
    saveSummary,
    saveSetting,
    saveOutline,
    deleteNovel,
    setCurrentNovel,
    addCharacter,
    updateCharacter,
    deleteCharacter,
    addChapter,
    updateChapter,
    deleteChapter,
    setCurrentChapter
  }
})
