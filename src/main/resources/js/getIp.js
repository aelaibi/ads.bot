function pixiGetPublicIp(url){
    var pixiXHR = new XMLHttpRequest();

    try{
        pixiXHR.open('GET', url, false);
        pixiXHR.send(null);
        if (pixiXHR.status == 200) {
            var pixiIP = JSON.parse(pixiXHR.responseText);
            return pixiIP.ip;
        } else {
            return "response_error";
        }
    } catch(e) {
        return "call_error-"+e;
    }
}
return pixiGetPublicIp('https://jsonip.com/');