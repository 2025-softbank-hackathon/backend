#!/bin/bash
set -e

echo "🚀 Starting application..."

cd /home/ec2-user/app

if [ ! -f .env.deploy ]; then
    echo "❌ .env.deploy not found!"
    exit 1
fi

source .env.deploy

# 기존 컨테이너 정리
if docker ps -a | grep -q chatapp-container; then
  echo "🛑 Removing old container..."
  docker stop chatapp-container || true
  docker rm chatapp-container || true
fi

echo "🔐 Logging in to ECR..."
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin $(echo ${ECR_URI} | cut -d'/' -f1)

echo "📦 Pulling new image: ${FULL_IMAGE}"
docker pull ${FULL_IMAGE}

echo "🚀 Starting container..."
docker run -d \
  --name chatapp-container \
  -p 3000:3000 \
  --env-file .env.app \
  --restart unless-stopped \
  ${FULL_IMAGE}

echo "🔍 Checking container logs..."
sleep 3
docker logs chatapp-container

echo "📋 Environment variables in container:"
docker exec chatapp-container env | grep -E "REDIS|DYNAMO" || echo "⚠️ Redis/Dynamo env vars not found!"

sleep 2
docker ps | grep chatapp-container && echo "✅ Container running"