authenticate-docker:
	./scripts/authenticate_docker.sh

build-dev:
	docker-compose pull hmpps-auth
	docker-compose build

build:
	docker build -t hmpps-integration-api .

serve: build-dev
	docker-compose up -d

publish:
	./scripts/publish.sh

stop:
	docker-compose down

unit-test:
	./gradlew unitTest

smoke-test: serve
	./gradlew smokeTest

smoke-test-deployed-env:
	./scripts/smoke-test-deployed-environment.sh

test: unit-test smoke-test

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

check:
	./gradlew check

.PHONY: authenticate-docker build-dev test serve publish unit-test smoke-test build lint
