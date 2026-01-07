#!/bin/bash
# Run with cloud infrastructure (PostgreSQL + Redis on cloud)
# This saves ~2-3GB RAM by not running local database containers

echo "🌩️  Starting app with CLOUD infrastructure..."
echo "📊 PostgreSQL: Render (Singapore)"
echo "📊 Redis: Redis Cloud (AP-East-1)"
echo ""

docker compose -f docker-compose.dev.yml up app
