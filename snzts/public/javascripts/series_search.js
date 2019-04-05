// var base = "http://localhost:9000/v1/";
var base = "v1/";

var getResults = function(){
  $("#results-tab").find("tbody > tr").remove();
  var skw = $("#subjectKeywords").val().split(",");
  var fkw = $("#familyKeywords").val().split(",");
  var res = $("#search-results");

  var query = base + "families?format=json";
  if (skw.length > 0) skw.forEach(function(x){if (x != "") query = query + "&subjectKeyword=" + x;});
  if (fkw.length > 0) fkw.forEach(function(x){if (x != "") query = query + "&familyKeyword="  + x;});

  $.ajax({
    contentType: 'application/json',
    type: "GET",
    url: query,
    success: function (data, textStatus, jqXHR) {
      data.forEach(function (series) {
        var q = "subjectCode=" + series.subject_code + "&familyCode=" + 
          series.family_code + "&familyNbr=" + series.family_nbr + "&limit=100";
        var link1 = '<a href="' + base + 'dataset?format=json&' + q +
          '" target="_blank">JSON</a>';
        var link2 = '<a href="' + base + 'dataset?format=csv&' + q +
          '" target="_blank">CSV</a>';
        $('#results-tab > tbody:last-child').append('<tr><td class=center>' + series.subject_code + 
          '</td><td class=center>' + series.family_code + 
          '</td><td class=center>' + series.family_nbr +
          '</td><td>' + series.title_text + '</td><td class=center>' + link1 + " | " + 
          link2 + '</td></tr>');
      });
    },
    error: function (jqXHR, textStatus, errorThrown) {
      console.log("Bugger.");
    }
  });
}

$("#subjectKeywords").keypress(function(e){
  var keycode = (event.keyCode ? event.keyCode : event.which);
	if(keycode == '13'){
		getResults();
	}
});

$("#familyKeywords").keypress(function(e){
  var keycode = (event.keyCode ? event.keyCode : event.which);
	if(keycode == '13'){
		getResults();
	}
});

$(document).ready(getResults);
$("#search").click(getResults);