var iel = arguments[0].querySelectorAll('i u');
for (i = 0; i < iel.length; i++){
    if ((iel[i].parentElement.clientHeight == 16)
        && (iel[i].parentElement.clientWidth == 16)){
    iel[i].innerHTML = '';
    }
}