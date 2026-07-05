#!/usr/bin/env bash
set -euo pipefail

# Nightly Postgres backup. Run via cron from the crontab entry below.
# crontab -e:
#   0 3 * * * bash /home/ubuntu/solydshop/scripts/backup-db.sh >> /home/ubuntu/solydshop/backups/backup.log 2>&1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

set -a
source .env
set +a

BACKUP_DIR="./backups"
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
FILE="$BACKUP_DIR/solydshop-$TIMESTAMP.sql.gz"

docker compose exec -T db pg_dump -U "$DB_USERNAME" solydShopdb | gzip > "$FILE"

echo "Backup written to $FILE"

find "$BACKUP_DIR" -name "solydshop-*.sql.gz" -mtime +7 -delete
