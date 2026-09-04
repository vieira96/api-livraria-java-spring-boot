#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker não foi encontrado. Instale o Docker Engine ou Docker Desktop antes de continuar."
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "O plugin Docker Compose não foi encontrado."
    exit 1
fi

if [[ ! -f .env ]]; then
    cp .env.example .env
    echo "Arquivo .env criado a partir de .env.example."
fi

server_port="$(grep '^SERVER_PORT=' .env | cut -d= -f2- || true)"
server_port="${server_port:-8000}"

if ! [[ "$server_port" =~ ^[0-9]+$ ]]; then
    echo "SERVER_PORT deve ser uma porta numérica no arquivo .env."
    exit 1
fi

echo "Construindo e iniciando a API e o PostgreSQL..."
echo
echo "Projeto pronto em http://localhost:${server_port}"
echo "Documentação: http://localhost:${server_port}/scalar"
echo "Watch ativo. Use Ctrl+C para encerrar."

docker compose --env-file .env -f docker/docker-compose.yml up --build --watch
