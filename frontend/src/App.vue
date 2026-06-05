<template>
  <main class="tree-explorer" :class="{ 'modal-open': detailOpen }">
    <div class="tree-atmosphere" aria-hidden="true"></div>

    <header class="tree-topbar">
      <div class="brand-cluster">
        <div>
          <span>Graduate Study Console</span>
          <strong>研途树谱</strong>
        </div>
      </div>

      <label class="command-search">
        <Search :size="18" />
        <input v-model="keyword" type="search" placeholder="搜索节点、感受、标签与薄弱点" />
      </label>

      <div class="top-actions">
        <span v-if="currentUser" class="user-chip">
          <Shield :size="15" />
          <b>{{ currentUser.displayName }}</b>
          <i>{{ roleText(currentUser.role) }}</i>
        </span>
        <button class="text-action tool-action" type="button" title="让当前知识树回到画布中心" @click="fitView">
          <LocateFixed :size="16" />
          <span>归中视图</span>
        </button>
        <button
          class="text-action tool-action"
          type="button"
          title="把官方大纲同步到当前这棵树里"
          :disabled="!currentUser || !activeTreeId || loading"
          @click="handleImport"
        >
          <RefreshCw :size="16" />
          <span>同步大纲</span>
        </button>
        <button
          class="text-action tool-action tool-action-accent"
          type="button"
          :disabled="!currentUser || !selectedNode || loading"
          title="为当前节点增加一次复习记录，用来统计复习频次"
          @click="handleReview"
        >
          <CheckCircle2 :size="16" />
          <span>记录一次复习</span>
        </button>
        <button v-if="currentUser" class="icon-action" type="button" title="退出登录" @click="handleLogout">
          <LogOut :size="18" />
        </button>
      </div>
    </header>

    <div v-if="pageError" class="system-toast">
      {{ pageError }}
    </div>
    <div v-if="pageSuccess" class="system-toast success">
      {{ pageSuccess }}
    </div>

    <aside class="tree-index">
      <section class="student-mark nav-block block-intro">
        <p>Study Workspace</p>
        <strong>考研辅助系统</strong>
        <span>用知识树组织专业课、数学、英语与政治的复习脉络。</span>
      </section>

      <section class="tree-manager nav-block block-group">
        <div class="section-heading">
          <span>树分组</span>
          <button class="line-action" type="button" :disabled="createGroupLoading || !groupDraft.name.trim()" @click="handleCreateGroup">
            新建分组
          </button>
        </div>
        <div class="group-strip">
          <button type="button" :class="{ active: activeGroupId === null }" @click="handleSelectGroup(null)">全部</button>
          <button
            v-for="group in studyGroups"
            :key="group.id"
            type="button"
            :class="{ active: activeGroupId === group.id }"
            @click="handleSelectGroup(group.id)"
          >
            {{ group.name }}
          </button>
        </div>
        <label class="field-line compact">
          <span>分组名</span>
          <input v-model="groupDraft.name" type="text" placeholder="例如：公共课 / 专业课" />
        </label>
      </section>

      <section class="tree-manager nav-block block-tree">
        <div class="section-heading">
          <span>考研树</span>
          <button class="line-action" type="button" :disabled="createTreeLoading || !treeDraft.name.trim()" @click="handleCreateTree">
            新建树
          </button>
        </div>
        <div class="tree-selector">
          <button
            v-for="item in filteredStudyTrees"
            :key="item.id"
            type="button"
            :class="{ active: item.id === activeTreeId }"
            @click="handleSelectTree(item.id)"
          >
            <span class="tree-selector-main">
              <strong>{{ item.name }}</strong>
              <small>{{ groupNameFor(item.groupId) }}</small>
            </span>
            <i>{{ item.id === activeTreeId ? '当前' : '' }}</i>
          </button>
          <div v-if="!filteredStudyTrees.length" class="list-empty">
            还没有树，先新建一棵。
          </div>
        </div>
        <label class="field-line compact">
          <span>树名称</span>
          <input v-model="treeDraft.name" type="text" placeholder="例如：408 / 高等数学 / 英语一" />
        </label>
        <label class="field-line compact">
          <span>说明</span>
          <input v-model="treeDraft.description" type="text" placeholder="可选，写这棵树的用途" />
        </label>
      </section>

      <nav class="view-switch nav-block block-mode" aria-label="知识树视图">
        <button type="button" :class="{ active: mode === 'all' }" @click="mode = 'all'">
          <GitBranch :size="17" />
          整树
        </button>
        <button type="button" :class="{ active: mode === 'weak' }" @click="mode = 'weak'">
          <Target :size="17" />
          薄弱
        </button>
        <button type="button" :class="{ active: mode === 'mastered' }" @click="mode = 'mastered'">
          <BookOpenCheck :size="17" />
          已掌握
        </button>
      </nav>

      <section class="ring-readout nav-block block-stats">
        <p>Growth Rings</p>
        <div>
          <span>节点 <b>{{ stats.total }}</b></span>
          <span>学习中 <b>{{ stats.inProgress }}</b></span>
          <span class="danger">薄弱 <b>{{ stats.weak }}</b></span>
          <span>掌握 <b>{{ stats.mastered }}</b></span>
        </div>
      </section>

      <section class="root-builder nav-block block-root-builder">
        <div class="section-heading">
          <span>树根扩展</span>
          <button
            class="line-action primary"
            type="button"
            :disabled="createNodeLoading || !rootDraft.title.trim() || !currentUser || !activeTreeId"
            @click="handleCreateRootNode"
          >
            新增一级节点
          </button>
        </div>
        <label class="field-line compact">
          <span>一级节点标题</span>
          <input v-model="rootDraft.title" type="text" placeholder="例如：高等数学" />
        </label>
        <label class="field-line compact">
          <span>层级标签</span>
          <input v-model="rootDraft.label" type="text" placeholder="默认：第 1 级" />
        </label>
      </section>

      <section class="root-thread nav-block block-root-list">
        <p>当前树 · 一级节点</p>
        <button
          v-for="root in tree"
          :key="root.id"
          type="button"
          :class="{ active: root.id === selectedRootId }"
          @click="jumpToNode(root)"
        >
          <i></i>
          <span class="root-entry">
            <strong>{{ root.title }}</strong>
            <small>{{ root.label }} · {{ statusText(root.status) }}</small>
          </span>
          <b>{{ countDescendants(root) }}</b>
        </button>
        <div v-if="!tree.length" class="list-empty">
          当前树还没有一级节点。
        </div>
      </section>
    </aside>

    <section
      ref="stageRef"
      class="tree-stage"
      :class="{ dragging: dragState.dragging }"
      @contextmenu.prevent
      @pointerdown="startPan"
      @pointermove="movePan"
      @pointerup="endPan"
      @pointercancel="endPan"
      @pointerleave="endPan"
      @wheel="handleWheel"
    >
      <div class="stage-title">
        <p>Graduate Study Map</p>
        <h2>{{ currentTree?.name || '知识树画布' }}</h2>
        <span>
          左键与右键都可拖动画布，滚轮缩放。点击节点后使用浮层编辑全部信息。
        </span>
      </div>

      <div class="zoom-strip">
        <button type="button" title="缩小" @click.stop="zoomBy(0.86)">
          <Minus :size="17" />
        </button>
        <button type="button" title="适配视图" @click.stop="fitView">
          <LocateFixed :size="17" />
        </button>
        <button type="button" title="放大" @click.stop="zoomBy(1.14)">
          <Plus :size="17" />
        </button>
        <span>{{ Math.round(viewport.scale * 100) }}%</span>
      </div>

      <div class="status-legend">
        <span><i class="idle"></i>未开始</span>
        <span><i class="active"></i>学习中</span>
        <span><i class="mastered"></i>已掌握</span>
        <span><i class="weak"></i>薄弱</span>
      </div>

      <div class="world-viewport">
        <div class="tree-world" :style="worldStyle">
          <svg
            class="world-svg"
            :viewBox="`0 0 ${canvasSize.width} ${canvasSize.height}`"
            :width="canvasSize.width"
            :height="canvasSize.height"
            aria-hidden="true"
          >
            <path class="trunk-line" :d="treeLayout.trunkPath" />
            <path
              v-for="link in treeLayout.links"
              :key="link.id"
              class="branch-line"
              :class="{
                weak: link.weak,
                active: activeLinkIds.has(link.id),
                muted: focusedVisualId && !activeLinkIds.has(link.id)
              }"
              :d="link.path"
            />
            <path
              v-for="connection in visibleConnections"
              :key="`connection-${connection.id}`"
              :d="connection.path"
              fill="none"
              stroke="#ffd36b"
              stroke-width="3"
              stroke-linecap="round"
              stroke-dasharray="10 8"
              opacity="0.82"
            />
            <circle
              v-for="ring in treeLayout.rings"
              :key="ring.id"
              class="growth-ring"
              :cx="ring.x"
              :cy="ring.y"
              :r="ring.r"
            />
          </svg>

          <button
            v-for="item in treeLayout.nodes"
            :key="item.id"
            class="tree-node"
            :class="[
              item.statusClass,
              `depth-${Math.min(item.depth, 5)}`,
              {
                selected: selectedNode?.id === item.node?.id,
                virtual: item.kind === 'virtual',
                weak: item.weakScore >= 70,
                focused: focusedVisualId === item.id,
                related: focusedVisualId && activeVisualIds.has(item.id),
                muted: focusedVisualId && !activeVisualIds.has(item.id)
              }
            ]"
            type="button"
            :style="{ left: `${item.x}px`, top: `${item.y}px` }"
            @pointerenter="hoveredVisualId = item.id"
            @pointerleave="hoveredVisualId = null"
            @focus="hoveredVisualId = item.id"
            @blur="hoveredVisualId = null"
            @click.stop="item.node ? handleNodeClick(item.node) : fitView()"
          >
            <span class="node-halo"></span>
            <span class="node-core">
              <component :is="nodeIconFor(item)" :size="item.kind === 'virtual' ? 26 : item.depth <= 1 ? 22 : 18" />
            </span>
            <span class="node-label">
              <b>{{ item.title }}</b>
              <small>{{ item.label }}</small>
            </span>
            <span class="node-tooltip" role="tooltip">
              <strong>{{ item.title }}</strong>
              <em>{{ item.label }}</em>
              <span v-if="item.kind === 'real'">
                弱点 {{ item.weakScore }} · 复习 {{ item.reviewCount }} · 子节点 {{ item.childCount }}
              </span>
              <span v-else>这是当前树根，点击可归中视图。</span>
            </span>
          </button>

          <div v-if="treeLayout.nodes.length <= 1" class="empty-tree">
            <Network :size="44" />
            <span>当前树还没有节点，从左侧先添加一层。</span>
          </div>
        </div>
      </div>
    </section>

    <Transition name="node-modal">
      <section
        v-if="detailOpen && selectedNode"
        class="node-modal-layer"
        aria-modal="true"
        role="dialog"
        @click.self="closeNodeDetail"
      >
        <article class="node-memory-capsule">
          <button class="modal-close" type="button" title="关闭" @click="closeNodeDetail">
            <X :size="20" />
          </button>

          <header class="capsule-head">
            <div class="capsule-kicker">
              <component :is="selectedNodeIcon" :size="16" />
              <p>Node Entry</p>
            </div>
            <h3>{{ selectedNode.title }}</h3>
          </header>

          <div class="capsule-meta">
            <i>{{ selectedNode.label }}</i>
            <i>第 {{ selectedNode.levelNo }} 级</i>
            <i>{{ selectedNode.customNode ? '自定义节点' : '官方节点' }}</i>
            <i>{{ selectedNode.reviewCount }} 次复习</i>
          </div>

          <div class="capsule-scroll">
            <section class="sheet-section section-status">
              <div class="status-switch" role="tablist" aria-label="学习状态">
                <button
                  v-for="item in statusOptions"
                  :key="item.value"
                  type="button"
                  :class="[
                    `status-${item.value.toLowerCase().replace('_', '-')}`,
                    { active: draft.status === item.value }
                  ]"
                  @click="draft.status = item.value"
                >
                  {{ item.label }}
                </button>
              </div>
            </section>

            <section class="sheet-section">
              <div class="section-heading">
                <span>节点标签</span>
                <button class="line-action" type="button" :disabled="tagSaving || !tagDraft.name.trim()" @click="handleCreateTag">
                  新增标签
                </button>
              </div>
              <div style="display: grid; gap: 8px; margin-bottom: 10px;">
                <label class="field-line compact">
                  <span>新标签名</span>
                  <input v-model="tagDraft.name" type="text" placeholder="例如：错题 / 高频 / 二刷" />
                </label>
                <label class="field-line compact">
                  <span>颜色</span>
                  <input v-model="tagDraft.color" type="color" style="width: 54px; min-height: 34px; padding: 2px;" />
                </label>
              </div>
              <div class="tag-flow">
                <span
                  v-for="tag in selectedNode.tags"
                  :key="tag.id"
                  class="tag-line"
                  :style="{ '--tag-color': tag.color }"
                  style="display: inline-grid; grid-template-columns: auto auto auto; gap: 6px; align-items: center;"
                >
                  <input
                    v-model="tagEditDraft[tag.id].name"
                    type="text"
                    aria-label="标签名"
                    style="width: 86px; min-height: 26px; color: inherit; background: transparent; border: 0; border-bottom: 1px solid currentColor;"
                  />
                  <input
                    v-model="tagEditDraft[tag.id].color"
                    type="color"
                    aria-label="标签颜色"
                    style="width: 26px; height: 24px; padding: 0; border: 0; background: transparent;"
                  />
                  <span style="display: inline-flex; gap: 4px;">
                    <button class="line-action" type="button" :disabled="tagSaving" @click="handleUpdateTag(tag)">改</button>
                    <button class="line-action" type="button" :disabled="tagSaving" @click="handleDeleteTag(tag)">删</button>
                  </span>
                </span>
              </div>
              <p v-if="!selectedNode.tags?.length" class="metric-note">还没有标签，可以先加一个用于错题、重点或复盘分类。</p>
            </section>

            <section class="sheet-section">
              <div class="section-heading">
                <span>节点连接</span>
                <button class="line-action" type="button" :disabled="connectionSaving" @click="beginConnectionFromSelected">
                  从此节点发起
                </button>
              </div>
              <label class="field-line compact">
                <span>关系类型</span>
                <input v-model="connectionDraft.relationType" type="text" placeholder="RELATED / DEPENDS_ON / SIMILAR" />
              </label>
              <label class="field-line compact">
                <span>连接说明</span>
                <input v-model="connectionDraft.label" type="text" placeholder="例如：前置知识 / 易混淆 / 题型关联" />
              </label>
              <p v-if="connectionSourceNode" class="metric-note">
                正在从“{{ connectionSourceNode.title }}”发起连接，点击画布上的另一个节点即可创建。
              </p>
              <div style="display: grid; gap: 8px;">
                <div
                  v-for="connection in selectedNodeConnections"
                  :key="connection.id"
                  style="display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 8px; align-items: center;"
                >
                  <span class="metric-note">
                    {{ nodeTitle(connection.sourceNodeId) }} → {{ nodeTitle(connection.targetNodeId) }}
                    · {{ connection.relationType }}{{ connection.label ? ` · ${connection.label}` : '' }}
                  </span>
                  <span style="display: inline-flex; gap: 4px;">
                    <button class="line-action" type="button" :disabled="connectionSaving" @click="startEditConnection(connection)">编辑</button>
                    <button class="line-action" type="button" :disabled="connectionSaving" @click="handleDeleteConnection(connection.id)">删除</button>
                  </span>
                </div>
              </div>
              <button
                v-if="editingConnectionId"
                class="line-action primary"
                type="button"
                :disabled="connectionSaving"
                @click="handleUpdateConnection"
              >
                保存连接修改
              </button>
            </section>

            <section class="sheet-section section-writing">
              <label class="field-line">
                <span>节点标题</span>
                <input v-model="draft.title" type="text" />
              </label>

              <label class="field-line">
                <span>自定义层级标签</span>
                <input v-model="draft.label" type="text" />
              </label>

              <label class="field-line">
                <span>大白话理解</span>
                <textarea v-model="draft.plainUnderstanding" rows="7" />
              </label>

              <label class="field-line">
                <span>今日复习感受</span>
                <textarea v-model="draft.todayFeeling" rows="7" />
              </label>
            </section>

            <section class="sheet-section sheet-secondary section-metric">
              <div class="weak-thread">
                <div>
                  <span>薄弱度评估</span>
                  <p class="metric-note">结合状态、复习次数和近期感受估算，数值越高越需要回头补。</p>
                </div>
                <strong :class="weakScoreTone">{{ Math.round(selectedNode.weakScore) }}</strong>
              </div>
              <i class="weak-meter" :class="weakScoreTone"><b :style="{ width: `${Math.min(100, selectedNode.weakScore)}%` }"></b></i>
            </section>

            <section class="sheet-section section-branch">
              <div class="section-heading">
                <span>新增下级节点</span>
                <button class="line-action" type="button" :disabled="createNodeLoading || !childDraft.title.trim()" @click="handleCreateChild">
                  新增
                </button>
              </div>
              <label class="field-line compact">
                <span>下级标题</span>
                <input v-model="childDraft.title" type="text" placeholder="例如：页表与地址转换" />
              </label>
              <label class="field-line compact">
                <span>层级标签</span>
                <input v-model="childDraft.label" type="text" :placeholder="`默认：第 ${selectedNode.levelNo + 1} 级`" />
              </label>
            </section>

            <section class="sheet-section section-icon">
              <div class="section-heading">
                <span>节点图案</span>
                <button class="line-action" type="button" :class="{ active: !selectedCustomIconKey }" @click="clearNodeIcon">
                  默认
                </button>
              </div>
              <div class="icon-grid" role="listbox" aria-label="节点图案库">
                <button
                  v-for="icon in iconLibrary"
                  :key="icon.key"
                  class="icon-choice"
                  :class="{ active: selectedCustomIconKey === icon.key }"
                  type="button"
                  :disabled="iconSaving"
                  :title="icon.label"
                  :aria-label="`选择${icon.label}图案`"
                  @click="chooseNodeIcon(icon.key)"
                >
                  <component :is="icon.component" :size="20" />
                  <span class="sr-only">{{ icon.label }}</span>
                </button>
              </div>
            </section>

            <section class="sheet-section section-agent">
              <button class="line-action" type="button" :disabled="agentLoading" @click="handleAskAgent">
                <Sparkles :size="16" />
                {{ agentLoading ? '分析中' : 'AI 助读（暂缓迭代）' }}
              </button>
            </section>

            <section v-if="agentAnswer" class="sheet-section">
              <div class="agent-answer">
                <p>{{ agentAnswer.answer }}</p>
                <strong>下一步</strong>
                <span>{{ agentAnswer.nextAction }}</span>
                <strong>疑似弱点</strong>
                <ul>
                  <li v-for="hint in agentAnswer.weaknessHints" :key="hint">{{ hint }}</li>
                </ul>
              </div>
            </section>
          </div>

          <footer class="capsule-actions">
            <button class="line-action" type="button" @click="resetDraft">还原</button>
            <button class="line-action primary" type="button" @click="handleSave">保存节点</button>
          </footer>
        </article>
      </section>
    </Transition>

    <Transition name="node-modal">
      <section v-if="!currentUser" class="auth-layer" aria-modal="true" role="dialog">
        <form class="auth-panel" @submit.prevent="handleLogin">
          <div class="auth-symbol">
            <Network :size="28" />
          </div>
          <p>Access Console</p>
          <h2>登录知识树</h2>
          <label class="field-line">
            <span>账号</span>
            <input v-model="loginForm.username" type="text" autocomplete="username" />
          </label>
          <label class="field-line">
            <span>密码</span>
            <input v-model="loginForm.password" type="password" autocomplete="current-password" />
          </label>
          <button class="solid-action" type="submit" :disabled="authLoading">
            <Shield :size="17" />
            {{ authLoading ? '登录中' : '进入系统' }}
          </button>
          <span class="auth-hint">candidate / user123 · root / admin123 · community / admin123</span>
          <strong v-if="authError" class="auth-error">{{ authError }}</strong>
        </form>
      </section>
    </Transition>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch, type Component } from 'vue'
