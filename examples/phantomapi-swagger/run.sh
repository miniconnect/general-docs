#!/bin/sh

SWAGGER_URL=http://localhost:8080/
( until curl -fsS "$SWAGGER_URL" >/dev/null 2>&1; do sleep 1; done; xdg-open "$SWAGGER_URL" >/dev/null 2>&1 ) &
WAITER_PID=$!; trap 'kill "$WAITER_PID" 2>/dev/null' INT TERM EXIT
docker compose up --remove-orphans
