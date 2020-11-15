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
  constructor(text="", color="#17fc2e", background="#222530"){
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
      this.element.innerHTML   = this.text
    else
      this.element.textContent = this.text
    
    this.onclose = ()=>{}
    this.onopen = ()=>{}
  }

  open(){
    if (this.useHTML)
      this.element.innerHTML   = this.text
    else
      this.element.textContent = this.text
    this.element.style.color = this.color
    this.element.style.backgroundColor = this.background
    this.element.classList.add('show');

    document.body.appendChild(this.element)
    this.timer = setTimeout(()=>{
      this.timer = null
      this.close()
    }, this.timeout)

    this.onopen()
  }

  close(){
    if (this.timer != null)
      clearTimeout(this.timer)
    this.element.classList.remove('show');
    this.onclose()
    setTimeout(()=>document.body.removeChild(this.element), 300)
  }
}

let bottomMargin = 24
function showSnackBar(text="", color="#17fc2e", background="#222530", open = true) {
  console.log(text);
  const snackbar = new Toast(text, color, background)
  if (open) {
    snackbar.open()
    bottomMargin += snackbar.element.clientHeight + 8
    snackbar.onopen = ()=>{
      snackbar.element.style.bottom = bottomMargin+"px"
      console.log("ADDING");
    }
    snackbar.onclose = ()=>{
      bottomMargin -= snackbar.element.clientHeight + 8
    }
  }
  
  return snackbar
}


export default {copyStringToClipboard, showSnackBar, Toast}