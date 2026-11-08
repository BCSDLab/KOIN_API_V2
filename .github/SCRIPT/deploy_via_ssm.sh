#!/bin/bash
# Upload the built JAR and run the host deploy entrypoint through a restricted SSM document.
# Usage: deploy_via_ssm.sh stage|prod   (expects AWS credentials from GitHub OIDC)
set -euo pipefail
case "$1" in
  stage) instance=i-0fb8488ed7173abf0; document=BCSD-KOIN-Stage-Deploy ;;
  prod) instance=i-0130a45c4dffcfb48; document=BCSD-KOIN-Prod-Deploy ;;
  *) echo "unknown environment: $1" >&2; exit 2 ;;
esac
jar=build/libs/KOIN_API_V2.jar
sha256=$(sha256sum "$jar" | cut -d' ' -f1)
aws s3 cp --only-show-errors "$jar" "s3://bcsd-koin-releases-965394270103-apne2/$1/$GITHUB_SHA/KOIN_API_V2.jar"

command_id=$(aws ssm send-command --instance-ids "$instance" --document-name "$document" \
  --parameters "revision=$GITHUB_SHA,sha256=$sha256" --timeout-seconds 60 \
  --query Command.CommandId --output text)
echo "SSM command $command_id"

deadline=$((SECONDS + 1260))
while :; do
  sleep 10
  status=$(aws ssm get-command-invocation --command-id "$command_id" --instance-id "$instance" \
    --query Status --output text 2>/dev/null || echo Pending)
  case "$status" in
    Pending|InProgress|Delayed) [ "$SECONDS" -lt "$deadline" ] || { echo "timed out waiting for $command_id" >&2; exit 1; } ;;
    *) break ;;
  esac
done
aws ssm get-command-invocation --command-id "$command_id" --instance-id "$instance" \
  --query '[StandardOutputContent,StandardErrorContent]' --output text
echo "SSM status: $status"
[ "$status" = Success ]
