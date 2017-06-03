function getAbsoluteFrom(url){
  if(url.startsWith('http:') || url.startsWith('https:') || url.startsWith('data')){
    return url;
  }
  if(url.startsWith('//')){
    return window.location.protocol+url;
  }
  var prefix = window.location.protocol + "//" + window.location.host + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")+1);
  return prefix + url;
}
return getAbsoluteFrom(arguments[0]);