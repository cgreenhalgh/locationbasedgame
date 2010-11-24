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
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.apache.tools.ant.types.Assertions.DisabledAssertion;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.model.GUIDFactory;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameClient;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/**
 * @author cmg
 *
 */
public class GameClientCRUDServlet extends CRUDServlet implements JsonConstants {

	/**
	 * 
	 */
	public GameClientCRUDServlet() {
		super();
	}

	/**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 * @param discardPathParts
	 */
	public GameClientCRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts) {
		super(listFilterPropertyName, listFilterPropertyValue, discardPathParts);
	}

	@Override
	protected Class getObjectClass() {
		return GameClient.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		GameClient g = (GameClient)o;
		jw.object();
		// ID first
		jw.key(ID);
		jw.value(g.getKey().getName());
		jw.key(NICKNAME);
		jw.value(g.getNickname());
		jw.key(CLIENT_ID);
		jw.value(g.getClientId());
		// due to type changes, createdTime could be null
		try {
			long createdTime = g.getCreatedTime();
			jw.key(CREATED_TIME);
			jw.value(g.getCreatedTime());
		}
		catch (Exception e) {
			logger.log(Level.FINE, "could not get createdTime for GameClient "+g.getKey().getName(), e);
		}
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
		throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"cannot create a GameClient like this");
	}

}
