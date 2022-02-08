function highlight(){
    let hash = document.location.hash;
    if( hash === undefined) return;

    let id = hash.split('#')[1];
    let element = document.getElementById(id);

    element.style.background = "#fccf1f"
}