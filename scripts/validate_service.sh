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
    # 먼저 /health 시도
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/health 2>/dev/null | grep -q "200"; then
        echo "✅ Service is healthy! (/health endpoint)"
        exit 0
    fi
    
    # /health가 없다면 메인 페이지 확인 (200 or 304)
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "304" ] || [ "$HTTP_CODE" = "301" ]; then
        echo "✅ Service is responding! (HTTP $HTTP_CODE)"
        exit 0
    fi
    
    # 포트가 열려있는지만 확인
    if nc -z localhost 3000 2>/dev/null; then
        echo "✅ Port 3000 is open!"
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