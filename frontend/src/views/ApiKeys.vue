<template>
  <div>
      <a class="button" @click="newApiKey">ADD API-KEY</a>
      <a class="button" style="background: #FFFFFF09" href="https://github.com/interaapps/pastefy/wiki/API-v2" target="_blank">API-DOCS</a>
      <div class="api-key" v-for="apiKey in apiKeys" :key="apiKey">
          <a class="button delete" @click="deleteApiKey(apiKey)">DELETE</a>
          <a class="button" @click="copyApiKey(apiKey)">COPY</a>
          <span>{{apiKey.substr(0, 20) + apiKey.substr(20, 50).replace(/./g, "*") }}</span>
      </div>
  </div>
</template>

<script>
import helper from "../helper.js";

export default {
    data: ()=>({
        apiKeys: []
    }),
    mounted(){
        this.load()
    },
    methods: {
        load(){
            this.pastefyAPI.get("/api/v2/user/keys")
                .then(res => {
                    this.apiKeys = res
                }).catch(()=>{
                    this.$router.push("/")
                })
        },
        newApiKey(){
            this.pastefyAPI.post("/api/v2/user/keys")
                .then(this.load)
        },
        copyApiKey(id){
            helper.copyStringToClipboard(id)
            helper.showSnackBar("Copied")
        },
        deleteApiKey(id){
            this.pastefyAPI.delete("/api/v2/user/keys/"+id)
                .then((res)=>{
                    if (res.success) {
                        helper.showSnackBar("Deleted API-Key")
                        this.load()
                    }
                })
        }
    }
}
</script>

<style lang="scss" scoped>
    .button {
        margin-right: 10px;
    }

    .api-key {
        background: #212531;
        padding: 10px;
        margin-top: 10px;
        border-radius: 10px;
        max-width: 900px;
        .button {
            margin-top: -3px;
            margin-right: 0px;
            vertical-align: middle;
            padding: 3px 10px;
            margin-left: 4px;
            float: right;

            &.delete {
                background: #d3414d;
            }
        }

        span {
            vertical-align: middle;
            display: block;
            width: calc(100% - 200px);
            overflow: hidden;
            user-select: none;
        }
    }
</style>