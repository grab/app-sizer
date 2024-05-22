#!/bin/bash -e

# We need to ensure this directory is writeable on start of the container
chmod 0777 /var/lib/grafana
# When Grafana runs for the first time, it copies the sample "App Download Size Breakdown" dashboard.
# The dashboard is copied here to ensure it also works with mounted volumes ("/var/lib/grafana/") using Docker's -v option.
# If grafana.db does not already exist in /var/lib/grafana, then the file is copied from /tmp/grafana to /var/lib/grafana.
if [ ! -f /var/lib/grafana/grafana.db ]; then
    cp /tmp/grafana/grafana.db /var/lib/grafana/grafana.db
fi

exec /usr/bin/supervisord