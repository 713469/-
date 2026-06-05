import type {
  AgentAssistResponse,
  ApiResponse,
  AuthUser,
  CommunityPost,
  LearningNodeConnection,
  LearningNode,
  LearningStatus,
  LearningNodeTag,
  LoginResponse,
  StudyGroup,
  StudyTree
} from './types'

const AUTH_SESSION_KEY = 'ky-examination.auth-session'

interface AuthSession {
  token: string
  user: AuthUser
}

interface RequestOptions extends RequestInit {
  auth?: boolean
}

export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

export function getAuthSession(): AuthSession | null {
  try {
    const raw = window.localStorage.getItem(AUTH_SESSION_KEY)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw) as AuthSession
    return parsed?.token && parsed?.user ? parsed : null
  } catch (error) {
    return null
  }
}

export function setAuthSession(session: AuthSession) {
  window.localStorage.setItem(AUTH_SESSION_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  window.localStorage.removeItem(AUTH_SESSION_KEY)
}

async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const { auth = true, headers, ...fetchOptions } = options
  const requestHeaders: Record<string, string> = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    ...(headers as Record<string, string> | undefined)
  }
  if (auth) {
    const session = getAuthSession()
    if (!session?.token) {
      throw new ApiError(401, '请先登录')
    }
    requestHeaders.Authorization = `Bearer ${session.token}`
  }

  const response = await fetch(url, {
    ...fetchOptions,
    headers: requestHeaders
  })
  if (!response.ok) {
    throw new ApiError(response.status, `HTTP ${response.status}`)
  }
  const body = (await response.json()) as ApiResponse<T>
  if (body.code !== 0) {
    if (body.code === 401) {
      clearAuthSession()
    }
    throw new ApiError(body.code, body.message)
  }
  return body.data
}

export async function login(username: string, password: string) {
  const session = await request<LoginResponse>(
    '/api/auth/login',
    {
      method: 'POST',
      auth: false,
      body: JSON.stringify({ username, password })
    }
  )
  setAuthSession(session)
  return session
}

export async function getCurrentUser() {
  const user = await request<AuthUser>('/api/auth/me')
  const session = getAuthSession()
  if (session) {
    setAuthSession({ ...session, user })
  }
  return user
}

async function resolveTreeId(treeId?: number) {
  if (treeId) {
    return treeId
  }
  const trees = await getStudyTrees()
  if (!trees.length) {
    throw new ApiError(404, '暂无可用考研树')
  }
  return trees[0].id
}

export function getStudyGroups() {
  return request<StudyGroup[]>('/api/study-groups')
}

export function getStudyGroup(id: number) {
  return request<StudyGroup>(`/api/study-groups/${id}`)
}