import {
  Atom,
  Binary,
  BookOpen,
  BookOpenCheck,
  Boxes,
  BrainCircuit,
  Cable,
  CheckCircle2,
  CircuitBoard,
  Circle,
  Code,
  Cpu,
  Database,
  GitBranch,
  Layers3,
  LocateFixed,
  LogOut,
  MemoryStick,
  Minus,
  Network,
  Plus,
  RefreshCw,
  Route,
  Search,
  Shield,
  Sigma,
  Sparkles,
  Target,
  Terminal,
  Workflow,
  X
} from 'lucide-vue-next'
import {
  ApiError,
  askAgent,
  clearAuthSession,
  createLearningConnection,
  createLearningNode,
  createLearningNodeTag,
  createStudyGroup,
  createStudyTree,
  deleteLearningConnection,
  deleteLearningNodeTag,
  getAuthSession,
  getCurrentUser,
  getLearningConnections,
  getLearningTree,
  getStudyGroups,
  getStudyTrees,
  importOfficialTree,
  login,
  reviewLearningNode,
  updateLearningConnection,
  updateLearningNode,
  updateLearningNodeTag,
  updateLearningNodeIcon
} from './api'
import type {
  AgentAssistResponse,
  AuthUser,
  LearningNode,
  LearningNodeConnection,
  LearningNodeTag,
  LearningStatus,
  RoleCode,
  StudyGroup,
  StudyTree
} from './types'

