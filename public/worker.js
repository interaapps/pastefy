importScripts("/assets/js/cajax.js")


let user
let storage = {}


self.addEventListener("message", (e)=>{
  const data = e.data
  
  if (data.action == 'updateStorage') {
	storage = {...storage, ...data.storage}
  } else if (e.data.action == 'registerUserLogin') {
	  console.log(e);
    console.log("User is logged in!");
    user = e.data.user
    setInterval(() => {
      Prajax.get("/notifications", {
        not_received: "true"
      }).then(res=>{
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
