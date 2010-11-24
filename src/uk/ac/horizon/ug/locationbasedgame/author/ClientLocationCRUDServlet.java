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

import uk.ac.horizon.ug.locationbasedgame.model.ClientLocation;
import uk.ac.horizon.ug.locationbasedgame.model.GUIDFactory;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameClient;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/**
 * @author cmg
 *
 */
public class ClientLocationCRUDServlet extends CRUDServlet implements JsonConstants {

	/**
	 * 
	 */
	public ClientLocationCRUDServlet() {
		super();
	}

	/**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 * @param discardPathParts
	 */
	public ClientLocationCRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts) {
		super(listFilterPropertyName, listFilterPropertyValue, discardPathParts);
	}

	@Override
	protected Class getObjectClass() {
		return ClientLocation.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		ClientLocation g = (ClientLocation)o;
		jw.object();
		// ID first
		jw.key(GAME_CLIENT_ID);
		jw.value(g.getGameClientKey().getName());
		jw.key(CREATED_TIME);
		jw.value(g.getCreatedTime());
		jw.key(CURRENT);
		jw.value(g.isCurrent());
		jw.key(ALTITUDE_METRES);
		jw.value(g.getAltitudeMetres());
		jw.key(LATITUDE_E6);
		jw.value(g.getLatitudeE6());
		jw.key(LONGITUDE_E6);
		jw.value(g.getLongitudeE6());
		jw.key(RADIUS_METRES);
		jw.value(g.getRadiusMetres());
		jw.endObject();
	}

	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		Game gc = new Game();
		Iterator keys = json.keys();
		while(keys.hasNext()) {
			String key = (String)keys.next();
			throw new JSONException("Unsupported key '"+key+"' in ClientLocation: "+json);

		}
		return gc;
	}

	@Override
	protected Key validateCreate(Object o) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"cannot create a ClientLocation like this");
	}

}
