#!/bin/bash
set -e

echo "🛑 Stopping old application..."

if docker ps -a | grep -q chatapp-container; then
  docker stop chatapp-container || true
  docker rm chatapp-container || true
  echo "✅ Container stopped and removed"
else
  echo "ℹ️ No container found"
fi
