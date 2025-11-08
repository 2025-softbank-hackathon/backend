#!/bin/bash
set -e

echo "📦 After install tasks running..."
cd /home/ec2-user/app

# 혹시 npm 빌드 결과물이 필요한 경우 (React 등)
if [ -f package.json ]; then
    echo "📦 Installing Node dependencies..."
    sudo npm install --omit=dev || true
fi

echo "✅ After install completed"
