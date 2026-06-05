<template>
  <div class="tree-node">
    <button
      class="tree-row"
      :class="{ selected: selectedId === node.id, weak: node.weakScore >= 70 }"
      type="button"
      @click="$emit('select', node)"
    >
      <ChevronRight
        class="chevron"
        :class="{ open: open && hasChildren, hidden: !hasChildren }"
        :size="16"
        @click.stop="open = !open"
      />
      <span class="node-title">{{ node.title }}</span>
      <span class="node-meta">{{ node.label }} · {{ statusText }}</span>
    </button>

    <div v-if="hasChildren && open" class="tree-children">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :selected-id="selectedId"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import type { LearningNode } from '../types'

const props = defineProps<{
  node: LearningNode
  selectedId?: number
}>()

defineEmits<{
  select: [node: LearningNode]
}>()

const open = ref(props.node.levelNo <= 2)
const hasChildren = computed(() => props.node.children.length > 0)
const statusText = computed(() => {
  const textMap: Record<LearningNode['status'], string> = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '学习中',
    MASTERED: '已掌握'
  }
  return textMap[props.node.status]
})
</script>
