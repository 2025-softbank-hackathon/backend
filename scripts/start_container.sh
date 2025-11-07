#!/bin/bash
set -e

cd /home/ec2-user/app
source .env.deploy

echo "🔐 Logging in to ECR..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin $(echo ${ECR_URI} | cut -d'/' -f1)

echo "🛑 Stopping existing container..."
docker stop chatapp-container 2>/dev/null || true
docker rm chatapp-container 2>/dev/null || true

echo "📦 Pulling new image: ${FULL_IMAGE}"
docker pull ${FULL_IMAGE}

echo "🚀 Starting new container..."
docker run -d \
  --name chatapp-container \
  -p 3000:3000 \
  -e DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME} \
  -e REDIS_HOST=${REDIS_HOST} \
  -e REDIS_PORT=${REDIS_PORT} \
  -e AWS_REGION=${AWS_REGION} \
  ${FULL_IMAGE}

echo "✅ Deployment completed!"
docker ps | grep chatapp-container