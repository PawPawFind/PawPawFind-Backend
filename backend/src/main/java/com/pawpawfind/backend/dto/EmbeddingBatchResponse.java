package com.pawpawfind.backend.dto;

/** Batch upsert summary for AI jobs. */
public class EmbeddingBatchResponse {

	private int upserted;
	private int skipped;

	public EmbeddingBatchResponse() {
	}

	public EmbeddingBatchResponse(int upserted, int skipped) {
		this.upserted = upserted;
		this.skipped = skipped;
	}

	public int getUpserted() {
		return upserted;
	}

	public void setUpserted(int upserted) {
		this.upserted = upserted;
	}

	public int getSkipped() {
		return skipped;
	}

	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}
}
