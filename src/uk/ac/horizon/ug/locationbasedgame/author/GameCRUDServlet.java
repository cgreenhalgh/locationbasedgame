/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of locationbasedgame.
 *
 *  locationbasedgame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  locationbasedgame is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with locationbasedgame.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.locationbasedgame.author;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.model.GUIDFactory;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/**
 * @author cmg
 *
 */
public class GameCRUDServlet extends CRUDServlet implements JsonConstants {

	@Override
	protected Class getObjectClass() {
		return Game.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		Game g = (Game)o;
		jw.object();
		// ID first
		jw.key(ID);
		jw.value(g.getId());
		jw.key(CREATED_TIME);
		jw.value(g.getCreatedTime());
		jw.key(GAME_CONFIGURATION_ID);
		jw.value(g.getGameConfigurationKey().getName());
		jw.key(STATUS);
		jw.value(g.getStatus().toString());
		jw.key(TAG);
		jw.value(g.getTag());
		jw.key(TITLE);
		jw.value(g.getTitle());
		jw.endObject();
	}

	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		Game gc = new Game();
		Iterator keys = json.keys();
		while(keys.hasNext()) {
			String key = (String)keys.next();
			throw new JSONException("Unsupported key '"+key+"' in Game: "+json);

		}
		return gc;
	}

	@Override
	protected Key validateCreate(Object o) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"cannot create a Game like this");
	}
}
