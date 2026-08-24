package com.pawpawfind.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaResponse;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportFeatureRepository;

/** 권한과 신고 조건을 검증한 뒤 AI 추천 흐름을 조정한다. */
@Service
public class SearchAreaService {

	private final ReportService reportService;
	private final ReportFeatureRepository reportFeatureRepository;
	private final SearchAreaRequestMapper requestMapper;
	private final SearchAreaAiClient aiClient;

	public SearchAreaService(ReportService reportService,
			ReportFeatureRepository reportFeatureRepository,
			SearchAreaRequestMapper requestMapper,
			SearchAreaAiClient aiClient) {
		this.reportService = reportService;
		this.reportFeatureRepository = reportFeatureRepository;
		this.requestMapper = requestMapper;
		this.aiClient = aiClient;
	}

	public SearchAreaResponse recommend(Long reportId, Long userId, String role) {
		reportService.assertCanManageReport(reportId, userId, role);
		Reports report = reportService.getReport(reportId);
		if (report == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "제보를 찾을 수 없습니다.");
		}
		if (!"LOST".equals(report.getReportType())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"실종 신고만 추천 수색 영역을 요청할 수 있습니다.");
		}
		if (!"강아지".equals(report.getSpecies())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"강아지 신고만 추천 수색 영역을 요청할 수 있습니다.");
		}
		List<ReportFeatures> features = reportFeatureRepository.findByReportId(reportId);
		SearchAreaAiRequest request = requestMapper.map(report, features);
		return aiClient.recommend(request);
	}
}
