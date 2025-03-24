<script setup lang="ts">
import Message from 'primevue/message'
defineProps<{
  error: { response: { data: { exception: string } } }
}>()
</script>
<template>
  <Message severity="error">
    <div>{{ error?.response?.data?.exception }}</div>
  </Message>
</template>
