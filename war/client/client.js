// Javascript for locationbasedgame client.hmtl

//http://www.netlobo.com/url_query_string_javascript.html
function gup( name ) {  
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");  
	var regexS = "[\\?&]"+name+"=([^&#]*)";  
	var regex = new RegExp( regexS ); 
	var results = regex.exec( window.location.href ); 
	if( results == null )   
		return "";  
	else    
		return results[1];
}


// loaded...
$(document).ready(function() {
	var playUrl = gup('playUrl');
	$('#playUrl').html(playUrl);
	//alert('playUrl: '+playUrl);
	var clientId = gup('clientId');
	$('#clientId').html(clientId);
	//alert('clientId: '+clientId);
	var conversationId = gup('conversationId');
	$('#conversationId').html(conversationId);
	var nickname = gup('nickname');
	$('#nickname').html(nickname);
	var gameId = gup('gameId');
	$('#gameId').html(gameId);
	var gameStatus = gup('gameStatus');
	$('#gameStatus').html(gameStatus);
});

