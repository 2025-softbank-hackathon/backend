#!/bin/bash
set -e

echo "🔧 Preparing for installation..."

# 앱 디렉토리 확인
mkdir -p /home/ec2-user/app
cd /home/ec2-user/app

# .env.deploy 파일이 있다면 로드
if [ -f .env.deploy ]; then
    source .env.deploy
    
    echo "🔐 Logging in to ECR..."
    aws ecr get-login-password --region ${AWS_REGION} | \
      docker login --username AWS --password-stdin $(echo ${ECR_URI} | cut -d'/' -f1)
    echo "✅ ECR login successful"
else
    echo "⚠️ .env.deploy not found yet, will be copied by CodeDeploy"
fi

# 이전 이미지 정리 (옵션)
echo "🧹 Cleaning up old images..."
docker image prune -af || true

echo "✅ Preparation completed successfully"