#!/bin/bash
set -e

echo "🔧 Preparing for installation..."

# 앱 디렉토리 준비
sudo mkdir -p /home/ec2-user/app
cd /home/ec2-user/app

# Docker 설치 확인
if ! command -v docker &> /dev/null; then
  echo "🐳 Installing Docker..."
  sudo yum update -y
  sudo amazon-linux-extras install docker -y
  sudo service docker start
  sudo usermod -aG docker ec2-user
  echo "✅ Docker installed successfully"
else
  echo "✅ Docker already installed"
fi

# .env.deploy 파일 로드 (있을 경우)
if [ -f .env.deploy ]; then
    source .env.deploy
    echo "🔐 Logging in to ECR..."
    aws ecr get-login-password --region ${AWS_REGION} | \
      docker login --username AWS --password-stdin $(echo ${ECR_URI} | cut -d'/' -f1)
    echo "✅ ECR login successful"
else
    echo "⚠️ .env.deploy not found yet (will be copied soon)"
fi

# 오래된 Docker 이미지 정리
echo "🧹 Cleaning up old images..."
docker image prune -af || true

echo "✅ Preparation complete"
