import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useAIStore = defineStore('ai', () => {
  // 状态
  const isGenerating = ref(false)
  const generationProgress = ref(0)
  const currentStep = ref('')

  // 模拟AI生成功能
  const api = axios.create({ baseURL: '/api' })

  const generateNovelOutline = async (promptOrNovel) => {
    isGenerating.value = true
    generationProgress.value = 0
    currentStep.value = '正在分析您的需求...'
    
    // 模拟生成过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 25
    currentStep.value = '正在生成小说大纲...'
    
    await new Promise(resolve => setTimeout(resolve, 1500))
    generationProgress.value = 50
    currentStep.value = '正在优化故事结构...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 75
    currentStep.value = '正在完善细节...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 100
    currentStep.value = '生成完成！'
    
    // 调用后端SSE生成大纲（拼接为字符串）
    const novelId = typeof promptOrNovel === 'object' ? promptOrNovel.id : promptOrNovel
    const res = await fetch(`/api/stream/novel/generateOutline`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ novelId })
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
        if (dataLines.length > 0) assembled += dataLines.join('\n')
      }
    }
    if (buffer.trim().length > 0) {
      const lines = buffer.split('\n')
      const dataLines = []
      for (const line of lines) {
        if (line.startsWith('data:')) {
          dataLines.push(line.slice(5))
        }
      }
      if (dataLines.length > 0) assembled += dataLines.join('\n')
    }
    const outline = { summary: assembled }
    
    isGenerating.value = false
    return outline
  }

  const generateCharacters = async (novel) => {
    isGenerating.value = true
    generationProgress.value = 0
    currentStep.value = '正在分析故事需求...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 33
    currentStep.value = '正在创建主要角色...'
    
    await new Promise(resolve => setTimeout(resolve, 1500))
    generationProgress.value = 66
    currentStep.value = '正在完善角色关系...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 100
    currentStep.value = '角色生成完成！'
    
    const novelId = typeof novel === 'object' ? novel.id : novel
    const { data: characters } = await api.post('/agent/novel/generateCharacters', null, { params: { novelId, count: 5 } })
    
    isGenerating.value = false
    return characters
  }

  const generateChapterOutlines = async (novel, characters) => {
    isGenerating.value = true
    generationProgress.value = 0
    currentStep.value = '正在规划章节结构...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 25
    currentStep.value = '正在生成章节大纲...'
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    generationProgress.value = 50
    currentStep.value = '正在优化故事节奏...'
    
    await new Promise(resolve => setTimeout(resolve, 1500))
    generationProgress.value = 75
    currentStep.value = '正在完善细节...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 100
    currentStep.value = '章节大纲生成完成！'
    
    const novelId = typeof novel === 'object' ? novel.id : novel
    const { data: chapterOutlines } = await api.post('/agent/novel/generateChaptersOutline', null, { params: { novelId, count: 10 } })
    
    isGenerating.value = false
    return chapterOutlines
  }

  const generateChapterContent = async (chapterOutline, novel, characters) => {
    isGenerating.value = true
    generationProgress.value = 0
    currentStep.value = '正在构思章节内容...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 25
    currentStep.value = '正在生成正文内容...'
    
    await new Promise(resolve => setTimeout(resolve, 3000))
    generationProgress.value = 50
    currentStep.value = '正在优化文笔...'
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    generationProgress.value = 75
    currentStep.value = '正在完善细节...'
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    generationProgress.value = 100
    currentStep.value = '章节内容生成完成！'
    
    const novelId = typeof novel === 'object' ? novel.id : novel
    const res = await fetch(`/api/stream/novel/generateChapterContent`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ novelId, chapterIndex: String(chapterOutline.chapterNumber) })
    })
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let content = ''
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
        if (dataLines.length > 0) content += dataLines.join('\n')
      }
    }
    if (buffer.trim().length > 0) {
      const lines = buffer.split('\n')
      const dataLines = []
      for (const line of lines) {
        if (line.startsWith('data:')) {
          dataLines.push(line.slice(5))
        }
      }
      if (dataLines.length > 0) content += dataLines.join('\n')
    }
    
    isGenerating.value = false
    return content
  }

  const generateChapterText = (chapterOutline) => {
    // 根据章节大纲生成具体的文本内容
    const templates = {
      1: `林星辰站在星际探索中心的指挥室里，看着全息投影中闪烁的星系图。陈教授的声音从通讯器中传来："星辰，这次任务非常重要，你要小心。"

"我明白，教授。"林星辰回答道，他的目光坚定而专注。

艾莉亚从实验室走出来，手里拿着一个数据板："所有设备都已经检查完毕，我们可以出发了。"

两人相视一笑，这是他们第三次合作执行任务。虽然每次任务都充满未知和危险，但正是这种挑战让他们感到兴奋。

"那么，我们出发吧。"林星辰说道，走向停靠在平台上的探索飞船。

飞船缓缓升空，向着未知的星系飞去。在浩瀚的宇宙中，他们即将开始一段充满冒险的旅程。`,

      2: `在前往目标星系的途中，飞船的通讯系统突然接收到一个奇怪的信号。这个信号既不是人类发出的，也不像是任何已知的外星文明。

"这个信号很奇怪，"艾莉亚分析着数据，"它的频率和模式我从未见过。"

林星辰调整着飞船的航向："信号来源在哪里？"

"就在我们前方不远的一个小行星带中。"艾莉亚指着星图上的一个位置。

两人交换了一个眼神，都明白这意味着什么。按照原计划，他们应该避开这个区域，但现在这个神秘的信号让他们改变了主意。

"我们要去看看吗？"艾莉亚问道。

林星辰思考了片刻，然后点了点头："当然，这就是我们在这里的原因。"`,

      3: `飞船降落在神秘星球的表面，林星辰和艾莉亚走出舱门，眼前的景象让他们震惊不已。

这个星球上有着奇异的植物，它们发出微弱的光芒，在微风中轻轻摇摆。空气中弥漫着一种他们从未闻过的香味。

"这里的生态系统完全不同于我们已知的任何星球，"艾莉亚兴奋地说道，她已经开始收集样本。

突然，一个奇怪的生物从植物丛中跳了出来。它有着六条腿，身体呈半透明状，看起来既美丽又神秘。

"小心！"林星辰提醒道，但那个生物似乎并不具有攻击性，它只是好奇地观察着这两个外来者。

艾莉亚慢慢靠近，那个生物没有逃跑，反而主动接近了她。当她的手触碰到生物时，一种温暖的感觉传遍全身。

"它...它在和我交流，"艾莉亚惊讶地说道，"我能感受到它的情感。"`,

      4: `深入星球内部，林星辰和艾莉亚发现了一个巨大的古代遗迹。墙壁上刻满了神秘的符号和图案，讲述着一个古老文明的故事。

"这些符号...它们记录了什么？"林星辰问道。

艾莉亚仔细研究着这些符号："看起来这个文明曾经非常先进，他们掌握了我们无法想象的技术。"

随着他们的探索，一个惊人的真相逐渐浮出水面。这个星球曾经是一个高度发达的外星文明的居住地，但不知什么原因，他们突然消失了。

"也许他们遇到了什么灾难，"林星辰推测道。

"或者他们选择了离开，"艾莉亚补充道，"但为什么？"

就在他们思考这个问题时，遗迹深处传来了一个声音，一个古老而智慧的声音。`,

      5: `危机突然爆发了。星球上的能量场开始不稳定，整个遗迹都在震动。林星辰和艾莉亚必须尽快做出决定。

"我们该怎么办？"艾莉亚问道，她的声音中带着紧张。

林星辰看着手中的数据："如果我们不采取行动，这个星球可能会爆炸，影响到整个星系。"

陈教授的声音从通讯器中传来："孩子们，你们必须小心。这个星球的能量系统比我们想象的更加复杂。"

"教授，我们需要您的建议，"林星辰说道。

"根据我的分析，你们需要找到能量核心，并稳定它。但这非常危险。"

林星辰和艾莉亚相视一眼，都明白这意味着什么。他们可能会牺牲自己，但这是拯救无数生命的唯一方法。

"我们去做，"林星辰坚定地说道。

"我们一起，"艾莉亚握住了他的手。

他们向着能量核心的方向走去，准备面对最终的挑战。`
    }
    
    return templates[chapterOutline.chapterNumber] || "章节内容正在生成中..."
  }

  return {
    // 状态
    isGenerating,
    generationProgress,
    currentStep,
    // 操作
    generateNovelOutline,
    generateCharacters,
    generateChapterOutlines,
    generateChapterContent
  }
})
