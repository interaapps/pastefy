import AutoCompletion from "./AutoCompletion.js"

export default class CodeEditor {
    
    constructor(parentElement, options = {}){
        this.parentElement = parentElement
        this.initElements()
        this.tab = options.tabSize ? new Array(options.tabSize).join(" ") : "    "
        this.tabShortcutsEnabled = options.tabShortcutsEnabled ? options.tabShortcutsEnabled : true
        this.placeholder = options.placeholder ? options.placeholder : ""
        this.codeRightPadding = options.codeRightPadding ? options.codeRightPadding : 0
        this.readonly = options.readonly ? options.readonly : false
        this.leftLineNumPadding = 0
        this.autoCompletionIndex = 0
        this.autoCompletionOpened = false
        
        this.stringKeys = ['"', "'", '`', ...(options.stringKeys ? options.stringKeys : [])]

        this.closeKeys = {
            '{': '}',
            '(': ')',
            '[': ']',
            '"': '"',
            "'": "'",
            '`': '`',
            ...(options.closeKeys ? options.closeKeys : {})
        }

        this.newLineTabs = [':', ': ', ...(options.newLineTabs ? options.newLineTabs : [])]

        this.value = options.value ? options.value : ""

        this.autoCompleteHandler = options.autoCompleteHandler ? options.autoCompleteHandler : null

        this.highlighter = v=>v.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;")
    }

