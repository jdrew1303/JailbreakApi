package org.jailbreak.service.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jailbreak.api.representations.Representations.StripeChargeRequest;
import org.jailbreak.service.core.StripeManager;
import org.jailbreak.service.errors.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.stripe.exception.StripeException;

@Path(Paths.STRIPE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StripeResource {
	
	private final StripeManager manager;
	private final Logger LOG = LoggerFactory.getLogger(StripeResource.class);
	
	@Inject
	public StripeResource(StripeManager manager) {
		this.manager = manager;
	}
	
	@POST
	public Response charge(StripeChargeRequest request) {
		try {
			manager.chargeCard(request);
			return Response.status(Status.NO_CONTENT).build();
		} catch (StripeException e) {
			LOG.error("Charge failure: name: " + request.getEmail() + " amount: " + request.getAmount() + " message: "+ e.getMessage());
			throw new AppException(e.getMessage(), e);
		}
	}

}
