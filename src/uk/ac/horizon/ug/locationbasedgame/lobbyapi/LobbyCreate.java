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
package uk.ac.horizon.ug.locationbasedgame.lobbyapi;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.author.RequestException;
import uk.ac.horizon.ug.locationbasedgame.model.EMF;
import uk.ac.horizon.ug.locationbasedgame.model.GUIDFactory;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;
import uk.ac.horizon.ug.locationbasedgame.model.GameStatus;

/**
 * @author cmg
 *
 */
public class LobbyCreate extends LobbyApiServlet {

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.locationbasedgame.lobbyapi.LobbyApiServlet#handleGet(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager, org.w3c.dom.Document)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, EntityManager em,
			Document doc) throws RequestException {
		if (!"POST".equals(req.getMethod()))
			throw new RequestException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "LobbyCreate requires POST");
		
		/*
		  <p>with URL-encoded parameters:</p>
		  <ul>
		   	<li>contentGroupID = UTF-8-encoded ID from content_group_list</li>
		   	<li>name = lobby-provided game name string</li>
		   	<li>tag = lobby-generated (unique) game tag string</li>
		  </ul>
	    */
		String gcId = req.getParameter("contentGroupID");
		if (gcId==null || gcId.length()==0)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "contentGroupID not specified");
		String name = req.getParameter("name");
		if (name==null || name.length()==0)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "name not specified");
		String tag = req.getParameter("tag");
		if (tag==null || tag.length()==0)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "tag not specified");
		
		/* returns 
		 * <?xml version="1.0"?>
<Game package="uk.ac.horizon.ug.exploding.db">
   <ID>GA514</ID>
   <contentGroupID>CG500</contentGroupID>
   <name>name</name>
   <tag>tag</tag>
   <timeCreated>1283957181008</timeCreated>
   <gameTimeID>GT514</gameTimeID>
   <state>NOT_STARTED</state>
</Game>
		 * Actually only the ID should be needed...
		 */
		Element gameEl = doc.createElement("Game");
		doc.appendChild(gameEl);

		// content group id is keyName
		Key gcKey = GameConfiguration.idToKey(gcId);
		GameConfiguration gc = em.find(GameConfiguration.class, gcKey);
		if (gc==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"GameConfiguration not found: "+gcId);
		Game game = new Game();
		
		String gameId = GUIDFactory.newGUID();
		game.setId(gameId);
		game.setGameConfigurationKey(gcKey);
		game.setCreatedTime(System.currentTimeMillis());
		game.setStatus(GameStatus.WAITING);
		game.setTag(tag);
		game.setTitle(name);
		
		em.persist(game);
		
		logger.info("Created Game "+game);
		
		addElement(doc, gameEl, "ID", gameId);
		// rest should be optional
		//addElement(doc, gameEl, "contentGroupID", gcId);
		//addElement(doc, gameEl, "name", game.getTitle());
		//addElement(doc, gameEl, "tag", game.getTag());
		//addElement(doc, gameEl, "timeCreated", new Long(game.getCreatedTime()).toString());
		//addElement(doc, gameEl, "state", game.getStatus().toString());

	}

}
