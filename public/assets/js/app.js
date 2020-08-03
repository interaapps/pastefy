/*
    JDOM IS A SIMPLE DOM SELECTOR WITH FUNCTIONS!
    This is not finished! If you want to add something, just do it!
*/


class jdom {
    constructor(element, parent=undefined) {
        if (typeof parent=='undefined')
            parent = document;

        this.usign = "queryselector";
        if (element instanceof HTMLElement || element===document  || element===window) {
            this.elem = element;
            this.usign = "htmlelement";
        } else if (element instanceof jdom) {
            this.elem = element.elem;
            this.usign = "jdom";
        } else
            this.elem = parent.querySelectorAll(element);

        this.$ = function(element){
            if (typeof this.elem[0] !== 'undefined')
                    return (new jdom(element, this.elem[0]));
            return (new jdom(element, this.elem));
        }
    }

    each(func) {
        if (this.usign == "htmlelement")
            func(this.elem);
        else
            [].forEach.call(this.elem, func);
    }

    getFirstElement() {
        if (this.usign == "htmlelement")
            return this.elem;
        else if (typeof this.elem[0] != 'undefined')
            return this.elem[0];
        return undefined;
    }


    html(html) {
    	if (typeof html == 'undefined') {
            var element = this.getFirstElement();
    	    if (typeof element !== 'undefined')
                return element.innerHTML;
            return "";
        } else {
            this.each( function (element) { element.innerHTML = html; });
            return this;
        }
    }

    text(text) {
        if (typeof text == 'undefined') {
            var element = this.getFirstElement();
            if (typeof element !== 'undefined')
                return element.innerText;
            return "";
        } else {
            this.each( function (element) { element.innerText = text; });
            return this;
        }
    }

    css(css={}, alternativeValue=undefined) {
        if (typeof css == "string" && typeof alternativeValue == 'undefined') {
            var element = this.getFirstElement();
            if (typeof element.style[css] !== 'undefined')
                return element.style[css];
            return "";
        } else
            this.each( function (element) {
                if (typeof css == "string" && typeof alternativeValue != 'undefined') {
                    element.style[css] = alternativeValue;
                } else {
                    for (var styleAttr in css)
                        element.style[styleAttr] = css[styleAttr];
                }
            });
        return this;
    }

    attr(attributes={}, alternativeValue=undefined) {
        if (typeof attributes == "string" && typeof alternativeValue == 'undefined') {
            var element = this.getFirstElement();

            if (typeof element !== 'undefined')
                return element.getAttribute(attributes);
        } else
            this.each( function (element) {
                if (typeof attributes == "string" && typeof alternativeValue != 'undefined') {
                    element.setAttribute(attributes, alternativeValue);
                } else {
                    for (var attribute in attributes)
                        element.setAttribute(attribute, attributes[attribute]);
                }
            });
        return this;
    }

    removeAttr(name) {
        this.each(function(element) {
            element.removeAttribute(name);
        });
        return this;
    }

    addClass(name) {
        this.each( function (element) {
            element.classList.add(name);
        });
        return this;
    }

    removeClass(name) {
        this.each( function (element) {
            element.classList.remove(name);
        });
        return this;
    }

    id(name) {
        if (typeof name == 'undefined') {
            var element = this.getFirstElement();
            if (typeof element !== 'undefined')
                return element.id;
        } else {
            this.each(function(element) {
                element.id = name;
            });
        }
        return this;
    }

    val(value) {
        if (typeof value == 'undefined') {
            var element = this.getFirstElement();
            if (typeof element !== 'undefined')
                return element.value;
        } else {
            this.each(function(element) {
                element.value = value;
            });
        }
        return this;
    }

    append(append) {
        if (append instanceof HTMLElement)
            this.each( function (element) {
                element.appendChild(append);
            });
        else if (append instanceof jdom)
            this.each( function (element) {
                element.appendChild(append.elem);
            });
        else {
            var outerThis = this;
            this.each( function (element) {
                outerThis.html(outerThis.html() + append);
            });
        }
        return this;
    }
    
    prepend(prepend) {
        if (prepend instanceof HTMLElement)
            this.each( function (element) {
                element.prepend(prepend);
            });
        else if (prepend instanceof jdom)
            this.each( function (element) {
                element.prepend(prepend.elem);
            });
        else {
            var outerThis = this;
            this.each( function (element) {
                outerThis.html(prepend+outerThis.html());
            });
        }
        return this;
    }


    getElem(){
    	return this.elem;
    }

    on(what, func, option) {
	    this.each( function(element){
    	    element.addEventListener(what,func);
        }, option);
	    return this;
    }

    rmEvent(what, func) {
        this.each(function(element) {
            element.removeEventListener(what, func);
        });
    }

