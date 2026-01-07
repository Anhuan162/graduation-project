#!/bin/bash
# Run with LOCAL infrastructure (PostgreSQL + Redis in Docker)
# Uses more RAM (~2-3GB extra) but faster and works offline

echo "💻 Starting app with LOCAL infrastructure..."
echo "📊 PostgreSQL: Docker container"
echo "📊 Redis: Docker container"
echo ""

docker compose -f docker-compose.dev.yml --profile local up
