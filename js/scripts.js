function getDateTime()
{
    var date = new Date();
    
    return date;
}

$(document).ready(function () {
    $("#retrieveBtn").on("click", function () {
        document.getElementById("date").innerHTML = "Last save was on " + getDateTime();
    });

    $("#sendBtn").on("click", function () {
        document.getElementById("date").innerHTML = "Saved at " + getDateTime();
    });
});
