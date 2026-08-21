# PawPawFind Backend 기여 가이드

PawPawFind Backend의 일관된 협업을 위한 GitHub 작업 규칙을 정의합니다.

## 1. 브랜치 전략

- `main`: 배포 가능한 안정 버전을 관리합니다.
- `develop`: 다음 배포를 위한 통합 브랜치입니다.
- 기능 개발 및 수정 브랜치는 `develop`에서 생성합니다.
- 모든 작업 PR은 원칙적으로 `develop`을 Base Branch로 사용합니다.
- `main`과 `develop`에는 직접 Push하지 않습니다.

## 2. 작업 흐름

모든 작업은 다음 순서로 진행합니다.

1. GitHub Issue를 생성합니다.
2. 최신 `develop`을 기준으로 작업 브랜치를 생성합니다.
3. 작업 내용을 구현하고 테스트합니다.
4. 변경 단위에 맞게 커밋합니다.
5. 원격 저장소에 작업 브랜치를 Push합니다.
6. `develop`을 Base Branch로 PR을 생성합니다.
7. CI, CodeRabbit, 팀원 리뷰 결과를 확인합니다.
8. 리뷰 내용을 반영하고 모든 리뷰 대화를 해결합니다.
9. Squash Merge합니다.
10. Merge된 작업 브랜치를 삭제합니다.

## 3. Issue 규칙

- 구현 전에 반드시 Issue를 생성합니다.
- 하나의 Issue에는 하나의 명확한 작업 목적을 작성합니다.
- 중복 Issue가 없는지 먼저 확인합니다.
- 기능명세와 연결되는 작업은 기능 ID를 작성합니다.
- 작업 내용과 완료 조건을 체크리스트로 작성합니다.

### Issue 제목

```text
[Feature] 새로운 기능 구현
[Bug] 버그 수정
[Chore] 환경 설정 및 기타 작업
[Docs] 문서 작성 및 수정
[Refactor] 코드 구조 개선
[Test] 테스트 추가 및 수정
[CI] CI/CD 설정
```

## 4. 브랜치 규칙

브랜치 이름은 다음 형식을 사용합니다.

```text
작업유형/issue-이슈번호-작업명
```

사용 가능한 작업 유형은 다음과 같습니다.

| 작업 유형 | 설명 |
|---|---|
| `feat` | 새로운 기능 구현 |
| `fix` | 버그 수정 |
| `chore` | 환경 설정 및 기타 작업 |
| `docs` | 문서 작성 및 수정 |
| `refactor` | 기능 변경 없는 코드 구조 개선 |
| `test` | 테스트 추가 및 수정 |
| `ci` | CI/CD 설정 |

브랜치 이름 예시는 다음과 같습니다.

```text
feat/issue-12-search-area-prediction
fix/issue-15-report-photo-filter
chore/issue-1-github-templates
docs/issue-20-api-spec
refactor/issue-25-report-service
test/issue-30-search-area-test
ci/issue-35-backend-ci
```

브랜치 생성 전 최신 `develop`을 반영합니다.

```bash
git switch develop
git pull origin develop
git switch -c feat/issue-12-search-area-prediction
```

브랜치 이름의 작업명은 영문 소문자와 하이픈을 사용합니다.

## 5. 커밋 규칙

커밋 메시지는 다음 형식을 사용합니다.

```text
type: 한글 작업 요약
```

사용 가능한 `type`은 다음과 같습니다.

| Type | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 구조 개선 |
| `test` | 테스트 추가 및 수정 |
| `docs` | 문서 추가 및 수정 |
| `chore` | 환경 설정 및 기타 작업 |
| `ci` | CI/CD 설정 |
| `style` | 코드 포맷 및 스타일 수정 |

커밋 메시지 예시는 다음과 같습니다.

```text
feat: 추천 수색 영역 생성 기능 추가
fix: 제보 사진 조회 조건 수정
test: 추천 수색 영역 계산 테스트 추가
docs: GitHub 협업 컨벤션 추가
chore: 프로젝트 설정 파일 정리
ci: 백엔드 테스트 워크플로 추가
```

커밋 작성 시 다음 규칙을 지킵니다.

- 하나의 커밋에는 하나의 논리적인 변경만 포함합니다.
- 작업 내용을 이해할 수 있도록 구체적으로 작성합니다.
- 마침표는 사용하지 않습니다.
- 관련 없는 파일을 함께 커밋하지 않습니다.
- 단순히 `수정`, `작업`, `업데이트`처럼 의미가 불분명한 메시지는 사용하지 않습니다.

## 6. Pull Request 규칙

