<template>
    <div>
        <h2>{{ helloMessage }}</h2>
    </div>
</template>

<script>
    export default {
        name: "example-component",
        data: ()=>({
            helloMessage: "Hi there!"
        }),
        mounted() {
            console.log('Welcome in vue!')
        }
    }
</script>
<style lang="scss">
h1 {
    color: #4365FF;
}
</style>