interface VisualNode {
  id: string
  kind: 'virtual' | 'real'
  node?: LearningNode
  title: string
  label: string
  depth: number
  x: number
  y: number
  childCount: number
  reviewCount: number
  weakScore: number
  statusClass: string
}

interface VisualLink {
  id: string
  path: string
  weak: boolean
  parentId: string
  childId: string
}

interface GrowthRing {
  id: string
  x: number
  y: number
  r: number
}

interface IconOption {
  key: string
  label: string
  component: Component
}

const NODE_ICON_STORAGE_KEY = 'ky-examination.node-icons'
const savedSession = getAuthSession()

const tree = ref<LearningNode[]>([])
const studyGroups = ref<StudyGroup[]>([])
const studyTrees = ref<StudyTree[]>([])
const connections = ref<LearningNodeConnection[]>([])
const selectedNode = ref<LearningNode | null>(null)
const currentUser = ref<AuthUser | null>(savedSession?.user || null)
const activeGroupId = ref<number | null>(null)
const activeTreeId = ref<number | null>(null)
const connectionSourceNodeId = ref<number | null>(null)
const editingConnectionId = ref<number | null>(null)

const mode = ref<'all' | 'weak' | 'mastered'>('all')
const keyword = ref('')
const loading = ref(false)
const authLoading = ref(false)
const agentLoading = ref(false)
const createNodeLoading = ref(false)
const createGroupLoading = ref(false)
const createTreeLoading = ref(false)
const tagSaving = ref(false)
const connectionSaving = ref(false)
const iconSaving = ref(false)
const detailOpen = ref(false)
const hoveredVisualId = ref<string | null>(null)
const agentAnswer = ref<AgentAssistResponse | null>(null)
const stageRef = ref<HTMLElement | null>(null)
const authError = ref('')
const pageError = ref('')
const pageSuccess = ref('')
const nodeIconMap = ref<Record<number, string>>(loadNodeIconMap())
let successTimer: ReturnType<typeof setTimeout> | null = null

