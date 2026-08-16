package com.pawpawfind.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 공공 API 보호소 공고. PK는 공공 고유번호 desertionNo.
 * 입양/봉사(adptn*, srvc*) 필드는 저장하지 않는다.
 */
@Entity
@Table(name = "animals")
public class Animal {

	/** 공공 고유번호. 우리 DB에서 발급하지 않는다. */
	@Id
	@Column(name = "desertion_no", length = 20)
	private String desertionNo;

	/** 공공 발견일 YYYYMMDD. 사용자 제보의 eventDate와는 다른 컬럼. */
	@Column(name = "happen_dt", length = 8)
	private String happenDt;

	@Column(name = "happen_place", length = 255)
	private String happenPlace;

	@Column(name = "up_kind_cd", length = 10)
	private String upKindCd;

	@Column(name = "up_kind_nm", length = 20)
	private String upKindNm;

	@Column(name = "kind_cd", length = 10)
	private String kindCd;

	@Column(name = "kind_nm", length = 100)
	private String kindNm;

	@Column(name = "kind_full_nm", length = 150)
	private String kindFullNm;

	@Column(name = "color_cd", length = 50)
	private String colorCd;

	@Column(name = "age", length = 50)
	private String age;

	@Column(name = "weight", length = 50)
	private String weight;

	@Column(name = "notice_no", length = 50)
	private String noticeNo;

	@Column(name = "notice_sdt", length = 8)
	private String noticeSdt;

	@Column(name = "notice_edt", length = 8)
	private String noticeEdt;

	@Column(name = "popfile1", columnDefinition = "TEXT")
	private String popfile1;

	@Column(name = "popfile2", columnDefinition = "TEXT")
	private String popfile2;

	@Column(name = "process_state", length = 30)
	private String processState;

	@Column(name = "sex_cd", length = 1)
	private String sexCd;

	@Column(name = "neuter_yn", length = 1)
	private String neuterYn;

	@Column(name = "special_mark", columnDefinition = "TEXT")
	private String specialMark;

	@Column(name = "care_reg_no", length = 30)
	private String careRegNo;

	@Column(name = "care_nm", length = 100)
	private String careNm;

	@Column(name = "care_tel", length = 30)
	private String careTel;

	@Column(name = "care_addr", length = 255)
	private String careAddr;

	@Column(name = "org_nm", length = 100)
	private String orgNm;

	/** 공공 API updTm. 우리 updatedAt과 구분. */
	@Column(name = "source_upd_tm", length = 30)
	private String sourceUpdTm;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public String getDesertionNo() {
		return desertionNo;
	}

	public void setDesertionNo(String desertionNo) {
		this.desertionNo = desertionNo;
	}

	public String getHappenDt() {
		return happenDt;
	}

    public void setHappenDt(String happenDt){
        this.happenDt = happenDt;
    }


	public String getHappenPlace() {
		return happenPlace;
	}

	public void setHappenPlace(String happenPlace) {
		this.happenPlace = happenPlace;
	}

	public String getUpKindCd() {
		return upKindCd;
	}

	public void setUpKindCd(String upKindCd) {
		this.upKindCd = upKindCd;
	}

	public String getUpKindNm() {
		return upKindNm;
	}

	public void setUpKindNm(String upKindNm) {
		this.upKindNm = upKindNm;
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

	public String getKindFullNm() {
		return kindFullNm;
	}

	public void setKindFullNm(String kindFullNm) {
		this.kindFullNm = kindFullNm;
	}

	public String getColorCd() {
		return colorCd;
	}

	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getNoticeNo() {
		return noticeNo;
	}

	public void setNoticeNo(String noticeNo) {
		this.noticeNo = noticeNo;
	}

	public String getNoticeSdt() {
		return noticeSdt;
	}

	public void setNoticeSdt(String noticeSdt) {
		this.noticeSdt = noticeSdt;
	}

	public String getNoticeEdt() {
		return noticeEdt;
	}

	public void setNoticeEdt(String noticeEdt) {
		this.noticeEdt = noticeEdt;
	}

	public String getPopfile1() {
		return popfile1;
	}

	public void setPopfile1(String popfile1) {
		this.popfile1 = popfile1;
	}

	public String getPopfile2() {
		return popfile2;
	}

	public void setPopfile2(String popfile2) {
		this.popfile2 = popfile2;
	}

	public String getProcessState() {
		return processState;
	}

	public void setProcessState(String processState) {
		this.processState = processState;
	}

	public String getSexCd() {
		return sexCd;
	}

	public void setSexCd(String sexCd) {
		this.sexCd = sexCd;
	}

	public String getNeuterYn() {
		return neuterYn;
	}

	public void setNeuterYn(String neuterYn) {
		this.neuterYn = neuterYn;
	}

	public String getSpecialMark() {
		return specialMark;
	}

	public void setSpecialMark(String specialMark) {
		this.specialMark = specialMark;
	}

	public String getCareRegNo() {
		return careRegNo;
	}

	public void setCareRegNo(String careRegNo) {
		this.careRegNo = careRegNo;
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

	public String getOrgNm() {
		return orgNm;
	}

	public void setOrgNm(String orgNm) {
		this.orgNm = orgNm;
	}

	public String getSourceUpdTm() {
		return sourceUpdTm;
	}

	public void setSourceUpdTm(String sourceUpdTm) {
		this.sourceUpdTm = sourceUpdTm;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


}
