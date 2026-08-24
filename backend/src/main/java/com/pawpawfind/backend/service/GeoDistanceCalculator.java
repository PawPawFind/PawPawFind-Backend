package com.pawpawfind.backend.service;

import org.springframework.stereotype.Component;

/** 구면 지구 모델 기반 bounding box와 Haversine 직선거리를 계산한다. */
@Component
public class GeoDistanceCalculator {

	static final double EARTH_RADIUS_METERS = 6_371_000.0;

	public double distanceMeters(double latitude1, double longitude1,
			double latitude2, double longitude2) {
		double latitudeDelta = Math.toRadians(latitude2 - latitude1);
		double longitudeDelta = Math.toRadians(longitude2 - longitude1);
		double latitude1Radians = Math.toRadians(latitude1);
		double latitude2Radians = Math.toRadians(latitude2);
		double sinLatitude = Math.sin(latitudeDelta / 2.0);
		double sinLongitude = Math.sin(longitudeDelta / 2.0);
		double haversine = sinLatitude * sinLatitude
				+ Math.cos(latitude1Radians) * Math.cos(latitude2Radians)
				* sinLongitude * sinLongitude;
		double centralAngle = 2.0 * Math.asin(Math.sqrt(Math.min(1.0, haversine)));
		return EARTH_RADIUS_METERS * centralAngle;
	}

	public BoundingBox boundingBox(double latitude, double longitude, double radiusMeters) {
		double latitudeRadians = Math.toRadians(latitude);
		double longitudeRadians = Math.toRadians(longitude);
		double angularRadius = radiusMeters / EARTH_RADIUS_METERS;
		double minLatitudeRadians = Math.max(-Math.PI / 2.0, latitudeRadians - angularRadius);
		double maxLatitudeRadians = Math.min(Math.PI / 2.0, latitudeRadians + angularRadius);

		double longitudeDelta;
		if (minLatitudeRadians <= -Math.PI / 2.0 || maxLatitudeRadians >= Math.PI / 2.0
				|| Math.abs(Math.cos(latitudeRadians)) < 1.0e-12) {
			longitudeDelta = Math.PI;
		} else {
			double ratio = Math.sin(angularRadius) / Math.cos(latitudeRadians);
			longitudeDelta = Math.asin(Math.min(1.0, Math.abs(ratio)));
		}

		double minLongitude;
		double maxLongitude;
		boolean crossesDateLine = false;
		if (longitudeDelta >= Math.PI) {
			minLongitude = -180.0;
			maxLongitude = 180.0;
		} else {
			double minLongitudeRadians = longitudeRadians - longitudeDelta;
			double maxLongitudeRadians = longitudeRadians + longitudeDelta;
			if (minLongitudeRadians < -Math.PI) {
				minLongitudeRadians += 2.0 * Math.PI;
				crossesDateLine = true;
			}
			if (maxLongitudeRadians > Math.PI) {
				maxLongitudeRadians -= 2.0 * Math.PI;
				crossesDateLine = true;
			}
			minLongitude = Math.toDegrees(minLongitudeRadians);
			maxLongitude = Math.toDegrees(maxLongitudeRadians);
		}

		return new BoundingBox(Math.toDegrees(minLatitudeRadians), Math.toDegrees(maxLatitudeRadians),
				minLongitude, maxLongitude, crossesDateLine);
	}

	public record BoundingBox(double minLatitude, double maxLatitude,
			double minLongitude, double maxLongitude, boolean crossesDateLine) {
	}
}