const viewport = reactive({
  x: 0,
  y: 0,
  scale: 0.58
})

const dragState = reactive({
  dragging: false,
  startX: 0,
  startY: 0,
  originX: 0,
  originY: 0
})

const loginForm = reactive({
  username: savedSession?.user.username || 'candidate',
  password: savedSession ? '' : 'user123'
})

const draft = reactive({
  title: '',
  label: '',
  status: 'NOT_STARTED' as LearningStatus,
  plainUnderstanding: '',
  todayFeeling: ''
})

const childDraft = reactive({
  title: '',
  label: ''
})

const rootDraft = reactive({
  title: '',
  label: ''
})

const groupDraft = reactive({
  name: ''
})

const treeDraft = reactive({
  name: '',
  description: ''
})

const tagDraft = reactive({
  name: '',
  color: '#6bfb9a'
})

const tagEditDraft = reactive<Record<number, { name: string; color: string }>>({})

const connectionDraft = reactive({
  relationType: 'RELATED',
  label: ''
})

const statusOptions: Array<{ value: LearningStatus; label: string }> = [
  { value: 'NOT_STARTED', label: '未开始' },
  { value: 'IN_PROGRESS', label: '学习中' },
  { value: 'MASTERED', label: '已掌握' }
]

const iconLibrary: IconOption[] = [
  { key: 'brain', label: '理解中枢', component: BrainCircuit },
  { key: 'branch', label: '知识分支', component: GitBranch },
  { key: 'layers', label: '层级结构', component: Layers3 },
  { key: 'book', label: '教材章节', component: BookOpen },
  { key: 'cpu', label: '组成原理', component: Cpu },
  { key: 'database', label: '数据沉淀', component: Database },
  { key: 'binary', label: '二进制', component: Binary },
  { key: 'circuit', label: '电路逻辑', component: CircuitBoard },
  { key: 'terminal', label: '命令行', component: Terminal },
  { key: 'code', label: '代码片段', component: Code },
  { key: 'workflow', label: '流程推演', component: Workflow },
  { key: 'memory', label: '长期记忆', component: MemoryStick },
  { key: 'route', label: '路径规划', component: Route },
  { key: 'network', label: '网络协议', component: Network },
  { key: 'shield', label: '安全边界', component: Shield },
  { key: 'sigma', label: '数学推导', component: Sigma },
  { key: 'boxes', label: '模块组合', component: Boxes },
  { key: 'cable', label: '连接关系', component: Cable },
  { key: 'atom', label: '原子知识', component: Atom },
  { key: 'circle', label: '普通节点', component: Circle }
]

const iconRegistry = iconLibrary.reduce<Record<string, Component>>((registry, item) => {
  registry[item.key] = item.component
  return registry
}, {})

const filteredStudyTrees = computed(() => {
  if (activeGroupId.value === null) {
    return studyTrees.value
  }
  return studyTrees.value.filter((item) => item.groupId === activeGroupId.value)
})

const currentTree = computed(() => studyTrees.value.find((item) => item.id === activeTreeId.value) || null)
const flatNodes = computed(() => flatten(tree.value))
const displayTree = computed(() => filterTree(tree.value))
const selectedRootId = computed(() => findRootId(selectedNode.value))
const treeCanvasTitle = computed(() => currentTree.value?.name || '当前知识树')

const canvasSize = computed(() => {
  const leaves = Math.max(4, countLeaves(displayTree.value))
  const depth = Math.max(3, getMaxDepth(displayTree.value) + 2)
  return {
    width: Math.max(2200, leaves * 280 + 520),
    height: Math.max(1500, depth * 290 + 420)
  }
})

const treeLayout = computed(() => buildOrganicLayout(displayTree.value, canvasSize.value.width, canvasSize.value.height))
const visualNodeByNodeId = computed(() => {
  const result = new Map<number, VisualNode>()
  treeLayout.value.nodes.forEach((item) => {
    if (item.node) {
      result.set(item.node.id, item)
    }
  })
  return result
})
const visibleConnections = computed(() => connections.value
  .map((connection) => {
    const source = visualNodeByNodeId.value.get(connection.sourceNodeId)
    const target = visualNodeByNodeId.value.get(connection.targetNodeId)
    if (!source || !target) {
      return null
    }
    return {
      ...connection,
      path: connectionPath(source.x, source.y, target.x, target.y)
    }
  })
  .filter((item): item is LearningNodeConnection & { path: string } => Boolean(item)))
const selectedNodeConnections = computed(() => {
  if (!selectedNode.value) {
    return []
  }
  return connections.value.filter((connection) =>
    connection.sourceNodeId === selectedNode.value?.id || connection.targetNodeId === selectedNode.value?.id
  )
})
const connectionSourceNode = computed(() =>
  connectionSourceNodeId.value ? flatNodes.value.find((node) => node.id === connectionSourceNodeId.value) || null : null
)
const connectionTargetOptions = computed(() => {
  if (!connectionSourceNodeId.value) {
    return []
  }
  return flatNodes.value.filter((node) => node.id !== connectionSourceNodeId.value)
})
const focusedVisualId = computed(() => hoveredVisualId.value)
const activeVisualIds = computed(() => {
  const focused = focusedVisualId.value
  if (!focused) {
    return new Set<string>()
  }
  const related = new Set<string>([focused])
  treeLayout.value.links.forEach((link) => {
    if (link.parentId === focused) {
      related.add(link.childId)
    }
    if (link.childId === focused) {
      related.add(link.parentId)
    }
  })
  return related
})

const activeLinkIds = computed(() => {
  const focused = focusedVisualId.value
  if (!focused) {
    return new Set<string>()
  }
  return new Set(
    treeLayout.value.links
      .filter((link) => link.parentId === focused || link.childId === focused)
      .map((link) => link.id)
  )
})

const worldStyle = computed(() => ({
  width: `${canvasSize.value.width}px`,
  height: `${canvasSize.value.height}px`,
  transform: `translate3d(${viewport.x}px, ${viewport.y}px, 0) scale(${viewport.scale})`
}))

const stats = computed(() => {
  const nodes = flatNodes.value
  return {
    total: nodes.length,
    inProgress: nodes.filter((node) => node.status === 'IN_PROGRESS').length,
    mastered: nodes.filter((node) => node.status === 'MASTERED').length,
    weak: nodes.filter((node) => node.weakScore >= 70).length
  }
})

const selectedCustomIconKey = computed(() => {
  if (!selectedNode.value) {
    return ''
  }
  const key = selectedNode.value.iconKey || nodeIconMap.value[selectedNode.value.id]
  return key && iconRegistry[key] ? key : ''
})

