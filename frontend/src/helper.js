import hljs from "highlight.js";
import LANGUAGE_REPLACEMENTS from "@/assets/data/langReplacements";

function copyStringToClipboard(str) {
    var el = document.createElement('textarea');
    el.value = str;
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
}


class Toast {
    constructor(text = "", color = "#17fc2e", background = "#222530") {
        this.text = text
        this.color = color
        this.background = background
        this.element = null
        this.timer = null
        this.timeout = 2600
        this.useHTML = false
        this.element = document.createElement("span");
        this.element.id = 'snackbar'
        if (this.useHTML)
            this.element.innerHTML = this.text
        else
            this.element.textContent = this.text

        this.onclose = () => {
        }
        this.onopen = () => {
        }
    }

    open() {
        if (this.useHTML)
            this.element.innerHTML = this.text
        else
            this.element.textContent = this.text
        this.element.style.color = this.color
        this.element.style.backgroundColor = this.background
        this.element.classList.add('show');

        document.body.appendChild(this.element)

        this.onopen()

        this.timer = setTimeout(() => {
            this.timer = null
            this.close()
        }, this.timeout)
    }

    close() {
        if (this.timer != null)
            clearTimeout(this.timer)
        this.element.classList.remove('show');
        this.onclose()
        setTimeout(() => document.body.removeChild(this.element), 300)
    }
}

let bottomMargin = 24

function showSnackBar(text = "", color = "#17fc2e", background = "#222530", open = true) {
    const snackbar = new Toast(text, color, background)

    if (open) {
        snackbar.onopen = () => {
            snackbar.element.style.bottom = bottomMargin + "px"
            bottomMargin += snackbar.element.clientHeight + 8
        }

        snackbar.open()

        snackbar.onclose = () => {
            bottomMargin -= snackbar.element.clientHeight + 8
        }
    }

    return snackbar
}

export function buildSearchAndFilterQuery(q) {
    const query = {}

    if (q.includes("=")) {
        const split = q.split("=")
        query[`filter[${ split[0] }]`] = split[1]
    } else if (q) {
        query.search = q
    }

    return query
}

const languages = [...hljs.listLanguages(), "text"];

export function getLanguageByFileName(fileName) {
    const fileNameExtension = fileName.split(".");
    let ending = fileNameExtension[fileNameExtension.length - 1];

    let language = null;

    for (let replace in LANGUAGE_REPLACEMENTS) {
        if (ending == replace) {
            ending = LANGUAGE_REPLACEMENTS[replace];
            break;
        }
    }

    if (languages.includes(ending)) {
        language = ending;
    }

    return [ending, language]
}

export default {copyStringToClipboard, showSnackBar, buildSearchAndFilterQuery, Toast}