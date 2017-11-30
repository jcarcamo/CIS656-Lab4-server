package edu.gvsu.restapi;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import edu.gvsu.restapi.shared.RegistrationInfo;

public class UserResource extends ServerResource{
	private RegistrationInfo user = null;
	@Override
	public void doInit() {

		// URL requests routed to this resource have the widget id on them.
		String userid = null;
		userid = (String) getRequest().getAttributes().get("name");

		// lookup the widget in google's persistance layer.
		Key<RegistrationInfo> theKey = Key.create(RegistrationInfo.class, userid);
		this.user = ObjectifyService.ofy()
				.load()
				.key(theKey)
				.now();

		// these are the representation types this resource supports.
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
	 * Represent the widget object in the requested format.
	 *
	 * @param variant
	 * @return
	 * @throws ResourceException
	 */
	@Get
	public Representation get(Variant variant) throws ResourceException {
		Representation result = null;
		if (null == this.user) {
			ErrorMessage em = new ErrorMessage();
			return representError(variant, em);
		} else {
			if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
				result = new JsonRepresentation(this.user.toJSON());
			} else {
				result = new StringRepresentation(this.user.toHtml(false));
				result.setMediaType(MediaType.TEXT_HTML);
			}
		}
		return result;
	}

	/**
	 * Handle a PUT Http request. Update an existing widget
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Put
	public Representation put(Representation entity)
			throws ResourceException
	{
		Representation rep = null;
		try {
			if (null == this.user) {
				ErrorMessage em = new ErrorMessage();
				rep = representError(entity.getMediaType(), em);
				getResponse().setEntity(rep);
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return rep;
			}
			if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
					true)) {
				Form form = new Form(entity);
				this.user.setStatus(Boolean.parseBoolean(form.getFirstValue("status")));

				// persist object
				ObjectifyService.ofy()
				.save()
				.entity(this.user)
				.now();

				getResponse().setStatus(Status.SUCCESS_OK);
				rep = new JsonRepresentation(this.user.toJSON());
				getResponse().setEntity(rep);

			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return rep;
	}

	/**
	 * Handle a DELETE Http Request. Delete an existing widget
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Delete
	public Representation delete(Variant variant)
			throws ResourceException
	{
		Representation rep = null;
		try {
			if (null == this.user) {
				ErrorMessage em = new ErrorMessage();
				rep = representError(MediaType.APPLICATION_JSON, em);
				getResponse().setEntity(rep);
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return rep;
			}

			try {
				rep = new JsonRepresentation(this.user.toJSON());

				// remove from persistance layer
				ObjectifyService.ofy()
				.delete()
				.entity(this.user);

			} finally {

			}

			getResponse().setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return rep;
	}



}