const selectedNodeIcon = computed(() => {
  if (!selectedNode.value) {
    return Network
  }
  return iconForLearningNode(selectedNode.value)
})
const weakScoreTone = computed(() => {
  const score = selectedNode.value?.weakScore ?? 0
  if (score >= 70) {
    return 'tone-alert'
  }
  if (score >= 40) {
    return 'tone-watch'
  }
  return 'tone-calm'
})

watch(selectedNode, () => {
  resetDraft()
  syncTagDrafts()
})
watch(displayTree, () => nextTick(fitView), { flush: 'post' })
watch(detailOpen, (open) => {
  document.body.classList.toggle('tree-modal-open', open)
})
onMounted(async () => {
  await bootstrapSession()
  await nextTick()
  fitView()
})

onUnmounted(() => {
  document.body.classList.remove('tree-modal-open')
  if (successTimer) {
    clearTimeout(successTimer)
  }
})

async function bootstrapSession() {
  if (!getAuthSession()) {
    currentUser.value = null
    return
  }
  authLoading.value = true
  try {
    currentUser.value = await getCurrentUser()
    await loadStudyWorkspace()
  } catch (error) {
    handleApiProblem(error, '登录状态已失效，请重新登录')
  } finally {
    authLoading.value = false
  }
}

async function loadStudyWorkspace(preferredTreeId?: number) {
  if (!currentUser.value) {
    return
  }
  pageError.value = ''
  const [groups, trees] = await Promise.all([getStudyGroups(), getStudyTrees()])
  studyGroups.value = groups
  studyTrees.value = trees

  const nextTreeId = resolveNextTreeId(trees, preferredTreeId)
  activeTreeId.value = nextTreeId

  const matchedTree = trees.find((item) => item.id === nextTreeId) || null
  activeGroupId.value = matchedTree?.groupId ?? activeGroupId.value

  if (nextTreeId) {
    await loadTree(nextTreeId)
    await loadConnections()
  } else {
    tree.value = []
    connections.value = []
    selectedNode.value = null
  }
}

function resolveNextTreeId(trees: StudyTree[], preferredTreeId?: number) {
  if (preferredTreeId && trees.some((item) => item.id === preferredTreeId)) {
    return preferredTreeId
  }
  if (activeTreeId.value && trees.some((item) => item.id === activeTreeId.value)) {
    return activeTreeId.value
  }
  if (activeGroupId.value !== null) {
    const grouped = trees.find((item) => item.groupId === activeGroupId.value)
    if (grouped) {
      return grouped.id
    }
  }
  return trees[0]?.id ?? null
}

