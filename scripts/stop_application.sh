#!/bin/bash
set -e

echo "🛑 Stopping application..."

# 실행 중인 컨테이너 중지 및 삭제
if docker ps -a | grep -q chatapp-container; then
    echo "Stopping existing container..."
    docker stop chatapp-container || true
    docker rm chatapp-container || true
    echo "✅ Container stopped and removed"
else
    echo "ℹ️ No container to stop"
fi

echo "✅ Application stopped successfully"