    initElements(){
        this.textAreaElement = document.createElement("textarea")
        this.lineNumerationsElement = document.createElement("div")
        this.codePreElement = document.createElement("pre")
        this.autoCompletionElement = document.createElement("div")

        this.autoCompletionElement.classList.add("petrel-code-editor-autocompletion")

        this.lineNumerationsElement.classList.add("petrel-code-editor-line-numbering")

        this.emit("elementsReady", this.textAreaElement)

        this.textAreaElement.setAttribute("contenteditable", "on")
        this.textAreaElement.setAttribute("spellcheck", "false")
        this.textAreaElement.setAttribute("wrap", "off")
        this.textAreaElement.setAttribute("placeholder", this.placeholder ? this.placeholder :'')
        this.textAreaElement.addEventListener("input", ()=>{
            this.update()
        })
        this.textAreaElement.addEventListener("keydown", event => {
            if (this.readonly) {
                event.preventDefault()
                return;
            }
            const caretPos = this.getCaretPosition()
            const oldValue = this.value
            
            if (this.tabShortcutsEnabled){
            switch (event.key) { 
                case "Tab":
                    if (!this.autoCompletionOpened){
                        if (event.shiftKey) {
                            if (!this.hasSelection() && this.value.substring(this.getCaretPosition() - this.tab.length, this.getCaretPosition()) == this.tab) {
                                const newCaretPosition = this.getCaretPosition() - 4;
                                this.value = this.value.substring(0, this.getCaretPosition() - 4) + this.value.substring(this.getCaretPosition(), this.value.length);
                                this.setCaretPosition(newCaretPosition);
                            }
                        } else {    
                            const newCaretPos = caretPos+this.tab.length
                            this.value = this.value.substring(0, this.getCaretPosition()) + this.tab + this.value.substring(this.getCaretPosition(), this.value.length)
                            this.setCaretPosition(newCaretPos)
                            this.emit("tab")
                        }
                        event.preventDefault()
                        break;
                    }
                case "Enter":
                    if (this.autoCompletionOpened) {
                        this.autoCompletionElement.childNodes[this.autoCompletionIndex].click()
                        this.checkAutoCompletion()
                        event.preventDefault()
                        return;
                    }
                    break
                case "Backspace":
                    if (!this.hasSelection() && this.value.substring(this.getCaretPosition() - this.tab.length, this.getCaretPosition()) == this.tab) {
                        const newCaretPosition = this.getCaretPosition() - 3;
                        this.value = this.value.substring(0, this.getCaretPosition() - 3) + this.value.substring(this.getCaretPosition(), this.value.length);
                        this.setCaretPosition(newCaretPosition);
                    }
                    this.emit("removedTab")
                
                    break;
                case "ArrowLeft":
                    if (!this.hasSelection() && !event.shiftKey && this.value.substring(this.getCaretPosition()-this.tab.length, this.getCaretPosition()) == this.tab) {
                        this.setCaretPosition(this.getCaretPosition()-this.tab.length+1);
                    }
                    break;
                case "ArrowRight":
                    if (!this.hasSelection() && !event.shiftKey && this.value.substring(this.getCaretPosition()+this.tab.length, this.getCaretPosition()) == this.tab) {
                        this.setCaretPosition(this.getCaretPosition()+this.tab.length-1);
                    }
                    break;
                case "ArrowUp":
                    if (this.autoCompletionOpened && !event.shiftKey) {
                        this.autoCompletionIndex--
                        this.checkAutoCompletion()
                        event.preventDefault()
                    }
                    break
                case "ArrowDown":
                    if (this.autoCompletionOpened && !event.shiftKey) {
                        this.autoCompletionIndex++
                        this.checkAutoCompletion()
                        event.preventDefault()
                    }
                    break
                }
            }

            if (event.key == 'Enter'){
                if (caretPos == this.value.length)
                    this.textAreaElement.scrollBy(0, this.textAreaElement.scrollHeight)
            }

            if (this.closeKeys !== false){
                for (const key in this.closeKeys) {
                    if (event.key == this.closeKeys[key] && this.value[caretPos-1] == key && this.value[caretPos] == this.closeKeys[key]
                    ) {
                        event.preventDefault()
                        this.setCaretPosition(caretPos+1)
                        break
                    }
                    if (event.key == 'Backspace'
                        && this.value.substring(caretPos-1, caretPos) == key
                        && this.value.substring(caretPos, caretPos+1) == this.closeKeys[key]
                    ) {
                        this.value = this.value.substring(0, caretPos-1)+this.value.substring(caretPos+1, this.value.length)
                        event.preventDefault()
                        this.setCaretPosition(caretPos-1)
                        break
                    }
                    if (event.key == key) {
                        const allowedKeys = ["\n", " ", ...Object.values(this.closeKeys)]
                        if (this.hasSelection()) {
                            const newCaret = caretPos+this.value.substring(this.textAreaElement.selectionStart, this.textAreaElement.selectionEnd).length
                            const oldStart = this.textAreaElement.selectionStart
                            const oldEnd = this.textAreaElement.selectionEnd
                            this.value = this.value.substring(0, this.textAreaElement.selectionStart)+key+this.value.substring(this.textAreaElement.selectionStart, this.textAreaElement.selectionEnd)+this.closeKeys[key]+this.value.substring(this.textAreaElement.selectionEnd, this.value.length)
                            
                            
                            this.setCaretPosition(newCaret+1);
                            this.textAreaElement.select()
                            this.textAreaElement.selectionStart = oldStart+1
                            this.textAreaElement.selectionEnd = oldEnd+1
                            event.preventDefault()
                            this.update()
                            return;
                        } else {
                            if (!(this.value.length > caretPos && !allowedKeys.includes(this.value[caretPos]))) {
                                this.value = this.value.substring(0, caretPos)+key+this.closeKeys[key]+this.value.substring(caretPos, this.value.length)
                                this.setCaretPosition(caretPos+1);
                                event.preventDefault()
                                this.update()
                                return;
                            }
                        }
                    }
                }

                if (event.key == "Enter" && !this.hasSelection()) {
                    let startingSpaces = ""
                    let i = 0
                    let lines = this.value.split("\n")
                    for (let line in lines) {
                        for (let a in lines[line].split("")) {
                            i++
                            if (caretPos == i) {
                                const l = lines[line].split("")
                                for (let b in l) {
                                    if (l[b] == ' ')
                                        startingSpaces += " "
                                    else
                                        break
                                }
                            }
                        }
                        i++
                    }
                    let caretInc = 0
                    if (['{','(','['].includes(this.value.substring(caretPos-1, caretPos))){
                        this.value = this.value.substring(0, caretPos)+"\n"+this.tab+startingSpaces
                            // Check if the closing bracket is right after the caret. If yes, add newline
                            + (this.value.substring(caretPos, caretPos+1) == this.closeKeys[this.value.substring(caretPos-1, caretPos)] ? "\n" : '')
                            + startingSpaces
                            + this.value.substring(caretPos, this.value.length)
                        caretInc = this.tab.length+1
                    } else if (this.newLineTabs.includes(this.value.substring(caretPos-1, caretPos))){
                        this.value = this.value.substring(0, caretPos)+"\n"+this.tab+startingSpaces+this.value.substring(caretPos, this.value.length)
                        caretInc = 5
                    } else {
                        this.value = this.value.substring(0, caretPos)+"\n"+startingSpaces+this.value.substring(caretPos, this.value.length)
                        caretInc = 1
                    }
                    event.preventDefault()
                    this.setCaretPosition(caretPos+startingSpaces.length+caretInc);
                }
            }

            if (this.value != oldValue){
                document.execCommand('insertText',false,this.text);
                this.update()
            }
        })
        const e = ()=>{
            this.updateLineNumbering()
            this.checkAutoCompletion()
        }
        this.textAreaElement.addEventListener("keyup", ev=>{
            if (ev.key == "Escape")
                this.closeAutocompletion()
            if (!ev.shiftKey && !["ArrowDown", "ArrowUp", "ArrowLeft", "ArrowRight", "Escape"].includes(ev.key))
                e()
        })
        this.textAreaElement.addEventListener("click", ()=>{
            e()
            this.closeAutocompletion()
        })
    }

