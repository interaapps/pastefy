<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  csv: string
}>()

const output = computed(() => props.csv.split('\n').map((row) => row.split(',')))

const maxCols = computed(() => {
  return Math.max(...output.value.map((row) => row.length))
})
</script>
<template>
  <div class="overflow-hidden text-sm">
    <div class="m-[-1px] overflow-auto">
      <table>
        <tr v-for="(row, index) in output" :key="index">
          <td
            v-for="cell in row"
            :key="cell"
            class="border border-neutral-200 p-1 px-2 whitespace-pre select-all dark:border-neutral-700"
          >
            {{ cell }}
          </td>
          <td
            v-for="i in maxCols - row.length"
            :key="i"
            class="border border-neutral-200 p-1 px-2 whitespace-pre dark:border-neutral-700"
          />
        </tr>
      </table>
    </div>
  </div>
</template>
