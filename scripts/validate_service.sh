#!/bin/bash
set -e

echo "🔍 Validating service..."

MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    ATTEMPT=$((ATTEMPT+1))
    echo "Health check attempt $ATTEMPT/$MAX_ATTEMPTS..."
    
    # 컨테이너 실행 확인
    if ! docker ps | grep -q chatapp-container; then
        echo "❌ Container is not running!"
        docker logs chatapp-container || true
        exit 1
    fi
    
    # 헬스체크 엔드포인트 확인
    # /health 엔드포인트가 있다면 사용, 없다면 메인 페이지 확인
    if curl -f -s http://localhost:3000/health > /dev/null 2>&1; then
        echo "✅ Service is healthy!"
        
        # 추가 확인: 실제 응답 내용
        RESPONSE=$(curl -s http://localhost:3000/health)
        echo "Health check response: $RESPONSE"
        
        exit 0
    fi
    
    # /health가 없다면 메인 페이지 확인
    if curl -f -s http://localhost:3000 > /dev/null 2>&1; then
        echo "✅ Service is responding!"
        exit 0
    fi
    
    if [ $ATTEMPT -lt $MAX_ATTEMPTS ]; then
        echo "⏳ Service not ready yet, waiting 2 seconds..."
        sleep 2
    fi
done

echo "❌ Service failed health check after $MAX_ATTEMPTS attempts"
echo "📋 Container logs:"
docker logs chatapp-container || true

echo "📊 Container status:"
docker ps -a | grep chatapp-container || true

exit 1