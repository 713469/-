export type LearningStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'MASTERED'
export type RoleCode = 'SYSTEM_ADMIN' | 'COMMUNITY_ADMIN' | 'USER'

export interface AuthUser {
  id: number
  username: string
  displayName: string
  role: RoleCode
  roles: RoleCode[]
}

export interface LoginResponse {
  token: string
  user: AuthUser
}

export interface LearningNode {
  id: number
  treeId?: number | null
  officialNodeId?: number | null
  parentId?: number | null
  title: string
  label: string
  iconKey?: string | null
  levelNo: number
  sortOrder: number
  status: LearningStatus
  reviewCount: number
  plainUnderstanding?: string | null
  todayFeeling?: string | null
  customNode: boolean
  weakScore: number
  tags: LearningNodeTag[]
  children: LearningNode[]
}

export interface StudyGroup {
  id: number
  userId: number
  name: string
  description?: string | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface StudyTree {
  id: number
  userId: number
  groupId?: number | null
  name: string
  description?: string | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface LearningNodeTag {
  id: number
  userNodeId: number
  name: string
  color: string
}

export interface LearningNodeConnection {
  id: number
  sourceNodeId: number
  targetNodeId: number
  relationType: string
  label?: string | null
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface AgentAssistResponse {
  answer: string
  weaknessHints: string[]
  nextAction: string
}

export interface CommunityPost {
  id: number
  authorId: number
  title: string
  content: string
  status: 'PENDING' | 'PUBLISHED' | 'REJECTED'
  createdAt: string
}
