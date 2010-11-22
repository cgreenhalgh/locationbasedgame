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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.locationbasedgame.model.EMF;

import java.util.List;
import java.util.logging.*;

/**
 * @author cmg
 *
 */
public abstract class CRUDServlet extends HttpServlet {
	/** logger */
	static Logger logger = Logger.getLogger(CRUDServlet.class.getName());
	
	public static final String ENCODING = "UTF-8";
	public static final String JSON_MIME_TYPE = "application/json";

	/** the persisent class to be managed */
	protected abstract Class getObjectClass();

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "doGet("+req.getPathInfo()+")");
		String pathInfo = req.getPathInfo();
		if (pathInfo==null || "/".equals(pathInfo)) {
			// get all
			doList(req, resp);
			return;
		}
		// TODO: item read
		super.doGet(req, resp);
	}
	
	private void doList(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, IOException {
		List results = null;
		EntityManager em = EMF.get().createEntityManager();
		try {
			Class clazz = getObjectClass();
			Query q = em.createQuery("SELECT x FROM "+clazz.getSimpleName()+" x");
			// TODO order?
			results = q.getResultList();
			// warning: lazy
			resp.setCharacterEncoding(ENCODING);
			resp.setContentType(JSON_MIME_TYPE);		
			Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
			JSONWriter jw = new JSONWriter(w);
			jw.array();
			for (Object o : results) {
				listObject(jw, o);
			}
			jw.endArray();
			w.close();
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Getting object of type "+getObjectClass(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
		finally {
			em.close();
		}
	}

	/** marshall 
	 * @throws JSONException */
	protected abstract void listObject(JSONWriter jw, Object o) throws JSONException;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "doPut("+req.getContextPath()+")");
		String pathInfo = req.getPathInfo();
		if (pathInfo==null || "/".equals(pathInfo)) {
			// get all
			doCreate(req, resp);
			return;
		}
		super.doPost(req, resp);
	}

	/** Create on POST.
	 * E.g. curl -d '{...}' http://localhost:8888/author/configuration/
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doCreate(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			Object o = parseObject(req);
			Key key = validateCreate(o);
			// try adding
			EntityManager em = EMF.get().createEntityManager();
			EntityTransaction et = em.getTransaction();
			try {
				et.begin();
				if (em.find(getObjectClass(), key)!=null)
					throw new RequestException(HttpServletResponse.SC_CONFLICT, "object already exists ("+key+")");
				em.persist(o);
				et.commit();
				logger.info("Added "+o);
			}
			finally {
				if (et.isActive())
					et.rollback();
				em.close();
			}
			resp.setCharacterEncoding(ENCODING);
			resp.setContentType(JSON_MIME_TYPE);		
			Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
			JSONWriter jw = new JSONWriter(w);
			listObject(jw, o);
			w.close();			
		}
		catch (RequestException e) {
			resp.sendError(e.getErrorCode(), e.getMessage());
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Getting object of type "+getObjectClass(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	}

	protected abstract Key validateCreate(Object o) throws RequestException;

	private Object parseObject(HttpServletRequest req) throws RequestException {
		try {
			//tx.begin();
			BufferedReader r = req.getReader();
			String line = r.readLine();
			//logger.info("UpdateAccount(1): "+line);
			// why does this seem to read {} ??
			//JSONObject json = new JSONObject(req.getReader());
			JSONObject json = new JSONObject(line);
			r.close();
			return parseObject(json);
		}
		catch (RequestException re)
		{
			throw re;
		}
		catch (Exception e) {
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Error parsing request (JSON): "+e);
		}
	}

	protected abstract Object parseObject(JSONObject json) throws RequestException, IOException, JSONException;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "doPost("+req.getContextPath()+")");
		// TODO: Update
		super.doPut(req, resp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "doDelete("+req.getContextPath()+")");
		// TODO: delete
		super.doDelete(req, resp);
	}
	
}
