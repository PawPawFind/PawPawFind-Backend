package com.pawpawfind.backend.dto;

import java.util.List;

/** BE → AI embedding batch export for shelter animals. */
public class AnimalForEmbeddingResponse {

	private List<AnimalForEmbeddingItem> items;

	public List<AnimalForEmbeddingItem> getItems() {
		return items;
	}

	public void setItems(List<AnimalForEmbeddingItem> items) {
		this.items = items;
	}

	public static class AnimalForEmbeddingItem {
		private String desertionNo;
		private String species;
		private List<String> photoUrls;
		private String kindCd;
		private String kindNm;
		private String colorCd;
		private String sexCd;
		private String careNm;
		private String careTel;
		private String careAddr;
		private String specialMark;
		private String updatedAt;

		public String getDesertionNo() {
			return desertionNo;
		}

		public void setDesertionNo(String desertionNo) {
			this.desertionNo = desertionNo;
		}

		public String getSpecies() {
			return species;
		}

		public void setSpecies(String species) {
			this.species = species;
		}

		public List<String> getPhotoUrls() {
			return photoUrls;
		}

		public void setPhotoUrls(List<String> photoUrls) {
			this.photoUrls = photoUrls;
		}

		public String getKindCd() {
			return kindCd;
		}

		public void setKindCd(String kindCd) {
			this.kindCd = kindCd;
		}

		public String getKindNm() {
			return kindNm;
		}

		public void setKindNm(String kindNm) {
			this.kindNm = kindNm;
		}

		public String getColorCd() {
			return colorCd;
		}

		public void setColorCd(String colorCd) {
			this.colorCd = colorCd;
		}

		public String getSexCd() {
			return sexCd;
		}

		public void setSexCd(String sexCd) {
			this.sexCd = sexCd;
		}

		public String getCareNm() {
			return careNm;
		}

		public void setCareNm(String careNm) {
			this.careNm = careNm;
		}

		public String getCareTel() {
			return careTel;
		}

		public void setCareTel(String careTel) {
			this.careTel = careTel;
		}

		public String getCareAddr() {
			return careAddr;
		}

		public void setCareAddr(String careAddr) {
			this.careAddr = careAddr;
		}

		public String getSpecialMark() {
			return specialMark;
		}

		public void setSpecialMark(String specialMark) {
			this.specialMark = specialMark;
		}

		public String getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(String updatedAt) {
			this.updatedAt = updatedAt;
		}
	}
}