export function createStudyGroup(payload: { name: string; description?: string; sortOrder?: number }) {
  return request<StudyGroup>('/api/study-groups', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateStudyGroup(
  id: number,
  payload: { name: string; description?: string; sortOrder?: number }
) {
  return request<StudyGroup>(`/api/study-groups/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteStudyGroup(id: number) {
  return request<void>(`/api/study-groups/${id}`, { method: 'DELETE' })
}

export function getStudyTrees() {
  return request<StudyTree[]>('/api/study-trees')
}

export function getStudyTree(id: number) {
  return request<StudyTree>(`/api/study-trees/${id}`)
}

export function createStudyTree(payload: {
  groupId?: number | null
  name: string
  description?: string
  sortOrder?: number
}) {
  return request<StudyTree>('/api/study-trees', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateStudyTree(
  id: number,
  payload: {
    groupId?: number | null
    name: string
    description?: string
    sortOrder?: number
  }
) {
  return request<StudyTree>(`/api/study-trees/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteStudyTree(id: number) {
  return request<void>(`/api/study-trees/${id}`, { method: 'DELETE' })
}

export function getStudyTreeChildren(id: number) {
  return request<LearningNode[]>(`/api/study-trees/${id}/children`)
}

export function getLearningTree(treeId?: number) {
  const query = treeId ? `?treeId=${treeId}` : ''
  return request<LearningNode[]>(`/api/learning/tree${query}`)
}

export async function importOfficialTree(treeId?: number) {
  const resolvedTreeId = await resolveTreeId(treeId)
  return request<LearningNode[]>(`/api/learning/import-official?treeId=${resolvedTreeId}`, { method: 'POST' })
}

export async function createLearningNode(payload: {
  treeId?: number
  parentId: number | null
  title: string
  label: string
  sortOrder: number
}) {
  const treeId = await resolveTreeId(payload.treeId)
  return request<LearningNode>('/api/learning/nodes', {
    method: 'POST',
    body: JSON.stringify({ ...payload, treeId })
  })
}

export function getLearningNode(id: number) {
  return request<LearningNode>(`/api/learning/nodes/${id}`)
}

export function updateLearningNode(
  id: number,
  payload: {
    title: string
    label: string
    iconKey?: string | null
    status: LearningStatus
    plainUnderstanding: string
    todayFeeling: string
  }
) {
  return request<LearningNode>(`/api/learning/nodes/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function reviewLearningNode(id: number) {
  return request<LearningNode>(`/api/learning/nodes/${id}/review`, { method: 'POST' })
}

export function deleteLearningNode(id: number) {
  return request<void>(`/api/learning/nodes/${id}`, { method: 'DELETE' })
}

export function updateLearningNodeIcon(id: number, iconKey: string | null) {
  return request<LearningNode>(`/api/learning/nodes/${id}/icon`, {
    method: 'PUT',
    body: JSON.stringify({ iconKey })
  })
}

export function getLearningNodeTags(id: number) {
  return request<LearningNodeTag[]>(`/api/learning/nodes/${id}/tags`)
}

export function getLearningNodeTag(tagId: number) {
  return request<LearningNodeTag>(`/api/learning/tags/${tagId}`)
}

export function createLearningNodeTag(id: number, payload: { name: string; color?: string }) {
  return request<LearningNodeTag>(`/api/learning/nodes/${id}/tags`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateLearningNodeTag(tagId: number, payload: { name: string; color?: string }) {
  return request<LearningNodeTag>(`/api/learning/tags/${tagId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteLearningNodeTag(tagId: number) {
  return request<void>(`/api/learning/tags/${tagId}`, { method: 'DELETE' })
}

export function getLearningConnections(treeId?: number) {
  const query = treeId ? `?treeId=${treeId}` : ''
  return request<LearningNodeConnection[]>(`/api/learning/connections${query}`)
}

export function getLearningConnection(connectionId: number) {
  return request<LearningNodeConnection>(`/api/learning/connections/${connectionId}`)
}

export function createLearningConnection(payload: {
  sourceNodeId: number
  targetNodeId: number
  relationType?: string
  label?: string
}) {
  return request<LearningNodeConnection>('/api/learning/connections', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateLearningConnection(
  connectionId: number,
  payload: {
    sourceNodeId: number
    targetNodeId: number
    relationType?: string
    label?: string
  }
) {
  return request<LearningNodeConnection>(`/api/learning/connections/${connectionId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteLearningConnection(connectionId: number) {
  return request<void>(`/api/learning/connections/${connectionId}`, { method: 'DELETE' })
}

export function askAgent(payload: {
  nodeId: number
  feeling: string
  plainUnderstanding: string
}) {
  return request<AgentAssistResponse>('/api/agent/assist', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getPublishedPosts() {
  return request<CommunityPost[]>('/api/community/posts')
}

export function createCommunityPost(payload: { title: string; content: string }) {
  return request<CommunityPost>('/api/community/posts', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getManagedPosts() {
  return request<CommunityPost[]>('/api/community/admin/posts')
}

export function updateCommunityPostStatus(id: number, status: CommunityPost['status']) {
  return request<CommunityPost>(`/api/community/admin/posts/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status })
  })
}

export const demoTree: LearningNode[] = [
  {
    id: 1,
    officialNodeId: 3,
    parentId: null,
    title: '操作系统',
    label: '科目',
    iconKey: 'brain',
    levelNo: 1,
    sortOrder: 30,
    status: 'IN_PROGRESS',
    reviewCount: 7,
    plainUnderstanding: 'OS 是资源管理者，把 CPU、内存、文件和 I/O 这些稀缺资源调度给进程。',
    todayFeeling: 'PV 操作还是会卡在信号量初值判断。',
    customNode: false,
    weakScore: 78,
    tags: [
      { id: 1, userNodeId: 1, name: '高频错题', color: '#ffb4ab' },
      { id: 2, userNodeId: 1, name: '二刷重点', color: '#44e2cd' }
    ],
    children: [
      {
        id: 2,
        officialNodeId: 10,
        parentId: 1,
        title: '进程与线程',
        label: '章',
        iconKey: 'branch',
        levelNo: 2,
        sortOrder: 10,
        status: 'IN_PROGRESS',
        reviewCount: 4,
        plainUnderstanding: '进程是资源分配单位，线程是调度单位。',
        todayFeeling: '调度算法还行，同步互斥需要题型归纳。',
        customNode: false,
        weakScore: 58,
        tags: [],
        children: [
          {
            id: 3,
            officialNodeId: 12,
            parentId: 2,
            title: 'PV 操作与信号量',
            label: '考点',
            iconKey: 'cable',
            levelNo: 3,
            sortOrder: 10,
            status: 'IN_PROGRESS',
            reviewCount: 5,
            plainUnderstanding: 'P 是申请资源或等待条件，V 是释放资源或通知条件成立。',
            todayFeeling: '今天做题又卡在生产者消费者变形题了。',
            customNode: false,
            weakScore: 92,
            tags: [{ id: 3, userNodeId: 3, name: 'Agent重点跟进', color: '#ffd36b' }],
            children: []
          }
        ]
      }
    ]
  },
  {
    id: 4,
    officialNodeId: 2,
    parentId: null,
    title: '数据结构',
    label: '科目',
    iconKey: 'database',
    levelNo: 1,
    sortOrder: 20,
    status: 'MASTERED',
    reviewCount: 11,
    plainUnderstanding: '数据结构就是数据之间的关系加上操作。',
    todayFeeling: '链表指针题比上周稳多了。',
    customNode: false,
    weakScore: 8,
    tags: [],
    children: []
  }
]
