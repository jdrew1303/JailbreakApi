package org.jailbreak.service.base;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.jailbreak.api.representations.Representations.Donation;
import org.jailbreak.api.representations.Representations.Team;
import org.jailbreak.api.representations.Representations.Donation.DonationType;
import org.jailbreak.api.representations.Representations.Donation.DonationsFilters;
import org.jailbreak.service.core.DonationsManager;
import org.jailbreak.service.core.TeamsManager;
import org.jailbreak.service.db.DonationsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;

public class DonationsManagerImpl implements DonationsManager {
	
	private final DonationsDAO dao;
	private final TeamsManager teams;
	private final Logger LOG = LoggerFactory.getLogger(DonationsManagerImpl.class);
	
	@Inject
	public DonationsManagerImpl(DonationsDAO dao, TeamsManager teams) {
		this.dao = dao;
		this.teams = teams;
	}

	@Override
	public Optional<Donation> getDonation(int id) {
		return this.dao.getDonation(id);
	}

	@Override
	public boolean updateDonation(Donation donation) {
		int result = this.dao.update(donation);
		if (result == 1)
			return true;
		else
			return false;
	}
	
	@Override
	public Donation createDonation(Donation donation) {
		LOG.info("Creating donatino for amount " + donation.getAmount());
		if (!donation.hasTime()) {
			donation = donation.toBuilder()
					.setTime(System.currentTimeMillis()/1000L)
					.build();
		}
		if (!donation.hasType()) {
			donation = donation.toBuilder()
					.setType(DonationType.ONLINE)
					.build();
		}
		int newId = this.dao.insert(donation);
		
		
		// update the count on the teams object
		if (donation.hasTeamId()) {
			Optional<Team> maybeTeam = teams.getTeam(donation.getTeamId());
			if (maybeTeam.isPresent()) {
				Team team = maybeTeam.get();
				LOG.info("Old amount raised online: " + team.getAmountRaisedOnline()/100 + " euro");
				LOG.info("Updating team " + team.getId() + " amount +" + donation.getAmount()/100 + " euro");
				team = team.toBuilder()
					.setAmountRaisedOnline(team.getAmountRaisedOnline() + donation.getAmount())
					.build();
				LOG.info("New amount raised online: " + team.getAmountRaisedOnline()/100 + " euro");
				teams.updateTeam(team);
			}
		}
		
		return this.dao.getDonation(newId).get(); // return full object with defaults set by DB
	}
	
	@Override
	public Optional<Donation> patchDonation(Donation donation) {
		Optional<Donation> maybeCurrent = dao.getDonation(donation.getId());
		if (maybeCurrent.isPresent()) {
			Donation merged = maybeCurrent.get().toBuilder().mergeFrom(donation).build();
			dao.update(merged);
			return Optional.of(merged);
		}
		return Optional.absent();
	}

	@Override
	public List<Donation> getDonations(int limit) {
		return this.dao.getDonations(limit);
	}
	
	@Override
	public List<Donation> getDonations(int limit, DonationsFilters filters) {
		try {
			return this.dao.getFilteredDonations(limit, filters);
		} catch (SQLException e) {
			throw new WebApplicationException();
		}
	}

	@Override
	public boolean deleteDonation(int id) {
		int result = this.dao.delete(id);
		if (result == 1)
			return true;
		else
			return false;
	}
	
	@Override
	public int getTotalRaised() {
		return dao.getDonationsTotalAmount();
	}

}
