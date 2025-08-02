# This script sets up an SSH tunnel to forward a local port to a remote PostgreSQL database.

if ! lsof -i :15432 > /dev/null; then
    ssh -N -L 15432:localhost:5432 bdch@bdch -p 11111
  echo "Starting SSH tunnel to forward local port 15432 to remote PostgreSQL database on port 5432..."
else
  echo "SSH tunnel is already running on port 15432."
fi
