<script setup lang="ts">
import ICAL from 'ical.js'
import { ref } from 'vue'
import Button from 'primevue/button'

const props = defineProps<{ ics: string }>()

const events = ref<
  {
    uid: string
    summary?: string
    location?: string
    organizer?: string
    attendees?: string[]
    start: Date
    end?: Date
  }[]
>([])

const addToCalendar = () => {
  const blob = new Blob([props.ics], { type: 'text/calendar' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'calendar.ics'
  a.click()
  URL.revokeObjectURL(url)
}

try {
  const jcalData = ICAL.parse(props.ics)
  const comp = new ICAL.Component(jcalData)

  events.value = comp.getAllSubcomponents('vevent').map((e) => {
    const event = new ICAL.Event(e)

    return {
      uid: event.uid,
      summary: event.summary,
      location: event.location,
      organizer: event.organizer,
      color: event.color,
      attendees: event.attendees.map((a) => a?.jCal?.[3]),
      start: event.startDate.toJSDate(),
      end: event.endDate?.toJSDate(),
    }
  })
} catch (err) {
  console.error('Failed to parse ICS:', err)
}
</script>
<template>
  <div class="p-4">
    <ul class="space-y-4">
      <li
        v-for="event in events"
        :key="event.uid"
        class="flex flex-col gap-2 rounded-lg border border-neutral-300 bg-neutral-50 p-4 dark:border-neutral-700 dark:bg-neutral-800"
      >
        <div class="flex items-center space-x-2">
          <i class="ti ti-calendar text-xl" />
          <strong class="text-lg">{{ event.summary }}</strong>
        </div>
        <div class="flex flex-wrap gap-5">
          <div v-if="event.location" class="flex items-center gap-2 opacity-60">
            <i class="ti ti-map-pin text-xl" />
            {{ event.location }}
          </div>
          <div v-if="event.organizer" class="flex items-center gap-2 opacity-60">
            <i class="ti ti-user-star text-xl" />
            {{ event.organizer }}
          </div>
          <div v-if="event.attendees?.length" class="flex items-center gap-2 opacity-60">
            <i class="ti ti-users text-xl" />
            {{ event.attendees.join(', ') }}
          </div>
        </div>
        <div class="flex items-center gap-2 opacity-60">
          <i class="ti ti-clock text-xl" />
          {{ event.start.toLocaleString() }} → {{ event.end?.toLocaleString() }}
        </div>
      </li>
    </ul>

    <Button
      severity="contrast"
      outlined
      label="Add to Calendar"
      class="mt-4"
      icon="ti ti-calendar-week"
      @click="addToCalendar()"
    />
  </div>
</template>
