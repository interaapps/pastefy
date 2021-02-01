importScripts("./assets/js/cajax.js")


let user
let storage = {}
let session = ""
let baseUrl = ""

self.addEventListener("message", (e)=>{
  const data = e.data
  
  if (data.action == 'updateStorage') {
	storage = {...storage, ...data.storage}
  } else if (e.data.action == 'registerUserLogin') {
    user = e.data.user
    session = e.data.session
    baseUrl = e.data.baseUrl
    setInterval(() => {
      console.log("yepee");
      Prajax.get(baseUrl+"/api/v2/user/notification", {
        not_received: "true"
      }, {header: {
        "x-auth-key": session
      }}).then(res=>{
        for (let notification of res.json()){
          self.postMessage({
            action: 'notification',
            title: 'Received paste!',
            url: notification.url,
            message: notification.message
          })
          if (storage.notificationsEnabled) {
            let browserNotification = new Notification("Pastefy - Shared paste", {
              body: notification.message
			      });
			
            browserNotification.onclick = ()=>{
              console.log(self);
              self.location = notification.url
              self.postMessage({
                action: 'pushRoute',
                url: notification.url
              })
            }

          }
        }
      })
    }, 5000);    
  }
})
