<script setup lang="ts">
import Button from 'primevue/button'
</script>
<template>
  <div class="mx-auto max-w-[1200px]">
    <div class="flex flex-col gap-4">
      <h1 class="text-2xl font-bold">Admin</h1>
      <div class="flex gap-1">
        <Button
          as="router-link"
          :to="{ name: 'admin-home' }"
          label="home"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          as="router-link"
          :to="{ name: 'admin-users' }"
          label="users"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          as="router-link"
          :to="{ name: 'admin-pastes' }"
          label="pastes"
          size="small"
          outlined
          severity="contrast"
        />
      </div>

      <div>
        <router-view />
      </div>
    </div>
  </div>
</template>
