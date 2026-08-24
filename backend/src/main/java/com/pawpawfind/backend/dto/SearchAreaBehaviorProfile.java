package com.pawpawfind.backend.dto;

public class SearchAreaBehaviorProfile {

	private String activityLevel;
	private String strangerResponse;
	private String noiseSensitivity;
	private String chaseTendency;
	private String mobility;
	private String escapeCause;

	public String getActivityLevel() { return activityLevel; }
	public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
	public String getStrangerResponse() { return strangerResponse; }
	public void setStrangerResponse(String strangerResponse) { this.strangerResponse = strangerResponse; }
	public String getNoiseSensitivity() { return noiseSensitivity; }
	public void setNoiseSensitivity(String noiseSensitivity) { this.noiseSensitivity = noiseSensitivity; }
	public String getChaseTendency() { return chaseTendency; }
	public void setChaseTendency(String chaseTendency) { this.chaseTendency = chaseTendency; }
	public String getMobility() { return mobility; }
	public void setMobility(String mobility) { this.mobility = mobility; }
	public String getEscapeCause() { return escapeCause; }
	public void setEscapeCause(String escapeCause) { this.escapeCause = escapeCause; }
}
