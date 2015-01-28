package org.jailbreak.service.core;

import java.util.List;

import org.jailbreak.api.representations.Representations.Donation;

import com.google.common.base.Optional;

public interface DonationsManager {
	
	public Optional<Donation> getDonation(int id);
	public boolean updateDonation(Donation donation);
	public Optional<Donation> patchDonation(Donation donation);
	public List<Donation> getDonations();
	public Donation createDonation(Donation donation);
	public boolean deleteDonation(int id);

}
