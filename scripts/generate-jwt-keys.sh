#!/usr/bin/env bash
set -euo pipefail

keys_dir="$(cd "$(dirname "$0")/.." && pwd)/keys"
private_key="$keys_dir/jwt-private.pem"
public_key="$keys_dir/jwt-public.pem"

if [[ -e "$private_key" || -e "$public_key" ]]; then
  echo "As chaves JWT já existem em $keys_dir. Nada foi alterado."
  exit 0
fi

mkdir -p "$keys_dir"
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out "$private_key"
openssl rsa -pubout -in "$private_key" -out "$public_key"
chmod 600 "$private_key"

echo "Chaves JWT criadas em $keys_dir"
