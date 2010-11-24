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

import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.author.RequestException;
import uk.ac.horizon.ug.locationbasedgame.model.EMF;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameClient;
import uk.ac.horizon.ug.locationbasedgame.model.GameStatus;

/** rpc/login class.
 * 
 * @author cmg
 *
 */
public class RpcLogin extends LobbyApiServlet {

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.locationbasedgame.lobbyapi.LobbyApiServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager, org.w3c.dom.Document)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, EntityManager em,
			Document doc) throws RequestException {
		/* expects POST
		 * With post body request like:

<login>
	<clientId>...</clientId>
	<conversationId>...</conversationId>
	<playerName>...</playerName>
	<clientVersion>1</clientVersion>
	<clientType>AndroidDevclient</clientType>
    <gameTag>...</gameTag>
</login>
  
Where:

clientId is the GameClient's clientId (i.e. persistent client identifier) 
conversationId is a newly-generated unique session ID (string) 
playerName is the GameSlot nickname or default player name or "Anonymous" 
The response must be an XML document of the form

<response>
	<status>OK</status> [or FAILED or GAME_NOT_FOUND => try later]
    <message>...</message>         
    <gameId>...</gameId>
    <gameStatus>...</gameStatus>
</response>
  
The clientVersion and clientType are currently hard-coded in the ExplodingPlacesServerProtocol class (not obtained from the client or configuration).


		 */
		if (!"POST".equals(req.getMethod()))
			throw new RequestException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "LobbyCreate requires POST");

		Document reqdoc = parseRequest(req);
		String clientId = getElement(reqdoc, "clientId");
		if (clientId==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"<clientId> not specified");
		String conversationId = getElement(reqdoc, "conversationId");
		if (conversationId==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"<conversationId> not specified");
		String nickname = getElement(reqdoc, "playerName");
		if (nickname==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"<playerName> not specified");
		String gameTag = getElement(reqdoc, "gameTag");
		if (gameTag==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"<gameTag> not specified");
		
		Element respEl = doc.createElement("response");
		doc.appendChild(respEl);

		// right tag, not ACTIVE or STOPPING
		Query q = em.createQuery("SELECT x FROM "+Game.class.getSimpleName()+" x WHERE x.tag = :tag AND x.status IN ( '"+GameStatus.ACTIVE+"', '"+GameStatus.STOPPING+"') ORDER BY x.createdTime DESC");
		q.setParameter("tag", gameTag);
		q.setMaxResults(2);
		
		List results = q.getResultList();
		if (results.size()==0)
		{
			addElement(doc, respEl, "status", "GAME_NOT_FOUND");
			addElement(doc, respEl, "message", "No (live) game found for tag "+gameTag);
			return;
		}
		if (results.size()>0)
			logger.log(Level.WARNING, "More than one live game found for tag "+gameTag);
		Game game = (Game)results.get(0);
		
		// atomic make/update player
		Key gameClientKey = GameClient.makeKey(game, clientId);
		EntityTransaction et = em.getTransaction();
		et.begin();
		try {
			GameClient gc = em.find(GameClient.class, gameClientKey);
			if (gc==null) {
				logger.info("Creating GameClient "+gameClientKey.getName());
				gc = new GameClient();
				gc.setClientId(clientId);
				gc.setCreatedTime(System.currentTimeMillis());
				gc.setCurrentConversationId(conversationId);
				gc.setGameKey(game.getKey());
				gc.setNickname(nickname);
				gc.setKey(gameClientKey);
				em.persist(gc);
			}
			else {
				logger.info("Updating conversationId to "+conversationId+" for GameClient "+gameClientKey.getName());
				gc.setCurrentConversationId(conversationId);
			}
			et.commit();
		}
		finally {
			if (et.isActive())
				et.rollback();
		}

		addElement(doc, respEl, "status", "OK");
		addElement(doc, respEl, "gameId", game.getId());
		addElement(doc, respEl, "gameStatus", game.getStatus().toString());
	}

}
