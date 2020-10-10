/* NPM REQUIRED */
import Vue from 'vue'
import router from './router'
import store from './store'
import hljs from 'highlight.js'
import { Prajax } from "cajaxjs";
require("./css/app.scss")

Vue.component('App', require('./App.vue').default);

const app = new Vue({
    router,
    store,
    el: '#app'
});

Prajax.get("/user").then(res=>{
    store.state.user = res.json()
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