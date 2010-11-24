// Javascript for locationbasedgame author/index.hmtl

// start here...
$.ajaxSetup({ cache: false, async: true, timeout: 30000 });

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
    return false;
}

// initialise table for start of loading
function prepare_table(table) {
    $('tr', table).remove();
    table.append('<tr><td>Loading...</td></tr>');
}

// update table with data array
function update_table_list(table, properties, data, detail_function_name) {
    $('tr', table).remove();
    var header = '<tr>';
    for (var i = 0; i < properties.length; i++) {
        header += '<td class="list_item">' + properties[i] + '</td>';
    }
    header += '</tr>';
    table.append(header);
    for (var di = 0; di < data.length; di++) {
        var item = data[di];
        var row = '<tr>';
        for (var i = 0; i < properties.length; i++) {
            if (properties[i] == 'id' && detail_function_name!=null && detail_function_name!=undefined)
                row += '<td class="list_item"><a href="#" onclick="'+detail_function_name+'(\''+item['id']+'\')">' + item[properties[i]] + '</a></td>';
            else            
                row += '<td class="list_item">' + item[properties[i]] + '</td>';
        }
        row += '</tr>';
        table.append(row);
    }
}

// update table with data array
function update_table_item(table, properties, data, editable) {
    $('tr', table).remove();
    for (var i = 0; i < properties.length; i++) {
        var item = data[properties[i]];
        var row = '<tr><td class="item_item">' + properties[i] + '</td><td class="item_item">';
        if (editable && !(properties[i]=='id'))
            row += '<input type="text" value="'+item+'" name="'+properties[i]+'" cols="40"/>';
        else
            row += item;
        row += '</td></tr>';
        table.append(row);
    }
}

// update table with error
function error_table(table, status) {
    $('tr', table).remove();
    table.append('<tr><td>Sorry - ' + status + '</td></tr>');
}

function refresh_table_list(table, properties, url, detail_function_name) {
    prepare_table(table);
    try {
        $.ajax({ url: url,
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                //  debug
                //alert('got ' + $.toJSON(data));
                update_table_list(table, properties, data, detail_function_name);
            },
            error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
    }
}

function refresh_table_item(table, properties, url, editable) {
    prepare_table(table);
    try {
        $.ajax({ url: url,
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                update_table_item(table, properties, data, editable);
            },
            error: function error(req, status) {
                error_table(table, status+' ('+req.status+': '+req.statusText+')');
            }
        });
    } catch (err) {
        error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
    }
}

var configuration_id = null;

function refresh_configuration(id) {
    // check/update id
    if (id == undefined)
        id = configuration_id;
    else
        configuration_id = id;
    var table = $('#configuration_table');
    if (id == null) {
        // empty
        var properties = ['tag', 'version'];
        update_table_item(table, properties, { tag: '', version: 1 }, true);
        return false;
    }
    //alert('refresh game ' + id);
    var properties = ['tag', 'version', 'createdTime', 'id'];
    refresh_table_item(table, properties, 'configuration/' + id, true);

    show_div('configuration');

    return false;
}

function add_update_configuration() {
    var table = $('#configuration_table');
    var item = {};
    item.tag = new String($('input[name=tag]',table).attr('value'));
    item.version = Math.floor(new Number($('input[name=version]',table).attr('value')));
    
    if (configuration_id == null) {
        //alert('add configuration...');
        var data = $.toJSON(item);
        $('tr', table).remove();
        table.append('<tr><td>Saving...</td></tr>');
        try {
            $.ajax({ url: 'configuration/',
                type: 'POST',
                contentType: 'application/json',
                processData: false,
                data: data,
                dataType: 'json',
                success: function success(data, status) {
                    refresh_configuration();
                    refresh_configuration_list();
                    if (configuration_id == null)
                        show_div('configuration_list');
                },
                error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
                }
            });
        } catch (err) {
            error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
        }
    }
    else {
        alert('update configuration... (not yet implemented)');
    }
}

function reset_configuration() {
    configuration_id = null;
    refresh_configuration(null);
}

function refresh_configuration_list() {
    var table = $('#configuration_list_table');
    var properties = ['tag', 'version', 'createdTime', 'id'];
    refresh_table_list(table, properties, 'configuration/', 'refresh_configuration');
    return false;
}

var done_get_game_list = false;

var game_id = null;

function refresh_game_client_list() {
    var table = $('#game_client_list_table');
    var properties = ['nickname', 'clientId', 'createdTime', 'id'];
    refresh_table_list(table, properties, 'game/'+game_id+'/client/', null);
    return false;
}

function refresh_client_location_list() {
    var table = $('#client_location_list_table');
    var properties = ['gameClientId', 'current', 'createdTime', 'latitudeE6', 'longitudeE6', 'altitudeMetres', 'radiusMetres'];
    refresh_table_list(table, properties, 'game/' + game_id + '/location/', null);
    return false;
}



function refresh_game(id) {
    // check/update id
    if (id == undefined)
        id = game_id;
    else
        game_id = id;
    if (id == null)
        return false;
    //alert('refresh game ' + id);
    var table = $('#game_table');
    var properties = ['title', 'tag', 'status', 'createdTime', 'gameConfigurationId', 'id'];
    refresh_table_item(table, properties, 'game/' + id);

    refresh_game_client_list();
    refresh_client_location_list();
    
    show_div('game');
    
    return false;
}


function refresh_game_list() {
    var table = $('#game_list_table');
    var properties = ['title','tag', 'status', 'createdTime', 'gameConfigurationId', 'id'];
    refresh_table_list(table, properties, 'game/', 'refresh_game');
    return false;
}

function show_game_list() {
    if (!done_get_game_list) {
        done_get_game_list = true;
        refresh_game_list();
    }
    show_div('game_list');
    return false;
}



// loaded...
$(document).ready(function() {
    show_div('configuration_list');
    refresh_configuration_list();
});
