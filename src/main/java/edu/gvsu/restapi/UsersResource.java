package edu.gvsu.restapi;

import java.util.List;

import org.json.JSONArray;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.googlecode.objectify.ObjectifyService;

import edu.gvsu.restapi.shared.RegistrationInfo;

public class UsersResource extends ServerResource{
	private List<RegistrationInfo> users = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
	    this.users = ObjectifyService.ofy()
	        .load()
	        .type(RegistrationInfo.class)
	        .list();
	
			getVariants().add(new Variant(MediaType.TEXT_HTML));
			getVariants().add(new Variant(MediaType.APPLICATION_JSON));

	}
	
	/**
	 * Represent an error message in the requested format.
	 *
	 * @param variant
	 * @param em
	 * @return
	 * @throws ResourceException
	 */
	private Representation representError(Variant variant, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}

	protected Representation representError(MediaType type, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (type.equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}
	
	/**
	 * Handle an HTTP GET. Represent the widget object in the requested format.
	 *
	 * @param variant
	 * @return
	 * @throws ResourceException
	 */
	@Get
	public Representation get(Variant variant) throws ResourceException {
		Representation result = null;
		if (null == this.users) {
			ErrorMessage em = new ErrorMessage();
			return representError(variant, em);
		} else {
			System.out.println("*********************MediaType:"+variant.getMediaType());
			if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {

				JSONArray widgetArray = new JSONArray();
				for(Object o : this.users) {
					RegistrationInfo u = (RegistrationInfo)o;
					widgetArray.put(u.toJSON());
				}

				result = new JsonRepresentation(widgetArray);

			} else {

				// create a plain text representation of our list of widgets
				StringBuffer buf = new StringBuffer("<html><head><title>Users Resources</title><head><body><h1>Registered Users</h1>");
				buf.append("<form name=\"input\" action=\"/v1/users\" method=\"POST\">");
				buf.append("User name: ");
				buf.append("<input type=\"text\" name=\"name\" />");
				buf.append("Host: ");
				buf.append("<input type=\"text\" name=\"host\" />");
				buf.append("Port: ");
				buf.append("<input type=\"text\" name=\"port\" />");
				buf.append("Status: ");
				buf.append("<input type=\"text\" name=\"status\" />");
				buf.append("<input type=\"submit\" value=\"Create\" />");
				buf.append("</form>");
				buf.append("<br/><h2> There are " + this.users.size() + " total.</h2>");
				for(Object o : this.users) {
					RegistrationInfo u = (RegistrationInfo)o;
					buf.append(u.toHtml(true));
				}
				buf.append("</body></html>");
				result = new StringRepresentation(buf.toString());
				result.setMediaType(MediaType.TEXT_HTML);
			}
		}
		return result;
	}
	
	/**
	 * Handle a POST Http request. Create a new widget
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Post
	public Representation post(Representation entity, Variant variant)
		throws ResourceException
	{

		Representation rep = null;

		// We handle only a form request in this example. Other types could be
		// JSON or XML.
		try {
			if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
					true))
			{
				// Use the incoming data in the POST request to create/store a new widget resource.
				Form form = new Form(entity);
				RegistrationInfo r = new RegistrationInfo();
				r.setName(form.getFirstValue("name"));
				r.setHost(form.getFirstValue("host"));
				r.setPort(Integer.parseInt(form.getFirstValue("port")));
				r.setStatus(Boolean.parseBoolean(form.getFirstValue("status")));
				
				if(!users.contains(r)){
			        // persist updated object
			        ObjectifyService.ofy().save().entity(r).now();
	
					getResponse().setStatus(Status.SUCCESS_OK);
					rep = new JsonRepresentation(r.toJSON());
					rep.setMediaType(MediaType.APPLICATION_JSON);
					getResponse().setEntity(rep);
				}else{
					getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
				}

			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return rep;
	}
}
