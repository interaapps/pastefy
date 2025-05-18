<script setup lang="ts">
import 'leaflet/dist/leaflet.css'

import { computed, onMounted, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { LGeoJson, LMap, LTileLayer } from '@vue-leaflet/vue-leaflet'
import type { GeoJsonObject } from 'geojson'
import Button from 'primevue/button'

const props = defineProps<{
  geoJson: string
  inEditor?: boolean
}>()

const render = async () => {}
onMounted(() => {
  render()
})
watch(
  () => props.geoJson,
  useDebounceFn(() => {
    render()
  }, 500),
)

const parsedGeoJson = computed(() => JSON.parse(props.geoJson) as GeoJsonObject)

const zoom = ref(2)
</script>
<template>
  <div
    class="geo-json-group relative w-full overflow-hidden text-sm"
    :class="inEditor ? '' : 'h-[700px] rounded-xl'"
  >
    <LMap
      :options="{ zoomControl: false }"
      :center="[47.41322, -1.219482]"
      :use-global-leaflet="false"
      v-model:zoom="zoom"
    >
      <LGeoJson :geojson="parsedGeoJson" />
      <LTileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        layer-type="base"
        name="OpenStreetMap"
      />
    </LMap>

    <div class="geo-json-group-show absolute right-5 bottom-5 z-1000 flex gap-1">
      <Button icon="ti ti-minus" size="small" severity="secondary" @click="zoom--" />
      <Button icon="ti ti-plus" size="small" severity="secondary" @click="zoom++" />
    </div>
  </div>
</template>

<style>
.geo-json-group {
  .geo-json-group-show {
    transition: 0.3s;
    opacity: 0;
  }
  &:hover {
    .geo-json-group-show {
      opacity: 1;
    }
  }
}
</style>
