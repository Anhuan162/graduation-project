# Cloud vs Local Infrastructure

## 🌩️ Cloud Mode (Recommended for 16GB RAM)

**Saves ~2-3GB RAM** by using cloud-hosted PostgreSQL and Redis instead of local Docker containers.

### Quick Start
```bash
./run-cloud.sh
```

### Current Cloud Services
- **PostgreSQL**: Render (Singapore) - `forum_ptit` database
- **Redis**: Redis Cloud (AP-East-1)

### Configuration
All cloud credentials are in `.env` file:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, etc.

### Pros
✅ Saves 2-3GB RAM  
✅ No local database management  
✅ Data persists across machine restarts  

### Cons
❌ Requires internet connection  
❌ Higher latency (~50-200ms vs <1ms)  
❌ Free tier limits (connections, storage)  

---

## 💻 Local Mode

Runs PostgreSQL and Redis in Docker containers on your machine.

### Quick Start
```bash
./run-local.sh
```

### Configuration
Update `.env` to use local settings:
```bash
# Comment out cloud settings, uncomment local:
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_SSL=false

# Database will use docker-compose defaults
```

### Pros
✅ Faster (<1ms latency)  
✅ Works offline  
✅ No external dependencies  

### Cons
❌ Uses ~2-3GB extra RAM  
❌ Need to manage Docker volumes  

---

## Switching Between Modes

### To Cloud
1. Make sure `.env` has cloud credentials (already configured)
2. Stop current containers: `docker compose -f docker-compose.dev.yml down`
3. Run: `./run-cloud.sh`

### To Local
1. Update `.env` with local settings
2. Stop current containers: `docker compose -f docker-compose.dev.yml down`
3. Run: `./run-local.sh`

---

## Technical Details

### Docker Compose Profiles
- **No profile** (default): Runs only `app` container
- **`--profile local`**: Runs `app`, `db`, and `redis` containers

### Environment Variables
The app reads database/Redis config from environment variables with fallbacks:
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://db:5432/authdb`)
- `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default: `root`)
- `REDIS_HOST` (required)
- `REDIS_PORT` (required)
- `REDIS_PASSWORD` (optional)
- `REDIS_SSL` (default: `false`)

### Dependency Management
The `app` service has optional dependencies on `db` and `redis`:
```yaml
depends_on:
  db:
    condition: service_healthy
    required: false  # Won't fail if db doesn't exist
  redis:
    condition: service_healthy
    required: false  # Won't fail if redis doesn't exist
```

This allows the app to start even when `db` and `redis` containers aren't running (cloud mode).