async function loadTree(treeId = activeTreeId.value ?? undefined) {
  if (!currentUser.value || !treeId) {
    return
  }
  loading.value = true
  pageError.value = ''
  try {
    const remoteTree = await getLearningTree(treeId)
    tree.value = remoteTree
    selectedNode.value = selectedNode.value
      ? flatNodes.value.find((node) => node.id === selectedNode.value?.id) || flatNodes.value[0] || null
      : flatNodes.value[0] || null
  } catch (error) {
    handleApiProblem(error, '学习树加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSelectTree(treeId: number) {
  if (treeId === activeTreeId.value) {
    return
  }
  activeTreeId.value = treeId
  const matched = studyTrees.value.find((item) => item.id === treeId)
  if (matched) {
    activeGroupId.value = matched.groupId ?? null
  }
  selectedNode.value = null
  detailOpen.value = false
  await loadTree(treeId)
  await loadConnections()
  await nextTick()
  fitView()
}

async function handleSelectGroup(groupId: number | null) {
  activeGroupId.value = groupId
  if (groupId === null) {
    if (studyTrees.value.length && !studyTrees.value.some((item) => item.id === activeTreeId.value)) {
      await handleSelectTree(studyTrees.value[0].id)
    }
    return
  }
  const nextTree = studyTrees.value.find((item) => item.groupId === groupId) || null
  if (!nextTree) {
    activeTreeId.value = null
    tree.value = []
    connections.value = []
    selectedNode.value = null
    detailOpen.value = false
    return
  }
  if (nextTree.id !== activeTreeId.value) {
    await handleSelectTree(nextTree.id)
  }
}

async function handleImport() {
  if (!currentUser.value || !activeTreeId.value) {
    return
  }
  loading.value = true
  pageError.value = ''
  try {
    tree.value = await importOfficialTree(activeTreeId.value)
    await loadConnections()
    selectedNode.value = flatNodes.value[0] || null
    flashSuccess('当前树已同步官方大纲。')
    await nextTick()
    fitView()
  } catch (error) {
    handleApiProblem(error, '导入官方大纲失败')
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  authLoading.value = true
  authError.value = ''
  pageError.value = ''
  try {
    const session = await login(loginForm.username.trim(), loginForm.password)
    currentUser.value = session.user
    await loadStudyWorkspace()
    await nextTick()
    fitView()
  } catch (error) {
    authError.value = errorMessage(error, '登录失败')
  } finally {
    authLoading.value = false
  }
}

function handleLogout() {
  clearAuthSession()
  currentUser.value = null
  studyGroups.value = []
  studyTrees.value = []
  tree.value = []
  connections.value = []
  selectedNode.value = null
  detailOpen.value = false
  agentAnswer.value = null
  pageError.value = ''
  pageSuccess.value = ''
  authError.value = ''
  activeGroupId.value = null
  activeTreeId.value = null
  loginForm.password = ''
}

async function handleCreateGroup() {
  if (!groupDraft.name.trim()) {
    return
  }
  createGroupLoading.value = true
  pageError.value = ''
  try {
    const created = await createStudyGroup({
      name: groupDraft.name.trim(),
      sortOrder: studyGroups.value.length * 10 + 10
    })
    groupDraft.name = ''
    await loadStudyWorkspace(activeTreeId.value ?? undefined)
    activeGroupId.value = created.id
    flashSuccess('已新建分组。')
  } catch (error) {
    handleApiProblem(error, '创建分组失败')
  } finally {
    createGroupLoading.value = false
  }
}

async function handleCreateTree() {
  if (!treeDraft.name.trim()) {
    return
  }
  createTreeLoading.value = true
  pageError.value = ''
  try {
    const created = await createStudyTree({
      groupId: activeGroupId.value,
      name: treeDraft.name.trim(),
      description: treeDraft.description.trim() || undefined,
      sortOrder: filteredStudyTrees.value.length * 10 + 10
    })
    treeDraft.name = ''
    treeDraft.description = ''
    await loadStudyWorkspace(created.id)
    flashSuccess('已新建考研树。')
    await nextTick()
    fitView()
  } catch (error) {
    handleApiProblem(error, '创建树失败')
  } finally {
    createTreeLoading.value = false
  }
}

function openNodeDetail(node: LearningNode) {
  selectedNode.value = node
  detailOpen.value = true
  agentAnswer.value = null
}

function handleNodeClick(node: LearningNode) {
  if (connectionSourceNodeId.value && connectionSourceNodeId.value !== node.id) {
    handleCreateConnection(node)
    return
  }
  openNodeDetail(node)
}

function closeNodeDetail() {
  detailOpen.value = false
}

function jumpToNode(node: LearningNode) {
  selectedNode.value = node
  detailOpen.value = false
  agentAnswer.value = null
  const visual = treeLayout.value.nodes.find((item) => item.node?.id === node.id)
  if (visual) {
    centerVisualNode(visual)
  }
}

function resetDraft() {
  if (!selectedNode.value) {
    return
  }
  draft.title = selectedNode.value.title
  draft.label = selectedNode.value.label
  draft.status = selectedNode.value.status
  draft.plainUnderstanding = selectedNode.value.plainUnderstanding || ''
  draft.todayFeeling = selectedNode.value.todayFeeling || ''
}

function syncTagDrafts() {
  if (!selectedNode.value) {
    return
  }
  selectedNode.value.tags.forEach((tag) => {
    tagEditDraft[tag.id] = {
      name: tag.name,
      color: tag.color || '#6bfb9a'
    }
  })
}

function applySelectedNodeTags(tags: LearningNodeTag[]) {
  if (!selectedNode.value) {
    return
  }
  const nextNode = { ...selectedNode.value, tags }
  selectedNode.value = nextNode
  patchNode(nextNode)
  syncTagDrafts()
}

async function handleCreateTag() {
  if (!selectedNode.value || !tagDraft.name.trim()) {
    return
  }
  tagSaving.value = true
  pageError.value = ''
  try {
    const created = await createLearningNodeTag(selectedNode.value.id, {
      name: tagDraft.name.trim(),
      color: tagDraft.color || '#6bfb9a'
    })
    applySelectedNodeTags([...selectedNode.value.tags, created])
    tagDraft.name = ''
    flashSuccess('标签已新增。')
  } catch (error) {
    handleApiProblem(error, '新增标签失败')
  } finally {
    tagSaving.value = false
  }
}

async function handleUpdateTag(tag: LearningNodeTag) {
  const draftValue = tagEditDraft[tag.id]
  if (!selectedNode.value || !draftValue?.name.trim()) {
    return
  }
  tagSaving.value = true
  pageError.value = ''
  try {
    const saved = await updateLearningNodeTag(tag.id, {
      name: draftValue.name.trim(),
      color: draftValue.color || '#6bfb9a'
    })
    applySelectedNodeTags(selectedNode.value.tags.map((item) => (item.id === saved.id ? saved : item)))
    flashSuccess('标签已更新。')
  } catch (error) {
    handleApiProblem(error, '更新标签失败')
  } finally {
    tagSaving.value = false
  }
}

async function handleDeleteTag(tag: LearningNodeTag) {
  if (!selectedNode.value) {
    return
  }
  tagSaving.value = true
  pageError.value = ''
  try {
    await deleteLearningNodeTag(tag.id)
    const nextTags = selectedNode.value.tags.filter((item) => item.id !== tag.id)
    delete tagEditDraft[tag.id]
    applySelectedNodeTags(nextTags)
    flashSuccess('标签已删除。')
  } catch (error) {
    handleApiProblem(error, '删除标签失败')
  } finally {
    tagSaving.value = false
  }
}

async function handleSave() {
  if (!selectedNode.value) {
    return
  }
  pageError.value = ''
  try {
    const saved = await updateLearningNode(selectedNode.value.id, { ...draft })
    patchNode(saved)
    selectedNode.value = { ...saved, children: selectedNode.value.children }
    closeNodeDetail()
    flashSuccess('节点内容已保存。')
  } catch (error) {
    handleApiProblem(error, '保存节点失败')
  }
}

async function handleReview() {
  if (!selectedNode.value) {
    return
  }
  pageError.value = ''
  try {
    const saved = await reviewLearningNode(selectedNode.value.id)
    patchNode(saved)
    selectedNode.value = { ...saved, children: selectedNode.value.children }
    flashSuccess('已记录一次复习。')
  } catch (error) {
    handleApiProblem(error, '记录复习次数失败')
  }
}

async function handleCreateChild() {
  if (!selectedNode.value || !childDraft.title.trim()) {
    return
  }
  createNodeLoading.value = true
  pageError.value = ''
  try {
    const parent = selectedNode.value
    const created = await createLearningNode({
      treeId: parent.treeId || activeTreeId.value || undefined,
      parentId: parent.id,
      title: childDraft.title.trim(),
      label: childDraft.label.trim() || `第 ${parent.levelNo + 1} 级`,
      sortOrder: parent.children.length * 10 + 10
    })
    childDraft.title = ''
    childDraft.label = ''
    await reloadTreeAndSelect(created.id)
    flashSuccess('已新增下级节点。')
  } catch (error) {
    handleApiProblem(error, '新增下级节点失败')
  } finally {
    createNodeLoading.value = false
  }
}

async function handleCreateRootNode() {
  if (!rootDraft.title.trim() || !activeTreeId.value) {
    return
  }
  createNodeLoading.value = true
  pageError.value = ''
  try {
    const created = await createLearningNode({
      treeId: activeTreeId.value,
      parentId: null,
      title: rootDraft.title.trim(),
      label: rootDraft.label.trim() || '第 1 级',
      sortOrder: tree.value.length * 10 + 10
    })
    rootDraft.title = ''
    rootDraft.label = ''
    await reloadTreeAndSelect(created.id)
    flashSuccess('已新增一级节点。')
  } catch (error) {
    handleApiProblem(error, '新增一级节点失败')
  } finally {
    createNodeLoading.value = false
  }
}

function beginConnectionFromSelected() {
  if (!selectedNode.value) {
    return
  }
  connectionSourceNodeId.value = selectedNode.value.id
  editingConnectionId.value = null
  connectionDraft.relationType = 'RELATED'
  connectionDraft.label = ''
  detailOpen.value = false
  flashSuccess('请选择画布上的目标节点。')
}

async function handleCreateConnection(targetNode: LearningNode) {
  if (!connectionSourceNodeId.value || connectionSourceNodeId.value === targetNode.id) {
    return
  }
  connectionSaving.value = true
  pageError.value = ''
  try {
    const created = await createLearningConnection({
      sourceNodeId: connectionSourceNodeId.value,
      targetNodeId: targetNode.id,
      relationType: connectionDraft.relationType.trim() || 'RELATED',
      label: connectionDraft.label.trim() || undefined
    })
    connections.value = [...connections.value, created]
    connectionSourceNodeId.value = null
    connectionDraft.relationType = 'RELATED'
    connectionDraft.label = ''
    flashSuccess('节点连接已创建。')
  } catch (error) {
    handleApiProblem(error, '创建节点连接失败')
  } finally {
    connectionSaving.value = false
  }
}

function startEditConnection(connection: LearningNodeConnection) {
  editingConnectionId.value = connection.id
  connectionDraft.relationType = connection.relationType || 'RELATED'
  connectionDraft.label = connection.label || ''
}

async function handleUpdateConnection() {
  if (!editingConnectionId.value) {
    return
  }
  const current = connections.value.find((connection) => connection.id === editingConnectionId.value)
  if (!current) {
    return
  }
  connectionSaving.value = true
  pageError.value = ''
  try {
    const saved = await updateLearningConnection(current.id, {
      sourceNodeId: current.sourceNodeId,
      targetNodeId: current.targetNodeId,
      relationType: connectionDraft.relationType.trim() || 'RELATED',
      label: connectionDraft.label.trim() || undefined
    })
    connections.value = connections.value.map((connection) => (connection.id === saved.id ? saved : connection))
    editingConnectionId.value = null
    connectionDraft.relationType = 'RELATED'
    connectionDraft.label = ''
    flashSuccess('连接已更新。')
  } catch (error) {
    handleApiProblem(error, '更新节点连接失败')
  } finally {
    connectionSaving.value = false
  }
}

async function handleDeleteConnection(connectionId: number) {
  connectionSaving.value = true
  pageError.value = ''
  try {
    await deleteLearningConnection(connectionId)
    connections.value = connections.value.filter((connection) => connection.id !== connectionId)
    if (editingConnectionId.value === connectionId) {
      editingConnectionId.value = null
    }
    flashSuccess('连接已删除。')
  } catch (error) {
    handleApiProblem(error, '删除节点连接失败')
  } finally {
    connectionSaving.value = false
  }
}

async function handleAskAgent() {
  if (!selectedNode.value) {
    return
  }
  if (!draft.todayFeeling.trim()) {
    pageError.value = '先写一点今日复习感受，再让 Agent 读取上下文。'
    return
  }
  agentLoading.value = true
  pageError.value = ''
  try {
    agentAnswer.value = await askAgent({
      nodeId: selectedNode.value.id,
      feeling: draft.todayFeeling,
      plainUnderstanding: draft.plainUnderstanding
    })
  } catch (error) {
    handleApiProblem(error, 'Agent 分析失败')
  } finally {
    agentLoading.value = false
  }
}

async function reloadTreeAndSelect(nodeId: number) {
  if (!activeTreeId.value) {
    return
  }
  const remoteTree = await getLearningTree(activeTreeId.value)
  tree.value = remoteTree
  await loadConnections()
  selectedNode.value = flatNodes.value.find((node) => node.id === nodeId) || flatNodes.value[0] || null
  await nextTick()
  const visual = treeLayout.value.nodes.find((item) => item.node?.id === nodeId)
  if (visual) {
    centerVisualNode(visual)
  }
}

async function loadConnections() {
  if (!currentUser.value) {
    connections.value = []
    return
  }
  const allConnections = await getLearningConnections()
  const visibleNodeIds = new Set(flatNodes.value.map((node) => node.id))
  connections.value = allConnections.filter((connection) =>
    visibleNodeIds.has(connection.sourceNodeId) && visibleNodeIds.has(connection.targetNodeId)
  )
}

function handleApiProblem(error: unknown, fallback: string) {
  const message = errorMessage(error, fallback)
  pageSuccess.value = ''
  if (error instanceof ApiError && error.code === 401) {
    clearAuthSession()
    currentUser.value = null
    tree.value = []
    selectedNode.value = null
    detailOpen.value = false
    authError.value = message
    return
  }
  pageError.value = message
}

function flashSuccess(message: string) {
  pageError.value = ''
  pageSuccess.value = message
  if (successTimer) {
    clearTimeout(successTimer)
  }
  successTimer = setTimeout(() => {
    pageSuccess.value = ''
  }, 2200)
}

function errorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback
}

function roleText(role: RoleCode) {
  if (role === 'SYSTEM_ADMIN') {
    return '系统管理员'
  }
  if (role === 'COMMUNITY_ADMIN') {
    return '社区管理员'
  }
  return '考生'
}

function groupNameFor(groupId?: number | null) {
  if (!groupId) {
    return '未分组'
  }
  return studyGroups.value.find((item) => item.id === groupId)?.name || '未分组'
}

function startPan(event: PointerEvent) {
  if (event.button !== 0 && event.button !== 2) {
    return
  }
  if ((event.target as HTMLElement).closest('button, input, textarea')) {
    return
  }
  dragState.dragging = true
  dragState.startX = event.clientX
  dragState.startY = event.clientY
  dragState.originX = viewport.x
  dragState.originY = viewport.y
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

function movePan(event: PointerEvent) {
  if (!dragState.dragging) {
    return
  }
  viewport.x = dragState.originX + event.clientX - dragState.startX
  viewport.y = dragState.originY + event.clientY - dragState.startY
}

function endPan() {
  dragState.dragging = false
}

function handleWheel(event: WheelEvent) {
  event.preventDefault()
  const stage = stageRef.value
  if (!stage) {
    return
  }
  const rect = stage.getBoundingClientRect()
  const pointerX = event.clientX - rect.left
  const pointerY = event.clientY - rect.top
  const nextScale = clamp(viewport.scale * (event.deltaY > 0 ? 0.9 : 1.1), 0.28, 1.55)
  const worldX = (pointerX - viewport.x) / viewport.scale
  const worldY = (pointerY - viewport.y) / viewport.scale
  viewport.scale = nextScale
  viewport.x = pointerX - worldX * nextScale
  viewport.y = pointerY - worldY * nextScale
}

function zoomBy(rate: number) {
  const stage = stageRef.value
  if (!stage) {
    return
  }
  const rect = stage.getBoundingClientRect()
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  const nextScale = clamp(viewport.scale * rate, 0.28, 1.55)
  const worldX = (centerX - viewport.x) / viewport.scale
  const worldY = (centerY - viewport.y) / viewport.scale
  viewport.scale = nextScale
  viewport.x = centerX - worldX * nextScale
  viewport.y = centerY - worldY * nextScale
}

function fitView() {
  const stage = stageRef.value
  if (!stage) {
    return
  }
  const rect = stage.getBoundingClientRect()
  const scale = Math.min(rect.width / canvasSize.value.width, rect.height / canvasSize.value.height) * 0.9
  viewport.scale = clamp(scale, 0.28, 0.82)
  viewport.x = (rect.width - canvasSize.value.width * viewport.scale) / 2
  viewport.y = (rect.height - canvasSize.value.height * viewport.scale) / 2 + 20
}

function centerVisualNode(item: VisualNode) {
  const stage = stageRef.value
  if (!stage) {
    return
  }
  const rect = stage.getBoundingClientRect()
  viewport.x = rect.width / 2 - item.x * viewport.scale
  viewport.y = rect.height / 2 - item.y * viewport.scale
}

function patchNode(next: LearningNode) {
  const current = flatNodes.value.find((node) => node.id === next.id)
  if (current) {
    Object.assign(current, next, { children: current.children })
  }
}

function filterTree(nodes: LearningNode[]): LearningNode[] {
  const term = keyword.value.trim().toLowerCase()
  return nodes
    .map((node) => {
      const children = filterTree(node.children)
      const statusMatch =
        mode.value === 'all' ||
        (mode.value === 'weak' && node.weakScore >= 70) ||
        (mode.value === 'mastered' && node.status === 'MASTERED')
      const textMatch =
        !term ||
        node.title.toLowerCase().includes(term) ||
        (node.label || '').toLowerCase().includes(term) ||
        (node.plainUnderstanding || '').toLowerCase().includes(term) ||
        (node.todayFeeling || '').toLowerCase().includes(term) ||
        node.tags.some((tag) => tag.name.toLowerCase().includes(term))
      if ((statusMatch && textMatch) || children.length > 0) {
        return { ...node, children }
      }
      return null
    })
    .filter((node): node is LearningNode => Boolean(node))
}

function buildOrganicLayout(nodes: LearningNode[], width: number, height: number) {
  const visualNodes: VisualNode[] = []
  const links: VisualLink[] = []
  const rings: GrowthRing[] = []
  const maxDepth = Math.max(3, getMaxDepth(nodes) + 1)
  const rootY = height - 420
  const rootGap = nodes.length > 1 ? (width - 760) / (nodes.length - 1) : 0
  const verticalGap = Math.min(260, Math.max(190, (height - 650) / maxDepth))

  const virtualRoot: VisualNode = {
    id: 'virtual-root',
    kind: 'virtual',
    title: treeCanvasTitle.value,
    label: '当前学习树',
    depth: 0,
    x: width / 2,
    y: height - 150,
    childCount: nodes.length,
    reviewCount: 0,
    weakScore: 0,
    statusClass: 'in-progress'
  }
  visualNodes.push(virtualRoot)
  rings.push({ id: 'ring-root-a', x: virtualRoot.x, y: virtualRoot.y, r: 96 })
  rings.push({ id: 'ring-root-b', x: virtualRoot.x, y: virtualRoot.y, r: 158 })

  function createVisual(node: LearningNode, depth: number, x: number, y: number): VisualNode {
    const visual: VisualNode = {
      id: `node-${node.id}`,
      kind: 'real',
      node,
      title: node.title,
      label: `${node.label} · ${statusText(node.status)}`,
      depth,
      x,
      y,
      childCount: node.children.length,
      reviewCount: node.reviewCount,
      weakScore: node.weakScore,
      statusClass: node.status.toLowerCase().replace('_', '-')
    }
    visualNodes.push(visual)
    if (depth <= 3) {
      rings.push({ id: `ring-${node.id}`, x, y, r: depth === 1 ? 82 : depth === 2 ? 62 : 46 })
    }
    return visual
  }

  function link(parent: VisualNode, child: VisualNode) {
    links.push({
      id: `${parent.id}-${child.id}`,
      path: branchPath(parent.x, parent.y, child.x, child.y),
      weak: child.weakScore >= 70 || parent.weakScore >= 70,
      parentId: parent.id,
      childId: child.id
    })
  }

  function placeChildren(parentNode: LearningNode, parentVisual: VisualNode, depth: number, seed: number, branchLean: number) {
    const children = parentNode.children
    if (!children.length) {
      return
    }
    const spread = Math.max(138, 430 - depth * 46)
    const canopyLift = Math.max(0, depth - 2) * 18
    children.forEach((child, index) => {
      const side = index - (children.length - 1) / 2
      const direction = side === 0 ? branchLean : Math.sign(side)
      const singleDrift = children.length === 1 ? branchLean * Math.max(82, 185 - depth * 20) : 0
      const wave = Math.sin((child.id + seed * 7 + depth * 11) * 0.42) * 46
      const fan = side * spread + singleDrift + branchLean * Math.max(18, 70 - depth * 8)
      const x = clamp(parentVisual.x + fan + wave, 280, width - 280)
      const y = parentVisual.y - verticalGap - canopyLift + Math.sin((child.id + depth) * 0.8) * 20
      const visual = createVisual(child, depth, x, y)
      link(parentVisual, visual)
      placeChildren(child, visual, depth + 1, seed + index + 5, direction || branchLean)
    })
  }

  nodes.forEach((node, index) => {
    const side = nodes.length === 1 ? 0 : index - (nodes.length - 1) / 2
    const lean = side === 0 ? (index % 2 === 0 ? -1 : 1) : Math.sign(side)
    const rootX = nodes.length === 1 ? width / 2 : 380 + index * rootGap
    const rootYShift = Math.abs(side) * -26 + (index % 2 === 0 ? 0 : -34)
    const visual = createVisual(node, 1, rootX, rootY + rootYShift)
    link(virtualRoot, visual)
    placeChildren(node, visual, 2, index * 17 + 5, lean)
  })

  return {
    nodes: visualNodes.sort((a, b) => a.depth - b.depth),
    links,
    rings,
    trunkPath: `M ${virtualRoot.x} ${height} C ${virtualRoot.x - 10} ${height - 104}, ${virtualRoot.x + 10} ${height - 104}, ${virtualRoot.x} ${virtualRoot.y + 12}`
  }
}

function branchPath(x1: number, y1: number, x2: number, y2: number) {
  const dy = Math.abs(y2 - y1)
  const dx = x2 - x1
  const sway = Math.sin((x1 + x2 + y2) * 0.004) * 42
  const c1x = x1 + dx * 0.18 + sway
  const c1y = y1 - dy * 0.38
  const c2x = x2 - dx * 0.24 - sway
  const c2y = y2 + dy * 0.34 + Math.sign(dx || 1) * 24
  return `M ${x1} ${y1} C ${c1x} ${c1y}, ${c2x} ${c2y}, ${x2} ${y2}`
}

function connectionPath(x1: number, y1: number, x2: number, y2: number) {
  const dx = x2 - x1
  const dy = y2 - y1
  const lift = Math.max(90, Math.min(260, Math.abs(dx) * 0.18 + Math.abs(dy) * 0.24))
  return `M ${x1} ${y1} C ${x1 + dx * 0.28} ${y1 - lift}, ${x2 - dx * 0.28} ${y2 - lift}, ${x2} ${y2}`
}

function nodeTitle(nodeId: number) {
  return flatNodes.value.find((node) => node.id === nodeId)?.title || `节点 ${nodeId}`
}

function flatten(nodes: LearningNode[]): LearningNode[] {
  return nodes.flatMap((node) => [node, ...flatten(node.children)])
}

function countLeaves(nodes: LearningNode[]): number {
  return nodes.reduce((sum, node) => sum + (node.children.length ? countLeaves(node.children) : 1), 0)
}

function getMaxDepth(nodes: LearningNode[], depth = 0): number {
  return nodes.reduce((max, node) => Math.max(max, node.children.length ? getMaxDepth(node.children, depth + 1) : depth), depth)
}

function countDescendants(node: LearningNode): number {
  return 1 + flatten(node.children).length
}

function findRootId(node: LearningNode | null) {
  if (!node) {
    return tree.value[0]?.id
  }
  let current: LearningNode | undefined = flatNodes.value.find((item) => item.id === node.id)
  while (current?.parentId) {
    current = flatNodes.value.find((item) => item.id === current?.parentId)
  }
  return current?.id || node.id
}

function statusText(status: LearningStatus) {
  return statusOptions.find((item) => item.value === status)?.label || status
}

function iconForDepth(depth: number) {
  if (depth === 0) {
    return Network
  }
  if (depth === 1) {
    return BrainCircuit
  }
  if (depth === 2) {
    return GitBranch
  }
  if (depth === 3) {
    return Layers3
  }
  return Circle
}

function nodeIconFor(item: VisualNode) {
  if (item.kind === 'virtual' || !item.node) {
    return iconForDepth(item.depth)
  }
  return iconForLearningNode(item.node, item.depth)
}

function iconForLearningNode(node: LearningNode, depth = node.levelNo) {
  const customIcon = node.iconKey || nodeIconMap.value[node.id]
  if (customIcon && iconRegistry[customIcon]) {
    return iconRegistry[customIcon]
  }
  return iconForDepth(depth)
}

async function chooseNodeIcon(key: string) {
  if (!selectedNode.value || !iconRegistry[key]) {
    return
  }
  iconSaving.value = true
  pageError.value = ''
  try {
    const saved = await updateLearningNodeIcon(selectedNode.value.id, key)
    patchNode(saved)
    selectedNode.value = { ...saved, children: selectedNode.value.children }
    nodeIconMap.value = {
      ...nodeIconMap.value,
      [selectedNode.value.id]: key
    }
    persistNodeIconMap()
  } catch (error) {
    handleApiProblem(error, '保存节点图案失败')
  } finally {
    iconSaving.value = false
  }
}

async function clearNodeIcon() {
  if (!selectedNode.value) {
    return
  }
  iconSaving.value = true
  pageError.value = ''
  try {
    const saved = await updateLearningNodeIcon(selectedNode.value.id, null)
    patchNode(saved)
    selectedNode.value = { ...saved, children: selectedNode.value.children }
    const next = { ...nodeIconMap.value }
    delete next[selectedNode.value.id]
    nodeIconMap.value = next
    persistNodeIconMap()
  } catch (error) {
    handleApiProblem(error, '清除节点图案失败')
  } finally {
    iconSaving.value = false
  }
}

function loadNodeIconMap(): Record<number, string> {
  if (typeof window === 'undefined') {
    return {}
  }
  try {
    const saved = window.localStorage.getItem(NODE_ICON_STORAGE_KEY)
    if (!saved) {
      return {}
    }
    const parsed = JSON.parse(saved)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return {}
    }
    return Object.entries(parsed).reduce<Record<number, string>>((result, [nodeId, key]) => {
      const id = Number(nodeId)
      if (Number.isInteger(id) && typeof key === 'string') {
        result[id] = key
      }
      return result
    }, {})
  } catch {
    return {}
  }
}

function persistNodeIconMap() {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(NODE_ICON_STORAGE_KEY, JSON.stringify(nodeIconMap.value))
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}
</script>