- PR의 Base Branch가 `develop`인지 확인합니다.
- 관련 Issue를 `Closes #이슈번호`로 연결합니다.
- 하나의 PR은 하나의 Issue를 해결하는 것을 원칙으로 합니다.
- 변경 내용과 구현 이유를 작성합니다.
- 실행한 테스트와 결과를 작성합니다.
- 관련 없는 파일 변경을 포함하지 않습니다.
- Reviewer, Assignee, Label을 지정합니다.
- 구현이 완료되지 않았다면 Draft PR을 사용합니다.
- 기능 또는 API 변경이 있다면 관련 문서를 함께 수정합니다.

### PR 제목

PR 제목은 다음 형식을 사용합니다.

```text
[작업 유형] 작업 내용
```

예시는 다음과 같습니다.

```text
[Feature] 추천 수색 영역 생성 기능 구현
[Bug] 제보 사진 조회 조건 수정
[Chore] 백엔드 CI 환경 구축
[Docs] GitHub 협업 컨벤션 추가
[Refactor] 제보 서비스 구조 개선
[Test] 추천 수색 영역 테스트 추가
[CI] GitHub Actions 테스트 워크플로 추가
```

### Issue 연결

PR 본문에는 다음과 같이 관련 Issue를 연결합니다.

```text
Closes #12
```

해당 PR이 Merge되면 연결된 Issue가 자동으로 닫힙니다.

Issue를 참고만 하고 자동으로 닫지 않을 경우 다음과 같이 작성합니다.

```text
Refs #12
```

## 7. 리뷰 규칙

### PR 작성자

- PR 생성 후 CI 결과를 먼저 확인합니다.
- CodeRabbit 리뷰 내용을 확인합니다.
- 리뷰 의견에 답변하거나 코드를 수정합니다.
- 의견을 반영하지 않는 경우 그 이유를 설명합니다.
- 코드 수정 후 어떤 내용을 변경했는지 댓글로 남깁니다.
- 모든 리뷰 대화가 해결된 후 Merge합니다.
- CodeRabbit 리뷰만으로 사람 리뷰를 대체하지 않습니다.

### 리뷰어

- 기능명세와 Issue의 완료 조건을 기준으로 검토합니다.
- 기능 오류, 예외 처리, 보안, 테스트 누락을 우선적으로 확인합니다.
- 단순한 취향보다 명확한 근거가 있는 내용을 리뷰합니다.
- 반드시 수정해야 하는 내용과 제안 사항을 구분합니다.
- 수정이 필요한 경우 파일과 코드 위치를 명확하게 작성합니다.
- 문제가 없다면 Approve합니다.

### 리뷰 의견 구분

리뷰 의견은 필요에 따라 다음과 같이 구분합니다.

```text
[Must] Merge 전에 반드시 수정해야 하는 내용
[Question] 구현 의도나 결정 이유를 확인하는 질문
[Suggestion] 반영을 권장하지만 필수는 아닌 제안
[Nit] 사소한 표현이나 스타일 개선
```

## 8. 테스트 규칙

Backend 테스트는 `backend` 디렉터리에서 실행합니다.

macOS/Linux:

```bash
cd backend
./gradlew clean test
```

Windows PowerShell:

```powershell
cd backend
.\gradlew.bat clean test
```

테스트와 관련해 다음 규칙을 지킵니다.

- 기능을 추가하거나 수정한 경우 관련 테스트를 함께 작성합니다.
- 정상 동작뿐 아니라 주요 에러 케이스도 확인합니다.
- 테스트가 실패한 상태로 PR을 Merge하지 않습니다.
- 로컬 테스트 결과와 CI 결과를 모두 확인합니다.
- 테스트를 실행하지 못했다면 PR 본문에 이유를 작성합니다.

## 9. Merge 규칙

PR을 Merge하기 전에 다음 조건을 확인합니다.

- CI가 성공했습니다.
- 필요한 팀원 승인을 받았습니다.
- 모든 리뷰 대화가 해결됐습니다.
- CodeRabbit 리뷰를 확인했습니다.
- 관련 Issue의 완료 조건을 충족했습니다.
- 관련 없는 변경이 포함되지 않았습니다.

Merge 방식은 `Squash Merge`를 사용합니다.

Merge가 완료되면 다음 작업을 진행합니다.

1. 연결된 Issue가 정상적으로 닫혔는지 확인합니다.
2. 원격 작업 브랜치를 삭제합니다.
3. 로컬 `develop` 브랜치를 최신 상태로 갱신합니다.

```bash
git switch develop
git pull origin develop
```

## 10. 보안 규칙

- API Key, 비밀번호, Access Token을 Git에 올리지 않습니다.
- 실제 환경변수 파일과 개인 설정 파일을 커밋하지 않습니다.
- 예시 설정이 필요한 경우 `.example` 파일을 사용합니다.
- `.idea`, 빌드 결과물 등 개인 개발 환경 파일을 커밋하지 않습니다.
- 민감한 정보가 커밋된 경우 즉시 팀원에게 알립니다.
- 노출된 API Key나 Token은 Git 기록에서 삭제하는 것만으로 끝내지 않고 반드시 폐기하거나 재발급합니다.