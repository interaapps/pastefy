<template>
  <div class="mx-auto w-full max-w-[800px]">
    <div class="mb-10 flex flex-col items-center gap-4">
      <div class="flex items-center gap-3">
        <Logo />
        <span>+</span>
        <svg
          class="w-[3rem]"
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 866.0254037844387 866.0254037844387"
        >
          <defs>
            <mask id="small-triangle-mask">
              <rect width="100%" height="100%" fill="white" />
              <polygon
                points="508.01270189221935 433.01270189221935, 208.0127018922194 259.8076211353316, 208.01270189221927 606.217782649107"
                fill="black"
              ></polygon>
            </mask>
          </defs>
          <polygon
            points="808.0127018922194 433.01270189221935, 58.01270189221947 -1.1368683772161603e-13, 58.01270189221913 866.0254037844386"
            mask="url(#small-triangle-mask)"
            fill="#d40000"
          ></polygon>
          <polyline
            points="481.2177826491071 333.0127018922194, 134.80762113533166 533.0127018922194"
            stroke="#d40000"
            stroke-width="90"
          ></polyline>
        </svg>
      </div>

      <h2 class="text-xl">Connecting your account with Asciicema.</h2>
    </div>

    <div class="mb-7">
      <h3 class="mb-2 text-lg">Create Api Key</h3>

      <Button
        as="router-link"
        :to="{ name: 'api-keys' }"
        label="Visit API-Keys page"
        outlined
        size="small"
      />
    </div>

    <div class="mb-7">
      <h3 class="mb-2 text-lg">Add Api-Key to Asciicema Config</h3>
      <div class="rounded-xl border border-neutral-200 bg-neutral-100">
        <div class="w-full border-b border-neutral-200 p-1 px-3">
          <span class="text-sm">bash</span>
        </div>
        <Highlighted
          file-name="example.bash"
          :contents="`# set YOUR_PASTEFY_API_KEY to your Api Key
echo YOUR_PASTEFY_API_KEY > ~/.config/asciinema/install-id`"
        />
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import Logo from '@/components/Logo.vue'
import Button from 'primevue/button'
</script>
