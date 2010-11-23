// Javascript for locationbasedgame client.hmtl

// start here...
$.ajaxSetup({ cache: false, async: true, timeout: 30000 });


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

function log(msg) {
    //if (get_lobbyclient() != undefined)
    //        get_lobbyclient().log(msg);
    // debug
    //	else
    alert(msg);
}


function show_div(id) {
    $('body > div').hide('fast');
    $('#' + id).show('fast');
}

// last known client location
var coords;
// startup config
var playUrl;
var clientId;
var conversationId;
var nickname;
var gameId;
var gameStatus;

function update_server() {
    if (playUrl==null || playUrl==undefined || playUrl.length==0) {
        alert('Cannot update_server: playUrl undefined');
        return;
    }
    if (coords == null || coords == undefined) {
        alert('Cannot update_server: coords undefined');
        return;
    }
    // server
    var url = playUrl;
    if (url.charAt(url.length - 1) != '/')
        url += '/';
    url += 'location';
    // expects: { "conversationId":"...", "clientId":"...", "latitudeE6":123, "longitudeE6":123, 
    // "radiusMetres":1.0, "altitudeMetres":1.0}
    var msg = { conversationId : conversationId, clientId : clientId };
    msg.latitudeE6 = Math.round(coords.latitude * 1000000);
    msg.longitudeE6 = Math.round(coords.longitude * 1000000);
    if (coords.accuracy!=undefined)
        msg.radiusMetres = coords.accuracy; 
    else
        msg.radiusMetres = 0.0;
    if (coords.altitude != undefined)
        msg.altitudeMetres = coords.altitude;
    else
        msg.altitudeMetres = 0.0;
    var req = $.toJSON(msg);

    try {
        var tr = $('#responseStatus');
        tr.html('POST to ' + url + ' : ' + req);
        log('POST to ' + url + ' ' + req);
        $.ajax({ url: url,
            type: 'POST',
            contentType: 'application/json',
            processData: false,
            data: req,
            dataType: 'json',
            success: function success(data, status) {
                log('Query resp. ' + $.toJSON(data));
                tr.html('Query resp. ' + $.toJSON(data));
                
                update_map_other(data);
            },
            error: function error(req, status) {
                log('Error updating location (' + status + ')');
                tr.html('Error updating location (' + status + ')');
            }
        });
    } catch (err) {
        log('Exception updating location (' + err.name + ':' + err.message + ')');
        tr.html('Exception updating location (' + err.name + ':' + err.message + ')');
    }
    return false;
}

var watching_location = false;

function get_location() {
    var tr = $('#locationStatus');
    tr.html('checking location API...');
    try {
        // Try W3C Geolocation
        if (navigator.geolocation) {
            if (!watching_location) {
                tr.html('checking location (W3C geolocation)...');
                watching_location = true;

                navigator.geolocation.watchPosition(function(position) {
                    // success  
                    tr.html('Latitude: ' + position.coords.latitude + '<br/>Longitude: ' + position.coords.longitude);
                    coords = position.coords;

                    update_map_player();

                    //update_server();

                }, function(position_error) {
                    // failure  
                    tr.html('Sorry - there was a problem (' + position_error + ')');
                }, {
                    // options  
                    enableHighAccuracy: true
                });
            }
        } else {
            tr.html('Sorry - no location API');
        }
    } catch (e) {
        tr.html('Sorry - '+e.name+': '+e.message);
    }
    return false;
}


// open layers map and map imagery layer
var map, layer;
// map marker layer
var markers;
// map marker icon
var player_icon;
var other_icon;
// player marker
var player_marker = null;
// other player's markers
var other_markers_map = {};
var other_names_map = {};

// default/standard click handler as class
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
                        {}, this.defaultHandlerOptions
                    );
        OpenLayers.Control.prototype.initialize.apply(
                        this, arguments
                    );
        this.handler = new OpenLayers.Handler.Click(
                        this, {
                            'click': this.trigger
                        }, this.handlerOptions
                    );
    },

    trigger: function(e) {
        var lonlat = map.getLonLatFromViewPortPx(e.xy);
        //alert("You clicked near " + lonlat.lat + " N, " +
        //                              +lonlat.lon + " E");
        //alert('lonlat = ' + lonlat);
        lonlat = lonlat.transform(
                    map.getProjectionObject(),
                    new OpenLayers.Projection("EPSG:4326")
                    );
        alert('trigger: ' + e);
        //        alert('lonlat = ' + lonlat);
        //        $('#latlon_out').attr('value', new Number(lonlat.lon).toFixed(6) + ',' + new Number(lonlat.lat).toFixed(6));
    }
});

