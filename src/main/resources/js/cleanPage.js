function removeElementsByClass(className){
    var elements = document.getElementsByClassName(className);
    console.log(elements);
    while(elements.length > 0){
        elements[0].parentNode.removeChild(elements[0]);
    }
}

removeElementsByClass('cbc');
removeElementsByClass('abgc');
removeElementsByClass('abgb');
removeElementsByClass('cbb');
removeElementsByClass('abgcp');

removeElementsByClass('bap-trigger');
removeElementsByClass('bap-img-container');

removeElementsByClass('yt-uix-button-subscription-container');