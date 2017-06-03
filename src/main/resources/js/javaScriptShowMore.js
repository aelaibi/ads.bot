var containers = document.getElementsByClassName('fb-container-ad-from-collector');for (i = 0; i < containers.length; i++) {
        var divs = containers[i].getElementsByTagName('div');
        for (j = 0; j < divs.length; j++) {
            if (divs[j].style.display == 'none') divs[j].style.display = '';
        }
    }