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
import org.w3c.dom.NodeList;

import uk.ac.horizon.ug.locationbasedgame.author.CRUDServlet;
import uk.ac.horizon.ug.locationbasedgame.author.RequestException;
import uk.ac.horizon.ug.locationbasedgame.model.EMF;
import uk.ac.horizon.ug.locationbasedgame.model.GameConfiguration;

/** Lobby service API, supporting stuff
 *
 * @author cmg
 *
 */
public abstract class  LobbyApiServlet extends HttpServlet {
	/** logger */
	static Logger logger = Logger.getLogger(LobbyApiServlet.class.getName());

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doOp(req, resp);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doOp(req, resp);
	}
	protected void doOp(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		EntityManager em = EMF.get().createEntityManager();
		try {
			// XML response
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			Document respdoc = db.newDocument();
			
			handleRequest(req, em, respdoc);
					
			// write it
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/xml");		
			Writer w = resp.getWriter();
			// TODO (standard) Xstream doesn't work on GAE :-(
			Transformer dt = TransformerFactory.newInstance().newTransformer();
			dt.transform(new DOMSource(respdoc), new StreamResult(w));
			w.close();

		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Getting returning GameConfigurations", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
		finally {
			em.close();
		}
	}
	protected abstract void handleRequest(HttpServletRequest req, EntityManager em, Document doc) throws RequestException;
	
	public static void addElement(Document doc, Element pel, String tag, String value) {
		Element el = doc.createElement(tag);
		pel.appendChild(el);
		el.appendChild(doc.createTextNode(value));		
	}
	public static String getElement(Document doc, String tag) {
		NodeList els = doc.getDocumentElement().getElementsByTagName(tag);
		if (els.getLength()==0)
			return null;
		return ((Element)els.item(0)).getTextContent();
	}

	public static Document parseRequest(HttpServletRequest req) throws RequestException {
		try {
			// XML response
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			return db.parse(req.getInputStream());
		}
		catch (Exception e) {
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Parsing request: "+e.toString());
		}

	}
}
