package com.pawpawfind.backend.dto;

/**
 * 보호동물 매칭 후보의 보호소 정보.
 */
public class MatchShelterDto {

	private String careRegNo;
	private String name;
	private String address;
	private String telephone;
	private Double latitude;
	private Double longitude;

	public String getCareRegNo() { return careRegNo; }
	public void setCareRegNo(String careRegNo) { this.careRegNo = careRegNo; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getTelephone() { return telephone; }
	public void setTelephone(String telephone) { this.telephone = telephone; }
	public Double getLatitude() { return latitude; }
	public void setLatitude(Double latitude) { this.latitude = latitude; }
	public Double getLongitude() { return longitude; }
	public void setLongitude(Double longitude) { this.longitude = longitude; }
}
