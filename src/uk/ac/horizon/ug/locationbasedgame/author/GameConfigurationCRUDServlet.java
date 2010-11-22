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
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/**
 * @author cmg
 *
 */
public class GameConfigurationCRUDServlet extends CRUDServlet implements JsonConstants {

	@Override
	protected Class getObjectClass() {
		return GameConfiguration.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		GameConfiguration gc = (GameConfiguration)o;
		jw.object();
		// ID first
		jw.key(ID);
		jw.value(gc.getId());
		jw.key(CREATED_TIME);
		jw.value(gc.getCreatedTime());
		jw.key(TAG);
		jw.value(gc.getTag());
		jw.key(VERSION);
		jw.value(gc.getVersion());
		jw.endObject();
	}

	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		GameConfiguration gc = new GameConfiguration();
		Iterator keys = json.keys();
		while(keys.hasNext()) {
			String key = (String)keys.next();
//			logger.info("GCT: "+key+"="+json.getObject(key));
			if (key.equals(TAG))
				gc.setTag(json.getString(key));
			else if (key.equals(VERSION))
				gc.setVersion(json.getInt(key));
			else 
				throw new JSONException("Unsupported key '"+key+"' in GameConfiguration: "+json);

		}
		return gc;
	}

	@Override
	protected Key validateCreate(Object o) throws RequestException {
		GameConfiguration gc = (GameConfiguration)o;
		if (gc.getTag()==null || gc.getTag().length()==0)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"tag must be specified");
		// TODO: already exists?
		Key key = GameConfiguration.makeKey(gc.getTag(), gc.getVersion());	
		// mutate in place
		gc.setCreatedTime(System.currentTimeMillis());
		gc.setKey(key);
		return key;
	}
}
