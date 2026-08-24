-- reports.color 제거 (색상은 report_features category=털색 으로 관리)
ALTER TABLE reports DROP COLUMN IF EXISTS color;
