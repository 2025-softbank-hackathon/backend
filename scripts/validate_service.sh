#!/bin/bash
set -e

echo "🔍 Validating service..."

MAX_ATTEMPTS=20
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
  ATTEMPT=$((ATTEMPT + 1))
  echo "Health check attempt $ATTEMPT/$MAX_ATTEMPTS..."

  if curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/health | grep -q "200"; then
    echo "✅ Health check passed!"
    exit 0
  fi

  echo "⏳ Waiting for service..."
  sleep 3
done

echo "❌ Health check failed after $MAX_ATTEMPTS attempts"
docker logs chatapp-container || true
exit 1
