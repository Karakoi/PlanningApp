function checkValue(form, message) {
    
    var userInput = form[form.id + ":login"];
    var userInput2 = form[form.id + ":password"];
    
    if (userInput.value=='' && userInput2.value==''){
        alert(message);
        userInput.focus();
        return false;
    } 
    
    return true;
}

function showProgress(data) {
    
    if (data.status === "begin") {
        document.getElementById('loading_wrapper').style.display = "block";
    } else if (data.status === "success") {
        document.getElementById('loading_wrapper').style.display = "none";
    }
}

function showSuccess() {
    
//     if (data.status == "begin") {
//        document.getElementById('success1').style.display = "block";
//    } else if (data.status == "success") {
//        document.getElementById('success1').style.display = "none";
//    } 
        document.getElementById('success1').style.display = "block";   
}

