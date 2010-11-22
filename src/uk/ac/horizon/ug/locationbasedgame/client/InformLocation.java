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
package uk.ac.horizon.ug.locationbasedgame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.author.CRUDServlet;
import uk.ac.horizon.ug.locationbasedgame.author.JsonConstants;
import uk.ac.horizon.ug.locationbasedgame.author.RequestException;
import uk.ac.horizon.ug.locationbasedgame.lobbyapi.LobbyApiServlet;
import uk.ac.horizon.ug.locationbasedgame.model.ClientLocation;
import uk.ac.horizon.ug.locationbasedgame.model.EMF;
import uk.ac.horizon.ug.locationbasedgame.model.Game;
import uk.ac.horizon.ug.locationbasedgame.model.GameClient;
import uk.ac.horizon.ug.locationbasedgame.model.GameStatus;

/** Client informs server of its location.
 * 
 * @author cmg
 *
 */
public class InformLocation extends HttpServlet implements JsonConstants {
	/** logger */
	static Logger logger = Logger.getLogger(InformLocation.class.getName());

	public static JSONObject parseObject(HttpServletRequest req) throws RequestException {
		try {
			//tx.begin();
			BufferedReader r = req.getReader();
			String line = r.readLine();
			//logger.info("UpdateAccount(1): "+line);
			// why does this seem to read {} ??
			//JSONObject json = new JSONObject(req.getReader());
			JSONObject json = new JSONObject(line);
			r.close();
			return json;
		}
		catch (Exception e) {
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Error parsing request (JSON): "+e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// expects: { "conversationId":"...", "clientId":"...", "latitudeE6":123, "longitudeE6":123, 
			// "radiusMetres":1.0, "altitudeMetres":1.0}
			JSONObject jreq = parseObject(req);
			
			String conversationId = jreq.getString(CONVERSATION_ID); 
			Key gameKey = null;
			EntityManager em = EMF.get().createEntityManager();
			try {
				Query q = em.createQuery("SELECT x FROM "+GameClient.class.getSimpleName()+" x WHERE x.currentConversationId = :currentConversationId");
				q.setParameter("currentConversationId", conversationId);
				q.setMaxResults(2);
				List results = q.getResultList();
				if (results.size()>1) 
					logger.log(Level.WARNING, "More than one GameClient with conversationId "+conversationId);
				if (results.size()==0)
					throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "conversationId unknonwn: "+conversationId);
				
				GameClient gc = (GameClient)results.get(0);
				// sanity check
				String clientId = jreq.getString(CLIENT_ID);
				if (!gc.getClientId().equals(clientId))
					throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "conversationId -> GameClient "+gc+" with different clientId cf. "+clientId);
				
				ClientLocation cloc = new ClientLocation();
				cloc.setCreatedTime(System.currentTimeMillis());
				cloc.setKey(ClientLocation.makeKey(gc, cloc.getCreatedTime()));
				cloc.setAltitudeMetres((float)jreq.getDouble(ALTITUDE_METRES));
				cloc.setCurrent(true);
				cloc.setGameClientKey(gc.getKey());
				gameKey = gc.getGameKey();
				cloc.setGameKey(gameKey);
				cloc.setLatitudeE6(jreq.getInt(LATITUDE_E6));
				cloc.setLongitudeE6(jreq.getInt(LONGITUDE_E6));
				cloc.setRadiusMetres((float)jreq.getDouble(RADIUS_METRES));
				
				em.persist(cloc);
				logger.log(Level.FINE, "added ClientLocation: "+cloc);
				
				// non-current any old one...
				q = em.createQuery("SELECT x FROM "+ClientLocation.class.getSimpleName()+" x WHERE x.gameClientKey= :gameClientKey AND x.current= TRUE AND x.createdTime < :createdTime");
				q.setParameter("gameClientKey", gc.getKey());
				q.setParameter("createdTime", cloc.getCreatedTime());
				List<ClientLocation> clocs = (List<ClientLocation>)q.getResultList();
				for (ClientLocation cl : clocs) {
					cl.setCurrent(false);
					logger.log(Level.FINER,"Marked not current ClientLocation "+cl);
				}
			}
			finally {
				em.close();
			}
			
			returnLocations(resp, gameKey);
		}
		catch (RequestException re)
		{
			resp.sendError(re.getErrorCode(), re.getMessage());
		} 
		catch (JSONException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
		}
	}

	
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String gameId = req.getParameter("gameId");
			if (gameId==null || gameId.length()==0)
				throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "gameId not specified");
			Key gameKey = Game.idToKey(gameId);
			returnLocations(resp, gameKey);
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
		}
	}
	
	private void returnLocations(HttpServletResponse resp, Key gameKey) throws RequestException {
		// TODO Auto-generated method stub
		EntityManager em = EMF.get().createEntityManager();
		try {

			Query q = em.createQuery("SELECT x FROM "+ClientLocation.class.getSimpleName()+" x WHERE x.gameKey = :gameKey AND x.current= TRUE ORDER BY x.createdTime DESC");
			q.setParameter("gameKey", gameKey);
			// TODO order?
			List<ClientLocation> results = (List<ClientLocation>)q.getResultList();

			resp.setCharacterEncoding(CRUDServlet.ENCODING);
			resp.setContentType(CRUDServlet.JSON_MIME_TYPE);		
			Writer w = resp.getWriter();
			JSONWriter jw = new JSONWriter(w);
			jw.array();
			for (ClientLocation cl : results) {
				GameClient gc = em.find(GameClient.class, cl.getGameClientKey());
				
				jw.object();
				if (gc!=null) {
					jw.key(CLIENT_ID);
					jw.value(gc.getClientId());
					jw.key(NICKNAME);
					jw.value(gc.getNickname());
				}
				jw.key(LATITUDE_E6);
				jw.value(cl.getLatitudeE6());
				jw.key(LONGITUDE_E6);
				jw.value(cl.getLongitudeE6());
				jw.key(ALTITUDE_METRES);
				jw.value(cl.getAltitudeMetres());
				jw.key(RADIUS_METRES);
				jw.value(cl.getRadiusMetres());
				jw.key(CREATED_TIME);
				jw.value(cl.getCreatedTime());
				jw.endObject();
			}
			jw.endArray();
			w.close();
		} catch (Exception e) {
			throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sending locations: "+e); 
		}
		finally {
			em.close();
		}
	}

}