    async checkAutoCompletion(){
        if (this.autoCompleteHandler === null)
            return;
        const val = this.value
        let lastWord = ""
        for (let i = this.getCaretPosition()-1; i > -1; i--) {
            const key = val[i]
            if (key == "\n" || key == " ") {
                break;
            }
            
            lastWord = key+lastWord
        }

        if (this.lastWord != lastWord){
            this.autoCompletionIndex = 0
            this.lastWord = lastWord
        }
        
        let completionsWord;

        if (typeof completionsWord == 'function') {
            completionsWord = this.autoCompleteHandler(lastWord, this)
        } else /*if (completionsWord instanceof AutoCompletion)*/ {
            completionsWord = this.autoCompleteHandler.autoComplete(lastWord, this)
        }

        this.autoCompletionElement.innerHTML = ""
        
        if (completionsWord.length > 0) { //petrel-code-editor-autocompletion
            // Get caret position
            const { offsetLeft: inputX, offsetTop: inputY } = this.textAreaElement
            const div = document.createElement('div')
            const copyStyle = getComputedStyle(this.textAreaElement)
            for (const prop of copyStyle) 
                div.style[prop] = copyStyle[prop]
            const inputValue = this.textAreaElement.value
            const textContent = inputValue.substr(0, this.textAreaElement.selectionEnd)
            div.textContent = textContent
            const span = document.createElement('span')
            span.textContent = inputValue.substr(this.textAreaElement.selectionEnd) || '.'
            div.appendChild(span)
            document.body.appendChild(div)

            this.autoCompletionOpened = true
            this.autoCompletionElement.style.display = "block"

            this.autoCompletionElement.style.top = inputX + span.offsetTop-20+"px"
            this.autoCompletionElement.style.left = inputY + span.offsetLeft+this.leftLineNumPadding+5+"px"

            document.body.removeChild(div)
            
            let i = 0
            for (const completion of completionsWord) {
                const el = document.createElement("a")
                el.innerText = completion.text

                if (completion.type){
                    const spanEl = document.createElement("span")
                    spanEl.innerText = completion.type
                    spanEl.classList.add("type-"+completion.type.toLowerCase())
                    el.appendChild(spanEl)
                }

                el.addEventListener("click", () => {
                    const caretPos = this.getCaretPosition()
                    const val = completion.replace()
                    this.value = this.value.substring(0, caretPos-lastWord.length)+val+this.value.substring(caretPos, this.value.length)
                    this.setCaretPosition(caretPos+val.length-lastWord.length+(completion.cursorMove||0))
                    this.update()
                    this.checkAutoCompletion()
                })
                this.autoCompletionElement.appendChild(el)
                if (this.autoCompletionIndex == i)
                    el.classList.add("selected")
                i++
            }
        } else {
            this.closeAutocompletion()
        }
    }