function onFeatureSelect(feature) {
    alert('select '+feature);
}
function onFeatureUnselect(feature) {
    alert('unselect '+feature);
}

function map_init() {
    map = new OpenLayers.Map('map');
    layer = new OpenLayers.Layer.OSM("Simple OSM Map");
    map.addLayer(layer);

    markers = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(markers);

    var size = new OpenLayers.Size(21, 25);
    var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
    player_icon = new OpenLayers.Icon('../img/marker.png', size, offset);
    // bigger for debug
    size = new OpenLayers.Size(42, 50);
    other_icon = new OpenLayers.Icon('../img/marker-blue.png', size, offset);

    // doesn't work with Marker layer?
    //var selectControl = new OpenLayers.Control.SelectFeature(markers,
    //            { onSelect: onFeatureSelect, onUnselect: onFeatureUnselect });
    //map.addControl(selectControl);
    //selectControl.activate();
                    
    map.setCenter(
                new OpenLayers.LonLat(-1.188, 52.953).transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    map.getProjectionObject()
                ), 12
            );

    // add click handler
    //var click = new OpenLayers.Control.Click();
    //map.addControl(click);
    //click.activate();
}

function update_map_player() {
    if (player_marker == null) {
        player_marker = new OpenLayers.Marker(new OpenLayers.LonLat(0, 0), player_icon);
        markers.addMarker(player_marker);
    }
    player_marker.lonlat = new OpenLayers.LonLat(coords.longitude, coords.latitude).transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    map.getProjectionObject()
                );
    markers.redraw();
}

// update map markers for other players using parsed JSON array returned from server
function update_map_other(others) {
    try {
        var new_other_markers_map = {};
        var old_other_markers_map = other_markers_map;
        for (var i = 0; i < others.length; i++) {
            var other = others[i];
            alert('update other ' + i + ': ' + $.toJSON(other));
            if (other.clientId != undefined) {
                other_names_map[other.cliendId] = other.nickname;
                var marker = old_other_markers_map[other.clientId];
                if (marker == undefined) {
                    marker = new OpenLayers.Marker(new OpenLayers.LonLat(0, 0), other_icon.clone());
                    markers.addMarker(marker);
                    new_other_markers_map[other.clientId] = marker;
                }
                else {
                    old_other_markers_map[other.clientId] = undefined;
                }
                marker.lonlat = new OpenLayers.LonLat(other.longitudeE6 * 0.000001, other.latitudeE6 * 0.000001).transform(
                        new OpenLayers.Projection("EPSG:4326"),
                        map.getProjectionObject()
                    );
            }
        }
        for (var old_other in old_other_markers_map) {
            if (old_other != undefined)
                alert('get rid of ' + $.toJSON(old_other));
        }
        other_markers_map = new_other_markers_map;
    }
    catch (e) {
        alert('Problem updating map: ' + e.name + ': ' + e.message);
    }    
    markers.redraw();
}

// loaded...
$(document).ready(function() {
    show_div('debug');

    playUrl = decodeURIComponent(gup('playUrl'));
    $('#playUrl').html(playUrl);
    //alert('playUrl: '+playUrl);
    clientId = decodeURIComponent(gup('clientId'));
    $('#clientId').html(clientId);
    //alert('clientId: '+clientId);
    conversationId = decodeURIComponent(gup('conversationId'));
    $('#conversationId').html(conversationId);
    nickname = decodeURIComponent(gup('nickname'));
    $('#nickname').html(nickname);
    gameId = decodeURIComponent(gup('gameId'));
    $('#gameId').html(gameId);
    gameStatus = decodeURIComponent(gup('gameStatus'));
    $('#gameStatus').html(gameStatus);

    map_init();

    get_location();
});