    bind(binds={}) {
	    this.each( function(element){
            for (var bind in binds)
    	        element.addEventListener(bind, binds[bind]);
        });
	    return this;
    }
    
    click(func){ 
        if (typeof func != 'undefined')
            this.on('click', func);
        else
            (this.getFirstElement()).click();

        return this;
    }
    
    contextmenu(func) { return this.on('contextmenu', func); }
    change(func) { return this.on('change', func); }
    mouseover(func) { return this.on('mouseover', func); }
    keypress(func) { return this.on('keypress', func); }
    keyup(func) { return this.on('keyup', func); }
    keydown(func) { return this.on('keydown', func); }
    dblclick(func) { return this.on('dblclick', func); }
    resize(func) { return this.on('resize', func); }

    timeupdate(func) { return this.on('timeupdate', func); }
    touchcancle(func) { return this.on('touchcancle', func); }
    touchend(func) { return this.on('touchend', func); }
    touchmove(func) { return this.on('touchmove', func); }
    touchstart(func) { return this.on('touchstart', func); }

    drag(func) { return this.on('drag', func); }
    dragenter(func) { return this.on('dragenter', func); }
    dragleave(func) { return this.on('dragleave', func); }
    dragover(func) { return this.on('dragover', func); }
    dragend(func) { return this.on('dragend', func); }
    dragstart(func) { return this.on('dragstart', func); }
    drop(func) { return this.on('drop', func); }

    focus(func) { return this.on('focus', func); }
    focusout(func) { return this.on('focusout', func); }
    focusin(func) { return this.on('focusin', func); }
    invalid(func) { return this.on('invalid', func); }
    popstate(func) { return this.on('popstate', func); }
    volumechange(func) { return this.on('volumechange', func); }
    unload(func) { return this.on('unload', func); }
    offline(func) { return this.on('offline', func); }
    online(func) { return this.on('online', func); }
    focus(func) { return this.on('focus', func); }

    ready(func) {
        this.on('DOMContentLoaded', func);
        return this;
    }

    hide() {
        this.each( function(element){
            element.style.display = "none";
        });
        return this;
    }

    show() {
        this.each( function(element){
            element.style.display = "";
        });
        return this;
    }

    toggle() {
        this.each( function(element){
            if (element.style.display == "none")
                element.style.display = "";
            else
                element.style.display = "none";
        });
        return this;

    }

    animate(css={}, duration=1000, then=function(){}) {
        this.css("transition", duration+"ms");
        this.css(css);
        setTimeout(function() {
            then();
        }, duration);
        return this;
    }

    animator(animations=[], async = false){
        var counting = 0;
        var outerThis = this;
        for (var animation in animations) {
            const css = typeof animations[animation].css != 'undefined' ? animations[animation].css : {};
            const then = typeof animations[animation].then != 'undefined' ? animations[animation].then : function () {};
            const duration = typeof animations[animation].duration != 'undefined' ? animations[animation].duration : 1000;
            setTimeout(function(){
                outerThis.animate(css, duration, then);
            }, counting);
            if (!async)
                counting += duration;
        }
        return this;
    }

    static noConflict() {
        var $ = _$beforeJdom;
        var $n = _$nBeforeJdom;
        var $$ = _$$beforeJdom;
    }
    
}

if (typeof $ != 'undefined')
var _$beforeJdom  = $;
if (typeof $n != 'undefined')
    var _$nBeforeJdom = $n;
if (typeof $$ != 'undefined')
    var _$$beforeJdom = $$;

$jdomfn = function(name, func){
	jdom.prototype[name] = func;
}

$jdomGetter = function(varName){
	varNameArray = varName.split("");
	if (varNameArray[0] !== undefined)
		varNameArray[0] = varName[0].toUpperCase();
	var out = "";
	for (letter in varNameArray)
		out += varNameArray[letter];
	jdom.prototype["get"+out] = function(){
		return this.getFirstElement()[varName];
	}
}

$jdomSetter = function(varName){
	varNameArray = varName.split("");
	if (varNameArray[0] !== undefined)
		varNameArray[0] = varName[0].toUpperCase();
	var out = "";
	for (letter in varNameArray)
		out += varNameArray[letter];
	jdom.prototype["set"+out] = function(value){
		this.each(function(elem){
			elem[varName] = value;
		});
		return this;
	}
}

var $ = function(element){
    return (new jdom(element));
}

var $jdom = function(element){
    return (new jdom(element));
}

var $n = function(element="div"){
    return (new jdom(document.createElement(element)));
}

var $$ = function (element) {
    return document.querySelectorAll(element);
}


