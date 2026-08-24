package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoDistanceCalculatorTest {

	private final GeoDistanceCalculator calculator = new GeoDistanceCalculator();

	@Test
	void sameCoordinateIsZeroMeters() {
		assertThat(calculator.distanceMeters(37.5, 127.0, 37.5, 127.0)).isZero();
	}

	@Test
	void calculatesKnownDistanceAndPreservesNearnessOrder() {
		double cityHallToJamsil = calculator.distanceMeters(37.5665, 126.9780, 37.5133, 127.1001);
		double near = calculator.distanceMeters(37.5665, 126.9780, 37.5670, 126.9780);
		double far = calculator.distanceMeters(37.5665, 126.9780, 37.5760, 126.9780);

		assertThat(cityHallToJamsil).isBetween(11_000.0, 13_000.0);
		assertThat(near).isLessThan(far);
	}

	@Test
	void boundingBoxDefendsPolesAndCrossesDateLine() {
		GeoDistanceCalculator.BoundingBox dateLine = calculator.boundingBox(0.0, 179.99, 20_000);
		GeoDistanceCalculator.BoundingBox pole = calculator.boundingBox(89.99, 0.0, 20_000);

		assertThat(dateLine.crossesDateLine()).isTrue();
		assertThat(dateLine.minLongitude()).isPositive();
		assertThat(dateLine.maxLongitude()).isNegative();
		assertThat(pole.minLongitude()).isEqualTo(-180.0);
		assertThat(pole.maxLongitude()).isEqualTo(180.0);
	}
}
