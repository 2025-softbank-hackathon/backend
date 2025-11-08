#!/bin/bash
set -e

echo "🚀 Starting application..."

cd /home/ec2-user/app

# .env.deploy 파일 확인
if [ ! -f .env.deploy ]; then
    echo "❌ Error: .env.deploy not found!"
    exit 1
fi

source .env.deploy

# ECR 로그인 (before_install에서 했지만 한 번 더 확인)
echo "🔐 Logging in to ECR..."
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin $(echo ${ECR_URI} | cut -d'/' -f1)

# 이미지 pull
echo "📦 Pulling new image: ${FULL_IMAGE}"
docker pull ${FULL_IMAGE}

# 컨테이너 시작
echo "🚀 Starting new container..."
docker run -d \
  --name chatapp-container \
  -p 3000:3000 \
  -e DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME} \
  -e REDIS_HOST=${REDIS_HOST} \
  -e REDIS_PORT=${REDIS_PORT} \
  -e AWS_REGION=${AWS_REGION} \
  --restart unless-stopped \
  ${FULL_IMAGE}

echo "✅ Container started successfully!"

# 컨테이너 상태 확인
sleep 3
docker ps | grep chatapp-container

# 간단한 로그 확인
echo "📋 Container logs:"
docker logs --tail 20 chatapp-container

echo "✅ Application start completed!"