if ( typeof module === "object" && typeof module.exports === "object" ) {
    module.exports = $;
}
class CajaxRequest {
    constructor(url,method, data=null, options={},usinginput=false) {
        // INIT        
        this.onResponseFunction = ()=>{};
        this.catchFunction = ()=>{};
        this.thenFunction = ()=>{};
    
        if (data != null) {        
            var urlEncodedData = "";
            var urlEncodedDataPairs = [];
            var name;
            for(name in data) {
                urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
            }
            this.data = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
        } else this.data = null;
        this.method = method;
        this.contenttype = (usinginput) ? "application/json; charset=utf-8" : "application/x-www-form-urlencoded";
        
        var xhr = new XMLHttpRequest();
        
        if (options != null) 
            for (var options_key__cajax in options) {
                xhr[options_key__cajax] = options[options_key__cajax];
            }
            
        
        xhr.open(method, url+((this.method=="GET")? "?"+this.data : "" ));
        if (options.header != null) for (var requestheader_obj__cajax in options.header) {
            xhr.setRequestHeader(requestheader_obj__cajax, options.header[requestheader_obj__cajax]);
        }
        
        xhr.setRequestHeader('Content-type', this.contenttype);
        this.request = xhr;
        if (usinginput && data != null) this.data = JSON.stringify(data);
    }
    
    response (func) {
        this.onResponseFunction = func;
        return this;
    }
    
    then (func) {
        this.thenFunction = func;
        return this;
    }
    
    catch (func) {
        this.catchFunction = func;
        return this;
    }
    
    custom (func) {
        func(this.request);
        return this;
    }
    
    send () {
    
        (this.request).onload = () => {
            this.onResponseFunction(this.request);
            if ((this.request).readyState == 4 && ((this.request).status == "200" || (this.request).status == "201")) {
		        this.thenFunction((this.request));
	        } else {
		        this.catchFunction((this.request));
	        }
        };

        (this.request).send(this.data);
        return this;
    }
}

class Cajax {
    
    static post(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "POST", data, options, usinginput);
    }
    
    static get(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "GET", data, options, usinginput);
    }
    
    static put(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "POST", data, options, usinginput);
    }
    
    static delete(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "DELETE", data, options, usinginput);
    }
    
    static trace(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "TRACE", data, options, usinginput);
    }
    
    static connect(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "CONNECT", data, options, usinginput);
    }
    
    static options(url, data={}, options={}, usinginput=false) {
        return new CajaxRequest(url, "OPTIONS", data, options, usinginput);
    }
    
    static ajax (json) {
        return new CajaxRequest(
        ((json.url != null) ? json.url : false ), 
        ((json.method != null) ? json.method : false ), 
        ((json.options != null) ? json.options : false ), 
        ((json.data != null) ? json.data : false ),
        ((json.input != null) ? json.input : false ));
    }
}

// Function to copy contents
function copyStringToClipboard(str) {
    // Create new element
    var el = document.createElement('textarea');
    // Set value (string to be copied)
    el.value = str;
    // Set non-editable to avoid focus and move outside of view
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    // Select text inside element
    el.select();
    // Copy text to clipboard
    document.execCommand('copy');
    document.body.removeChild(el);
 }
 

  let snackBarTimeout;
  
  function showSnackBar(text, color="#17fc2e", background="#222530") {
    const snackbar = document.querySelector('#snackbar');
    snackbar.textContent = text;
    snackbar.style.color = color;
    snackbar.style.backgroundColor = background;
    snackbar.classList.add('show');
    clearTimeout(snackBarTimeout);
    snackBarTimeout = setTimeout(() => {
      snackbar.classList.remove('show');
    }, 1500);
  }
 
 function openNav() {
     //if (screen.width <= 720) {
         document.getElementById("navbar").style.width = "100%";
     /*} else {
         document.getElementById("navbar").style.width = "250px";
     }*/
 }
 
 function closeNav() {
     document.getElementById("navbar").style.width = "0px";
 }
 
 $(document).ready(function(){
    let s = document.createElement('div');
    s.id = 'snackbar';
    s.textContent = 'done';
    document.body.append(s);


    $("#short_new_paste").click(function(e){
        e.stopPropagation();
    });

    $(window).click(function(e){
        if (window.innerWidth >= 720 && e.target.id != "create_new_paste") {
            if ($("#short_new_paste").css("display") == "block"){
                event.preventDefault();
                $("#short_new_paste").css("display", "none");
            }
        }
    });

    $(window).keydown(function(e){
        if (e.key == "Escape" && $("#short_new_paste").css("display") == "block")
            $("#short_new_paste").css("display", "none");
        else if (e.key == "N" && window.location.pathname != "/" && $("#short_new_paste").css("display") == "none") // Open the new paste window
            $("#short_new_paste").css("display", "block");
    });

    $("#create_new_paste").click(function(e){
        if (window.innerWidth >= 720) {
            if ($("#short_new_paste").css("display") == "none")
                $("#short_new_paste").css("display", "block");
            else
                $("#short_new_paste").css("display", "none");
            e.preventDefault();
        }
    });

 });

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