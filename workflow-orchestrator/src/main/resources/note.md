curl -s http://localhost:9998/actuator/health | jq

curl -s http://localhost:9998/actuator/metrics | jq .names

curl -s http://localhost:9998/actuator/metrics/workflow.triggered.total | jq

curl -s http://localhost:9998/actuator/metrics/workflow.runs.total | jq