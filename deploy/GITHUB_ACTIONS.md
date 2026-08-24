# GitHub Actions 배포 (Organization — repo 3개)

PawPawFind org은 **Backend / Frontend / AI repo가 분리**되어 있다.  
각 repo에 **워크플로 1개 + Secrets**만 등록하면 된다.

```
PawPawFind-Backend  ──push──► EC2 systemd restart (pawpawfind-backend)
PawPawFind-AI       ──push──► EC2 systemd restart (pawpawfind-ai)
PawPawFind-Frontend ──push──► S3 static sync (+ CloudFront optional)
```

브랜치: **`develop`**, **`main`** push 시 자동.  
수동: Actions → **Run workflow**.

---

## Repo별 워크플로

| Repo | 파일 | 상태 |
|------|------|------|
| **PawPawFind-Backend** | `.github/workflows/deploy-backend.yml` | ✅ 포함됨 |
| **PawPawFind-AI** | `.github/workflows/deploy-ai.yml` | ✅ 포함됨 |
| **PawPawFind-Frontend** | `.github/workflows/deploy-frontend.yml` | ⬇️ `deploy/frontend-repo-workflow.yml` 복사 |

Frontend repo에 아직 없으면:

```bash
# Frontend repo clone 후
mkdir -p .github/workflows
cp /path/to/PawPawFind-Backend/deploy/frontend-repo-workflow.yml .github/workflows/deploy-frontend.yml
git add .github/workflows/deploy-frontend.yml
git commit -m "ci: add S3 deploy workflow"
git push
```

---

## Secrets (repo별)

### PawPawFind-Backend

| Secret | 값 |
|--------|-----|
| `EC2_HOST` | `54.180.121.41` |
| `EC2_USER` | `ec2-user` |
| `EC2_SSH_PRIVATE_KEY` | pem 전체 |

### PawPawFind-AI

| Secret | 값 |
|--------|-----|
| `EC2_HOST` | 동일 |
| `EC2_USER` | `ec2-user` |
| `EC2_SSH_PRIVATE_KEY` | 동일 |

### PawPawFind-Frontend

| Secret | 값 |
|--------|-----|
| `AWS_ACCESS_KEY_ID` | IAM Access Key |
| `AWS_SECRET_ACCESS_KEY` | IAM Secret |
| `AWS_REGION` | `ap-southeast-2` |
| `FRONTEND_S3_BUCKET` | `pawpawfind-dev-web` |
| `CLOUDFRONT_DISTRIBUTION_ID` | (선택) |

**Tip:** Organization → Settings → Secrets → **Actions**에 `EC2_*`를 org secret으로 두면 Backend·AI repo에서 공유 가능.

---

## IAM (Frontend S3)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": "arn:aws:s3:::pawpawfind-dev-web"
    },
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:DeleteObject", "s3:GetObject"],
      "Resource": "arn:aws:s3:::pawpawfind-dev-web/*"
    }
  ]
}
```

---

## EC2 (Backend + AI 공통, 최초 1회)

```bash
bash ~/deploy/setup-ec2.sh
mkdir -p ~/PawPawFind-AI ~/logs
```

---

## Backend repo 디렉터리 구조 참고

현재 **PawPawFind-Backend** 로컬/원격은 `backend/` 하위에 Gradle 프로젝트가 있다.  
나중에 repo 루트를 Gradle root로 flatten하면 `deploy-backend.yml`에서:

- `working-directory: backend` → `.`
- `paths: backend/**` → `src/**` 등으로 수정.

---

## 보안 (나중에)

GitHub hosted runner → EC2 SSH(22)는 SG에서 IP 제한이 어렵다.  
운영 단계에서는 **self-hosted runner on EC2** 또는 **AWS SSM** 검토.
