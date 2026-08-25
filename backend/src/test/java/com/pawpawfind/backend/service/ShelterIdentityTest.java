package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShelterIdentityTest {

	private final ShelterIdentity identity = new ShelterIdentity();

	@Test
	void registrationNumberHasPriorityAndIsTrimmed() {
		ShelterIdentity.Values values = identity.resolve(" REG-123 ", " 서울시  중구 ").orElseThrow();
		assertThat(values.shelterKey()).isEqualTo("REG:REG-123");
		assertThat(values.careRegNo()).isEqualTo("REG-123");
		assertThat(values.normalizedAddress()).isEqualTo("서울시 중구");
	}

	@Test
	void normalizedAddressCreatesStableSha256FallbackKey() {
		ShelterIdentity.Values first = identity.resolve(null, "  서울시\t중구\n세종대로  1 ").orElseThrow();
		ShelterIdentity.Values second = identity.resolve(" ", "서울시 중구 세종대로 1").orElseThrow();

		assertThat(first.normalizedAddress()).isEqualTo("서울시 중구 세종대로 1");
		assertThat(first.addressHash()).hasSize(64).isEqualTo(second.addressHash());
		assertThat(first.shelterKey()).isEqualTo("ADDR:" + first.addressHash())
				.isEqualTo(second.shelterKey());
	}

	@Test
	void missingRegistrationNumberAndAddressIsSkipped() {
		assertThat(identity.resolve(null, "  ")).isEmpty();
	}
}
