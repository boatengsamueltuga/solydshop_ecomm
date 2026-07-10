#!/usr/bin/env bash
set -euo pipefail

# Nightly Postgres backup. Run via cron from the crontab entry below
# (substitute your actual home directory, e.g. /root if logged in as root):
# crontab -e:
#   0 3 * * * bash /root/solydshop/scripts/backup-db.sh >> /root/solydshop/backups/backup.log 2>&1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Don't `source .env` - some values (e.g. the mail app password) contain
# unquoted spaces, which breaks shell parsing under `set -e`. Extract just
# the one variable this script actually needs.
DB_USERNAME=$(grep '^DB_USERNAME=' .env | cut -d '=' -f2-)

BACKUP_DIR="./backups"
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
FILE="$BACKUP_DIR/solydshop-$TIMESTAMP.sql.gz"

docker compose exec -T db pg_dump -U "$DB_USERNAME" solydShopdb | gzip > "$FILE"

echo "Backup written to $FILE"

find "$BACKUP_DIR" -name "solydshop-*.sql.gz" -mtime +7 -delete
