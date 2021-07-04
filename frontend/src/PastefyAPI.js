import { Cajax } from "cajaxjs";
// import store from './store'

export class PastefyAPI extends Cajax {
    constructor(baseURL = "https://pastefy.ga"){
        super(baseURL)

        this.promiseInterceptor =  async res =>{
            const json = await res.json()
            
            if (json.error)
                throw new Error("Authentication")
            
            return json
        }
    }

    createPaste(data){
        return new Promise((res, err)=>{
            this.post("/api/v2/paste", data)
                .then(response => {
                    if (response.success) {
                        const paste = response.paste

                        res(paste)
                    } else err(Error());
                    this.loading = false
                })
        })
    }

    editPaste(id, data){
        return new Promise((res, err)=>{
            this.put(`/api/v2/paste/${id}`, data)
                .then(response => {
                    if (response.success) {
                        res()
                    } else err(Error());
                    this.loading = false
                })
        })
    }
}