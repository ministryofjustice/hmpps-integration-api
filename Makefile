authenticate-docker:
	./scripts/authenticate_docker.sh

build-dev:
	#docker compose pull hmpps-auth
	docker compose build

build:
	docker build -t hmpps-integration-api .

serve-dependencies:
	docker compose up hmpps-auth prism local-stack-aws --build -d

serve: build-dev
	docker compose up -d --wait

publish:
	./scripts/publish.sh

stop:
	docker compose down

unit-test:
	./gradlew unitTest

smoke-test: serve
	./gradlew smokeTest --warning-mode all

integration-test: serve
	./gradlew integrationTest --warning-mode all

heartbeat:
	./scripts/heartbeat.sh

test: unit-test smoke-test

e2e:
	./gradlew integrationTest --warning-mode all

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

check:
	./gradlew check

generate-client-certificate:
	cd ./scripts/client_certificates && ./generate.sh

.PHONY: authenticate-docker build-dev test serve publish unit-test smoke-test build lint
