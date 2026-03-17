<script setup lang="ts">
import Message from 'primevue/message'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useNow, useStorage } from '@vueuse/core'
import Highlighted from '@/components/Highlighted.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const jwtInput = useStorage(
  'pastefy-utility-jwt-input',
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlBhc3RlZnkiLCJhZG1pbiI6dHJ1ZSwiaWF0IjoxNzEwNjYyNDAwLCJleHAiOjE4OTM0NTYwMDB9.signature',
)

const now = useNow({ interval: 1000 })

const base64UrlDecode = (value: string) => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  return atob(padded)
}

const jwtState = computed(() => {
  const token = jwtInput.value.trim()
  if (!token) return { error: 'Enter a JWT to inspect it.' }

  const parts = token.split('.')
  if (parts.length < 2) return { error: 'JWTs need at least header and payload sections.' }

  try {
    return {
      header: JSON.parse(base64UrlDecode(parts[0])),
      payload: JSON.parse(base64UrlDecode(parts[1])),
      signature: parts[2] || '',
      error: undefined,
    }
  } catch {
    return { error: 'Could not decode this JWT. Check that the token is base64url encoded.' }
  }
})

const formatTimestamp = (value: unknown) =>
  typeof value === 'number' ? new Date(value * 1000).toLocaleString() : undefined

const jwtTiming = computed(() => {
  if (!('payload' in jwtState.value) || !jwtState.value.payload) return []

  const payload = jwtState.value.payload as Record<string, unknown>
  const current = now.value.getTime()

  return ['iat', 'nbf', 'exp']
    .map((key) => {
      const raw = payload[key]
      if (typeof raw !== 'number') return undefined
      let status = 'neutral'
      if (key === 'exp') status = raw * 1000 > current ? 'valid' : 'expired'
      else if (key === 'nbf') status = raw * 1000 > current ? 'pending' : 'active'
      return {
        key,
        label: formatTimestamp(raw),
        status,
      }
    })
    .filter(Boolean)
})

const result = computed(() => {
  if (!('payload' in jwtState.value) || !jwtState.value.payload) return ''
  return JSON.stringify(
    {
      header: jwtState.value.header,
      payload: jwtState.value.payload,
      signature: jwtState.value.signature,
    },
    null,
    2,
  )
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">JWT</label>
      <Textarea v-model="jwtInput" auto-resize rows="8" fluid />
      <Message severity="secondary" size="small" variant="simple">
        The token is only decoded locally. Signature verification is not performed.
      </Message>
    </template>

    <template #result>
      <Message v-if="'error' in jwtState && jwtState.error" severity="error">
        {{ jwtState.error }}
      </Message>
      <template v-else>
        <div class="mb-4 flex flex-wrap gap-2">
          <span
            v-for="entry of jwtTiming"
            :key="entry?.key"
            class="rounded-full border border-neutral-200 bg-white px-3 py-1 text-xs dark:border-neutral-700 dark:bg-neutral-900"
          >
            {{ entry?.key }}: {{ entry?.label }} ({{ entry?.status }})
          </span>
        </div>
        <div class="grid gap-4 xl:grid-cols-2">
          <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
            <div class="border-b border-neutral-200 px-4 py-2 text-sm font-medium dark:border-neutral-700">Header</div>
            <Highlighted
              :contents="JSON.stringify(('header' in jwtState ? jwtState.header : {}), null, 2)"
              file-name="header.json"
            />
          </div>
          <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
            <div class="border-b border-neutral-200 px-4 py-2 text-sm font-medium dark:border-neutral-700">Payload</div>
            <Highlighted
              :contents="JSON.stringify(('payload' in jwtState ? jwtState.payload : {}), null, 2)"
              file-name="payload.json"
            />
          </div>
        </div>
      </template>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="jwt-inspection.json" />
    </template>
  </UtilityShell>
</template>
