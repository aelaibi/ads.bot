function findElementsByClass(className) {
 var elements = document.getElementsByClassName(className);
 return elements.length > 0;
}

var isAdsense = ( findElementsByClass('cbc')
|| findElementsByClass('abgc')
|| findElementsByClass('abgb')
|| findElementsByClass('cbb')
|| findElementsByClass('abgcp') ||
findElementsByClass('bap-trigger') ||
findElementsByClass('bap-img-container'));

return isAdsense;