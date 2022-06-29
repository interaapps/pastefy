<template>
    <div>
        <Header />

        <p>
            Created pastes: {{ stats.created_pastes }}<br>
            Created pastes (Logged in): {{ stats.logged_in_pastes }}
        </p>
    </div>
</template>

<script>
import Header from "@/components/admin/Header";
export default {
    name: "Overview",
    data: () => ({
        stats: {}
    }),
    components: {Header},
    async mounted() {
        this.stats = await this.pastefyAPI.get("/api/v2/app/stats")
    }
}
</script>