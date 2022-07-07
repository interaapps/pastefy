function createConsole(theme, opened = true, height = '300px'){
    const style = (el, st) => {
        for (const key in st)
            el.style[key] = st[key]
    }

    const consoleEl     = document.createElement("div")
    const consoleText   = document.createElement("div")
    const consoleInput  = document.createElement("input")
    const consoleToggle = document.createElement("a")

    style(consoleEl, {
        height,
        background: theme['--background-color'],
        color: theme['--text-color'],
        position: 'fixed',
        bottom: '0px',
        width: '100%',
        borderTop: '2px solid #00000011',
        boxSizing: 'border-box',
        display: opened ? '' : 'none'
    })

    style(consoleText, {
        height: 'calc(100% - 30px)',
        overflow: 'auto'
    })

    consoleInput.placeholder = 'Console. Click enter to submit'
    style(consoleInput, {
        width: '100%',
        display: 'block',
        height: '30px',
        color: theme['--text-color'],
        background: theme['--obj-background-color'],
        border: 'none',
        overflow: 'none',
        padding: "0px 9px",
        borderTop: '2px solid '+theme["--tab-color"]
    })

    consoleToggle.innerText = opened ? 'Close' : 'Console'

    consoleToggle.onclick = () => {
        opened = !opened
        consoleToggle.innerText = opened ? 'Close' : 'Console'
        style(consoleToggle, {
            bottom: opened ? height : '0px'
        })
        style(consoleEl, {
            display: opened ? '' : 'none'
        })
    }

    style(consoleToggle, {
        position: "fixed",
        right: "0px",
        cursor: 'pointer',
        bottom: opened ? height : '0px',
        padding: '5px',
        background: '#00000011',
        borderRadius: '5px 0px 0px 0px'
    })


    consoleEl.appendChild(consoleText)
    consoleEl.appendChild(consoleInput)

    const log = (logs, styles = {}) => {
        for (const log of logs) {
            const logEl = document.createElement("pre")
            logEl.textContent = typeof log == 'string' ? log : JSON.stringify(log, null, 4)
            style(logEl, {
                margin: '0px',
                padding: '8px 11px',
                borderBottom: '1px solid '+theme['--page-background-color'],
                ...styles
            })
            consoleText.appendChild(logEl)
        }
    }

    window.console.log = function(...logs){
        log(logs)
    }
    window.console.table = window.console.log
    window.console.debug = window.console.log

        window.console.error = function(...logs){
        log(logs, {
            color: '#FF0000',
            borderLeft: '#FF0000 2px solid',
            background: '#FF000011'
        })
    }

    window.console.info = function(...logs){
        log(logs, {
            color: '#ff5900',
            borderLeft: '#ff5900 2px solid',
            background: '#ff590011'
        })
    }

    window.require = () => {
        console.error(`The require function does not exist because this is not a node.js environment.`)
    }

    consoleInput.onkeydown = e => {
        if (e.key == 'Enter') {
            try {
                console.log((window.evalReplace || eval)(e.target.value))
            } catch (e) {
                console.error(e.message)
            }
            e.target.value = ''
        }
    }

    document.addEventListener("DOMContentLoaded", ()=>{
        document.body.appendChild(consoleEl)
        document.body.appendChild(consoleToggle)
    })
}

createConsole;