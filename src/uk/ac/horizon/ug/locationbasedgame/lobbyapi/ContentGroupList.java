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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.horizon.ug.locationbasedgame.author.CRUDServlet;
import uk.ac.horizon.ug.locationbasedgame.author.RequestException;
import uk.ac.horizon.ug.locationbasedgame.model.EMF;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/** Lobby service API: returning something like:
 * 
 * <array size="2" elementjavatype="java.lang.Object">
 *	<item>
 *		<ContentGroup package="uk.ac.horizon.ug.exploding.db">
 *			<ID>CG500</ID> 
 *			<name>gameState.xml</name> 
 *			<version>1.0</version> 
 *			<location>Woolwich</location> 
 *			<startYear>1900</startYear> 
 *			<endYear>2020</endYear> 
 *		</ContentGroup>
 *	</item>
 * <array>
 *  
 * Of these, the structure is important, as are the "item", "ContentGroup" and "ID" element names.
 *
 * @author cmg
 *
 */
public class ContentGroupList extends LobbyApiServlet {

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.locationbasedgame.lobbyapi.LobbyApiServlet#handleGet(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager, org.w3c.dom.Document)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, EntityManager em,
			Document doc) throws RequestException {
		if (!"GET".equals(req.getMethod()))
			throw new RequestException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "ContentGroupList requires GET");
		// TODO Auto-generated method stub
		Query q = em.createQuery("SELECT x FROM "+GameConfiguration.class.getSimpleName()+" x");
		// TODO order?
		List<GameConfiguration> results = (List<GameConfiguration> )q.getResultList();

		Element arrayEl = doc.createElement("array");
		doc.appendChild(arrayEl);
	
		for (GameConfiguration gc : results) {
			Element itemEl = doc.createElement("item");
			arrayEl.appendChild(itemEl);
			Element cgEl = doc.createElement("ContentGroup");
			itemEl.appendChild(cgEl);
			addElement(doc, cgEl, "ID", gc.getId());
			addElement(doc, cgEl, "tag", gc.getTag());
			addElement(doc, cgEl, "version", new Integer(gc.getVersion()).toString());
		}
	}
}
