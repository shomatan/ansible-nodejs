#!/usr/bin/env bash
#
# -----------------------------------------------------------------------------

echo "** Preparing Ayumi CMS Server"

APP_SECRET=`date +%s | sha256sum | base64 | head -c 32 ; echo`

echo "########################################################"

echo "** Executing Ayumi CMS Server"

exec /app/bin/ayumi \
        -Dplay.http.secret.key="${APP_SECRET}" \
        -Dslick.dbs.default.db.url="jdbc:postgresql://${DB_HOST}/${DB_NAME}" \
        -Dslick.dbs.default.db.user="${DB_USER}" \
        -Dslick.dbs.default.db.password="${DB_PASS}" \
        -Dplay.evolutions.db.default.autoApply=true \
        "$@"