    closeAutocompletion(){
        this.autoCompletionElement.style.display = "none"
        this.autoCompletionOpened = false
    }

    emit(event, ...values){}
    on(event, callable){}

    updateLineNumbering(){
        this.lineNumerationsElement.innerHTML = ""
        const line = this.getCaretLine()
        for (let i in this.value.split("\n")) {
            i = Number(i)
            const el = document.createElement("span")
            
            if (line == i){
                const el2 = document.createElement("div")
                el.appendChild(el2)
            }
            el.appendChild(document.createTextNode(i+1))
            this.lineNumerationsElement.appendChild(el)
        }
    }

    getCaretLine(){
        const caretPos = this.getCaretPosition()
        const val = this.value
        let line = 0
        let i = 0
        for (const c of val.split("")) {
            if (caretPos == i)
                break;
            if (c == '\n')
                line++;
            i++;
        }
        return line
    }

    update(){
        if (!this.lineNumberingDisabled)
            this.updateLineNumbering()
        this.codePreElement.innerHTML = this.highlighter(this.value)
        
        const leftPadding = (this.lineNumberingDisabled ? 0 : this.lineNumerationsElement.offsetWidth)+this.codeRightPadding
        this.leftLineNumPadding = leftPadding
        this.textAreaElement.style.marginLeft = leftPadding+'px'
        this.codePreElement.style.marginLeft  = leftPadding+'px'

        this.textAreaElement.style.height = '0px'
        this.textAreaElement.style.height = this.textAreaElement.scrollHeight+'px'
        this.textAreaElement.style.width = '0px'
        this.textAreaElement.style.width = this.textAreaElement.scrollWidth+'px'
    }

    create(){
        this.parentElement.classList.add("petrel-code-editor")
        this.parentElement.appendChild(this.lineNumerationsElement)

        this.parentElement.appendChild(this.textAreaElement)
        this.parentElement.appendChild(this.codePreElement)
        this.parentElement.appendChild(this.autoCompletionElement)
        this.autoCompletionElement.style.display = "none"
        this.update()
    }


    setHighlighter(highlighter){
        this.highlighter = highlighter
        return this
    }

    setAutoCompleteHandler(autoCompleteHandler){
        this.autoCompleteHandler = autoCompleteHandler
        return this
    }

    setValue(val){
        this.value = val
        this.update()
        return this
    }

    getValue(){
        return this.value
    }

    set value(to){
        this.textAreaElement.value = to
    }

    get value(){
        return this.textAreaElement.value
    }

    set readonly(to){
        this.textAreaElement.readOnly = to
    }
    get readonly(){
        return this.textAreaElement.readOnly
    }

    focus(){
        this.textAreaElement.focus()
    }

    getCaretPosition(){
        return this.textAreaElement.selectionStart
    }

    setCaretPosition(position) { //change the caret position of the textarea
        this.textAreaElement.selectionStart = position;
        this.textAreaElement.selectionEnd = position;
        this.focus();
    };
    hasSelection() {
        return this.textAreaElement.selectionStart != this.textAreaElement.selectionEnd
    };
    getSelectedText() {
        return this.value.substring(this.textAreaElement.selectionStart, this.textAreaElement.selectionEnd);
    };
    setSelection(start, end) {
        this.textAreaElement.selectionStart = start;
        this.textAreaElement.selectionEnd = end;
        this.textAreaElement.focus();
    };

}