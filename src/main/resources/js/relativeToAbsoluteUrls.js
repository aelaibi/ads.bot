var src = Array.prototype.slice.call(document.getElementsByTagName('script'))
        .concat(Array.prototype.slice.call(document.getElementsByTagName('img')))
        .concat(Array.prototype.slice.call(document.getElementsByTagName('source')))
        .concat(Array.prototype.slice.call(document.getElementsByTagName('embed')))
        .concat(Array.prototype.slice.call(document.getElementsByTagName('video')))
        .concat(Array.prototype.slice.call(document.getElementsByTagName('audio')));
var href = Array.prototype.slice.call(document.getElementsByTagName('link'))
            .concat(Array.prototype.slice.call(document.getElementsByTagName('a')));

var element;
function getAbsoluteFrom(url){
  if(url === undefined || url === null){
    return url;
  }
  if(url.startsWith('http:') || url.startsWith('https:') || url.startsWith('data')){
    return url;
  }
  if(url.startsWith('//')){
    return window.location.protocol+url;
  }
  var prefix = window.location.protocol + "//" + window.location.host + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")+1);
  return prefix + url;
}
/* processing elements with src attribute*/
for (var i = 0, max = src.length; i < max; i++){
    element = src[i];

    var tmpPath = element.src;
    if (element.hasAttribute('source') && !element.getAttribute("source").startsWith('data')) {
        tmpPath = element.getAttribute('source');
    }
    var path = getAbsoluteFrom(tmpPath);

    if (element.hasAttribute('source')) element.setAttribute('source', path);
    if (element.hasAttribute('src')) element.src = path;
    if (element.hasAttribute('data-source')) element.setAttribute('data-source', path);
}
/* processing elements with href attribute*/
for (var i = 0, max = href.length; i < max; i++)
{
    element = href[i];
    if(element.hasAttribute('href') && !element.href.startsWith('data')){
        var newHref = getAbsoluteFrom(element.href);
        element.href = newHref;
    }
}

var elements = document.getElementsByTagName('*');
/* processing elements with backgroundurl attribute*/
for (var i = 0, max = elements.length; i < max; i++) {
    var backgroundImageUrl = elements[i].style.backgroundImage.slice(5, - 2);
    if(backgroundImageUrl != undefined && backgroundImageUrl != null && backgroundImageUrl != ""){
        if(!backgroundImageUrl.startsWith('data')){
            var newUrl = getAbsoluteFrom(backgroundImageUrl);
            elements[i].style.backgroundImage = 'url("' + newUrl +'")';
        }
    }
}