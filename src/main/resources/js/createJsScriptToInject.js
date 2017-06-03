<script pixi="true">
    document.onreadystatechange = function(e)
{
    if (document.readyState === 'complete')
    {
        var manifest = lib.properties.manifest;
        for (var i=0, l=manifest.length; i<l; i++) {
            var id = manifest[i].id;
            var img = document.getElementById(id);
            if (img != null) {
                images[id] = img;
            }
        }
        handleComplete();

    }
};
</script>
