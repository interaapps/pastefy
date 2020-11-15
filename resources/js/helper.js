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
    this.onclose = ()=>{}
  }

  open(){
    this.element = document.createElement("span");
    this.element.id = 'snackbar'
    this.element.textContent = this.text
    this.element.style.color = this.color
    this.element.style.backgroundColor = this.background
    this.element.classList.add('show');

    document.body.appendChild(this.element)
    this.timer = setTimeout(()=>{
      this.timer = null
      this.close()
    }, this.timeout)
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
function showSnackBar(text="", color="#17fc2e", background="#222530") {
  console.log(text);
  const snackbar = new Toast(text, color, background)
  snackbar.open()
  snackbar.element.style.bottom = bottomMargin+"px"
  bottomMargin += snackbar.element.clientHeight + 8
  console.log("LOGS:");
  console.log(bottomMargin);
  console.log(snackbar.element.style.bottom);
  snackbar.onclose = ()=>{
    bottomMargin -= snackbar.element.clientHeight + 8
  }
  return snackbar
}


export default {copyStringToClipboard, showSnackBar}