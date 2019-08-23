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

