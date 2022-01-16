import AutoCompletion from "../AutoCompletion.js"

export default class JavaAutoComplete extends AutoCompletion {

    constructor(environment = {}){
        super()
        this.defaultFunctions = environment.variables ? environment.functions : []
        this.defaultVariables = environment.variables ? environment.variables : []
        this.defaultClasses = environment.classes ? environment.classes : []
        this.prependSource = environment.prependSource ? environment.prependSource+"\n\n" : ''
    }

    analyseCode(code){
        const variables = [...this.defaultVariables]
        const functions = [...this.defaultFunctions]
        const classes = [...this.defaultClasses]

        const val = this.prependSource+code
        for (const varRes of val.matchAll(/(^| |\n)((public|private|protected) )?((native|transient|volatile|static) )?([a-zA-Z_$][a-zA-Z_$0-9]*(<[a-zA-Z_$][a-zA-Z_$0-9]*?>)?) (\s*?)([a-zA-Z_$\.][a-zA-Z_$0-9\.]*)(\s*?)(=((\s*?)[^;]*)|;|\n|$)/gm)) {
            if (varRes[6] == 'import') {
                const split = varRes[9].split(".")
                classes.push({
                    access: '',
                    type: 'imported',
                    name: split.length == 1 ? split[0] : split[split.length-1]
                })
            } else {
                variables.push({
                    access: varRes[3],
                    type: varRes[6],
                    name: varRes[9],
                    value: varRes[12]
                })
            }
        }

        for (const varRes of val.matchAll(/(^| |\n)((public|private|protected) )?((native|transient|volatile|static) )?([a-zA-Z_$][a-zA-Z_$0-9]*(<[a-zA-Z_$][a-zA-Z_$0-9]*?>)?) (\s*?)([a-zA-Z_$][a-zA-Z_$0-9]*)(\s*?)(\s*?)(\(([a-zA-Z_$0-9 ,]*)\))|\{.*\n(|;|\n|$)/gm)){
            if (typeof varRes[10] != 'undefined' && typeof varRes[13]  != 'undefined' && typeof varRes[6] != 'undefined'){
                let params = varRes[13].split(",").map(a=>a.trim().replaceAll(/([a-zA-Z_$][a-zA-Z_$0-9]*(<.*>)?) ([a-zA-Z_$][a-zA-Z_$0-9]*)/gi, (_,g1,g2,g3)=>g3))
                if (params[0] == '')
                    params = []
                functions.push({
                    access: varRes[3],
                    type: varRes[6],
                    params: params,
                    paramsLength: params.length,
                    name: varRes[9]
                })
            }
        }
        for (const varRes of val.matchAll(/(^| |\n)((public|private|protected) )?((native|transient|volatile|static) )?(class|abstract class|enum|interface|@interface) (([a-zA-Z_$][a-zA-Z_$0-9]*)(<([a-zA-Z_$][a-zA-Z_$0-9])*?>)?)(\s*?)( extends ([a-zA-Z_$][a-zA-Z_$0-9]*((\s*?)<([a-zA-Z_$][a-zA-Z_$0-9])*?>)?))?( implements ([a-zA-Z_$][a-zA-Z_$0-9]*((\s*?)<([a-zA-Z_$][a-zA-Z_$0-9])*?>)?))?(\s*?)(|;|\n|$)/gm)){
            if (typeof varRes[3] != 'undefined' && typeof varRes[6]  != 'undefined' && typeof varRes[8] != 'undefined'){
                classes.push({
                    access: varRes[3],
                    type: varRes[6],
                    name: varRes[8]
                })
            }
        }

        return {variables, functions, classes}
    }

    autoComplete(word, editor){
        const searchWord = word.replaceAll(/\(|{|;/g, "")
        
        const ret = []
        if (searchWord == "")
            return []

        const {variables, functions, classes} = this.analyseCode(editor.value)
        
        for (const key of JavaAutoComplete.KEYWORDS) {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){
                ret.push({
                    text: key,
                    type: 'KEYWORD',
                    replace: ()=> key+" " 
                })
            }
        }
        
        for (const key in JavaAutoComplete.KEYWORDS_OWN_LOGIC) {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){
                ret.push({
                    text: key,
                    type: 'KEYWORD',
                    replace: ()=> key,
                    ...JavaAutoComplete.KEYWORDS_OWN_LOGIC[key](key)
                })
            }
        }

        variables.forEach(va => {
            if (va.name.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== va.name){
                ret.push({
                    text: va.name+': '+va.type,
                    type: 'VARIABLE',
                    replace: ()=> va.name
                })
            }
        })
        functions.forEach(fn => {
            if (fn.name.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== fn.name){
                const fnN = fn.name+"("+(fn.paramsLength > 0 ? fn.params.join(", ") : '')+")"+(fn.type == 'void' ? ';' : '')
                ret.push({
                    text: fnN.replace(";",'')+": "+fn.type,
                    type: 'FUNCTION',
                    replace: ()=>fnN,
                    cursorMove: fn.paramsLength > 0 ? -1 : 0
                })
            }
        })
        classes.forEach(cl => {
            if (cl.name.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== cl.name){
                ret.push({
                    text: cl.name+": "+cl.type,
                    type: 'CLASS',
                    replace: ()=>cl.name
                })
            }
        })
        return ret
    }
    
}
JavaAutoComplete.KEYWORDS_OWN_LOGIC = {
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
    'sout': key=>({
        text: "sout",
        replace: ()=>'System.out.println();',
        cursorMove: -2,
        type: "SHORTCUT"
    })
}

JavaAutoComplete.KEYWORDS = ["abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"]