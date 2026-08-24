package com.pawpawfind.backend.dto;

/**
 * AI /match 요청용 제보 특징 태그.
 */
public class MatchFeatureDto {

	private String category;
	private String keyword;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
