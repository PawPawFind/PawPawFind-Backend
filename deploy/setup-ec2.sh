#!/usr/bin/env bash
# EC2(Amazon Linux 2023)에서 root 또는 sudo로 실행.
# 사용: scp deploy/* ec2-user@54.180.121.41:~/deploy/ 후 ssh 접속해서 bash ~/deploy/setup-ec2.sh

set -euo pipefail

DEPLOY_DIR="${HOME}/deploy"
LOG_DIR="${HOME}/logs"

echo "==> logs 디렉터리"
mkdir -p "${LOG_DIR}"

echo "==> OpenCV ML 의존성 (libGL)"
sudo dnf install -y mesa-libGL || true

echo "==> systemd 유닛 설치"
sudo cp "${DEPLOY_DIR}/pawpawfind-ai.service" /etc/systemd/system/
sudo cp "${DEPLOY_DIR}/pawpawfind-backend.service" /etc/systemd/system/
sudo systemctl daemon-reload

echo "==> 기존 nohup 프로세스 종료"
pkill -f 'backend-0.0.1-SNAPSHOT.jar' || true
pkill -f 'uvicorn app.main:app' || true
sleep 2

echo "==> nginx 설치 및 설정"
sudo dnf install -y nginx
sudo cp "${DEPLOY_DIR}/nginx-pawpawfind.conf" /etc/nginx/conf.d/pawpawfind.conf
# default server 비활성화(충돌 방지)
if [ -f /etc/nginx/nginx.conf ] && grep -q 'include /usr/share/nginx/modules' /etc/nginx/nginx.conf; then
  sudo mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.bak 2>/dev/null || true
fi
sudo nginx -t
sudo systemctl enable --now nginx

echo "==> AI / Backend 서비스 enable & start"
sudo systemctl enable --now pawpawfind-ai
sudo systemctl enable --now pawpawfind-backend

echo "==> 상태 확인"
sudo systemctl status nginx --no-pager -l || true
sudo systemctl status pawpawfind-ai --no-pager -l || true
sudo systemctl status pawpawfind-backend --no-pager -l || true

echo ""
echo "완료. API: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo 'PUBLIC_IP')/"
echo "Security Group 인바운드 80/tcp 열려 있는지 확인하세요."
