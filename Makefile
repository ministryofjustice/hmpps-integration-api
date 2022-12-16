authenticate-docker:
	./scripts/authenticate_docker.sh

build-dev:
	docker-compose build

build:
	docker build -t hmpps-integration-api .

serve: build-dev
	docker-compose up -d

publish: build-dev
	./scripts/publish.sh

stop:
	docker-compose down

unit-test:
	./gradlew test

smoke-test: serve
	# docker-compose exec client ./run_test.sh
	echo "TODO"

test: unit-test smoke-test

lint:
	./gradlew ktlintCheck

.PHONY: authenticate-docker build-dev test serve publish unit-test smoke-test build lint
