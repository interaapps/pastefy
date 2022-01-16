import AutoCompletion from "../AutoCompletion.js"

export default class JavaScriptAutoComplete extends AutoCompletion {

    constructor(environment = {}){
        super()
        this.defaultFunctions = environment.functions ? environment.functions : []
        this.defaultVariables = environment.variables ? environment.variables : []
        this.prependSource = environment.prependSource ? environment.prependSource+"\n\n" : ''

        if (!environment.disableAutoLoad){
            for (const name in window) {
                const val = window[name]
                if (typeof val == 'function'){
                    // let matchParams = val.toString().match(/\([^)]*\)/)
                    // matchParams = matchParams ? matchParams[0].substring(1).substring(0,matchParams[0].length-2) : null
                    
                    this.defaultFunctions.push({
                        name: name,
                        // paramsLength: val.length,
                        // params: matchParams ? matchParams.split(",").map(a=>a.trim()) : []
                    })
                } else
                    this.defaultVariables.push(name)
            }
        }
    }

    analyseCode(value){
        const variables = [...this.defaultVariables]
        const functions = [...this.defaultFunctions]

        const val = this.prependSource+value
        for (const varRes of val.matchAll(/(^| |\n)(const|let|var) (\s*?)([A-Za-z0-9]*)(\s*?)(=|;|\n|$)/gm))
            variables.push(varRes[4])
        
        for (const varRes of val.matchAll(/(^|;(\s*)?|\n)(class) (\s*?)([A-Za-z0-9]*)(\s*?)(extends (\s*?)[A-Za-z0-9]*)?(\s*?)({|\n|$)/gm))
            variables.push(varRes[5])

        for (const varRes of val.matchAll(/(^|;(\s*)?|\n)(function) (\s*?)([A-Za-z0-9]*)(\s*?)((\s*?)(\(([^(]*))\))(\s*?){/gm)){
            let params = varRes[10].split(",").map(a=>a.trim())
            if (params[0] == '')
                params = []
            functions.push({
                params: params,
                paramsLength: params.length,
                name: varRes[5]
            })
        }
        return {variables, functions}
    }

    autoComplete(word, editor){
        const searchWord = word.replaceAll(/\(|{|;/g, "")
        
        const ret = []
        if (searchWord == "")
            return []

        const { variables, functions } = this.analyseCode(editor.value)

        
        for (const key of JavaScriptAutoComplete.KEYWORDS) {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){
                ret.push({
                    text: key,
                    type: 'KEYWORD',
                    replace: ()=> key+" " 
                })
            }
        }
        
        for (const key in JavaScriptAutoComplete.KEYWORDS_OWN_LOGIC) {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){
                ret.push({
                    text: key,
                    type: 'KEYWORD',
                    replace: ()=> key,
                    ...JavaScriptAutoComplete.KEYWORDS_OWN_LOGIC[key](key)
                })
            }
        }

        variables.forEach(key => {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){
                ret.push({
                    text: key,
                    type: 'VARIABLE',
                    replace: ()=> key
                })
            }
        })
        functions.forEach(fn => {
            if (fn.name.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== fn.name){
                ret.push({
                    text: fn.name,
                    type: 'FUNCTION',
                    replace: ()=> fn.name+"("+(fn.paramsLength > 0 ? fn.params.join(", ") : '')+")",
                    cursorMove: fn.paramsLength > 0 ? -1 : 0
                })
            }
        })
        return ret
    }
    
}
JavaScriptAutoComplete.KEYWORDS_OWN_LOGIC = {
    'function () {\n    \n}': key=>({
        text: "function",
        cursorMove: -11,
        type: "SHORTCUT"
    }),
    'if () {\n    \n}': key=>({
        text: "if",
        replace: ()=>key,
        cursorMove: -10,
        type: "SHORTCUT"
    }),
    'else if () {\n    \n}': key=>({
        text: "else if",
        cursorMove: -10,
        type: "SHORTCUT"
    }),
    'else {\n    \n}': key=>({
        text: "else",
        cursorMove: -3,
        type: "SHORTCUT"
    }),
    'console.log()': key=>({
        text: "console.log(...)",
        cursorMove: -1,
        type: "SHORTCUT"
    })
}

JavaScriptAutoComplete.KEYWORDS = ["await", "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "enum", "export", "extends", "false", "finally", "for", "implements", "import", "in", "instanceof", "interface", "let", "new", "null", "package", "private", "protected", "public", "return", "super", "switch", "static", "this", "throw", "try", "true", "typeof", "var", "void", "while", "with", "yield"]