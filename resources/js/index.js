/* NPM REQUIRED */
import Vue from 'vue'
import router from './router'
import store from './store'
import hljs from 'highlight.js'
import helper from "./helper"
import { Prajax } from "cajaxjs"
import { $n } from 'jdomjs'
require("babel-polyfill");
require("./css/app.scss")

Vue.component('App', require('./App.vue').default);

let worker = null

const app = new Vue({
    router,
    store,
    el: '#app'
});

store.state.app.loadingUser = true
Prajax.get("/user").then(res=>{
    store.state.user = res.json()
    store.state.app.loadingUser = false
    if (worker !== null && store.state.user.loggedIn) {
        worker.postMessage({
            action: 'registerUserLogin',
            user: store.state.user
        })
    }
})

if (typeof navigator !== 'undefined') {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/service-worker.js', {
            scope: '.'
          }).then(function(registration) {
            console.log('Registration successful, scope is:', registration.scope);
        }).catch(function(error) {
            console.log('Service worker registration failed, error:', error);
        });
    }
}

if (typeof Worker !== 'undefined') {
    worker = new Worker("/worker.js")
    worker.postMessage({action: 'updateStorage', storage: {
        notificationsEnabled: localStorage["browser_notifications"] == 'true'
    }})
    worker.onmessage = (e)=>{
        const data = e.data
        console.log("RECEIVED DATA");
        console.log(e);
        if (data.action == 'pushRoute') {
            window.focus()
            router.push(data.url)
        } else if (data.action == 'notification') {
            const toast = helper.showSnackBar($n('div')
                .append($n("i").text("close").addClass("material-icons").css({
                    float: 'right',
                    marginTop: '6px'
                }).click((e)=>{
                    toast.close()
                    e.preventDefault()
                    e.stopPropagation()
                    return false
                }))
                .append($n("h1").text(data.title).css({
                    fontSize: "21px",
                    textAlign: "left",
                    marginBottom: "10px",
                    verticalAlign: "middle"
                }))
                .append($n("p").text(data.message).css({
                    textAlign: "left",
                    fontSize: "18px",
                    maxWidth: "400px"
                })).html(), 
            "#434343", '#FFFFFF', false);
            toast.useHTML = true
            toast.timeout = 5000
            if (data.url != "") {
                const oldOnOpen = toast.onopen
                toast.onopen = ()=>{
                    toast.element.style.cursor = "pointer"
                    toast.element.addEventListener("click", (e)=>{
                        if (e.target != null && e.target.nodeName == 'I') {
                            toast.close()
                            return false
                        }

                        if (data.url.includes("://"))
                            window.location = data.url
                        else
                            router.push(data.url)
                        
                        toast.close()
                    })
                    
                    oldOnOpen()
                }
            }
            toast.open()
        }
    }
}


Vue.mixin({
    data: function(){return {
        worker
    }}
})

window.addEventListener("resize", function(){
    store.state.mobileVersion = window.innerWidth <= 720 
})

HTMLTextAreaElement.prototype.getCaretPosition = function () { //return the caret position of the textarea
    return this.selectionStart;
};
HTMLTextAreaElement.prototype.setCaretPosition = function (position) { //change the caret position of the textarea
    this.selectionStart = position;
    this.selectionEnd = position;
    this.focus();
};
HTMLTextAreaElement.prototype.hasSelection = function () { //if the textarea has selection then return true
    if (this.selectionStart == this.selectionEnd) {
        return false;
    } else {
        return true;
    }
};
HTMLTextAreaElement.prototype.getSelectedText = function () { //return the selection text
    return this.value.substring(this.selectionStart, this.selectionEnd);
};
HTMLTextAreaElement.prototype.setSelection = function (start, end) { //change the selection area of the textarea
    this.selectionStart = start;
    this.selectionEnd = end;
    this.focus();
};